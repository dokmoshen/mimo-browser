package app.mimo.browser.web

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import app.mimo.browser.preferences.BrowserSettings
import app.mimo.browser.preferences.BrowserPreferences
import app.mimo.browser.data.DownloadRepository
import app.mimo.browser.data.HistoryRepository
import app.mimo.browser.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PageFailure(
    val url: String,
    val code: Int,
    val description: String,
)

data class TabState(
    val id: Long,
    val url: String = "",
    val title: String = "",
    val favicon: Bitmap? = null,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = false,
    val isDesktop: Boolean = false,
    val chromeBlurred: Boolean = false,
    val error: PageFailure? = null,
) {
    val isHome: Boolean get() = UrlKit.isHome(url)
    val displayHost: String get() = UrlKit.host(url)
    val displayTitle: String get() = UrlKit.titleOr(url, title)
}

class BrowserTab internal constructor(
    val id: Long,
    val webView: WebView,
    private val onChanged: () -> Unit,
) {
    private val _state = MutableStateFlow(TabState(id))
    val state: StateFlow<TabState> = _state.asStateFlow()

    internal fun update(transform: (TabState) -> TabState) {
        _state.update(transform)
        onChanged()
    }

    val current: TabState get() = _state.value
}

data class FullscreenView(
    val view: View,
    val callback: WebChromeClient.CustomViewCallback,
)

data class FileChooserRequest(
    val params: WebChromeClient.FileChooserParams,
    val callback: ValueCallback<Array<Uri>>,
)

/**
 * Owns the live WebViews and translates their callbacks into small immutable
 * tab states. WebViews deliberately stay out of the composable tree's
 * lifecycle: a tab survives screen changes and is only destroyed when closed.
 */
class BrowserEngine(
    private val context: Context,
    private val history: HistoryRepository,
    private val downloads: DownloadRepository,
    private val preferences: BrowserPreferences,
    private val scope: CoroutineScope,
) {
    private val tabs = LinkedHashMap<Long, BrowserTab>()
    private var nextId = 1L
    private var settings = BrowserSettings()
    private val defaultUserAgent = WebSettings.getDefaultUserAgent(context)

    private val _tabList = MutableStateFlow<List<BrowserTab>>(emptyList())
    val tabList: StateFlow<List<BrowserTab>> = _tabList.asStateFlow()

    private val _snapshots = MutableStateFlow<List<TabState>>(emptyList())
    val snapshots: StateFlow<List<TabState>> = _snapshots.asStateFlow()

    private val _activeTab = MutableStateFlow<BrowserTab?>(null)
    val activeTab: StateFlow<BrowserTab?> = _activeTab.asStateFlow()

    private val _fullscreen = MutableStateFlow<FullscreenView?>(null)
    val fullscreen: StateFlow<FullscreenView?> = _fullscreen.asStateFlow()

    private val _fileChooser = MutableStateFlow<FileChooserRequest?>(null)
    val fileChooser: StateFlow<FileChooserRequest?> = _fileChooser.asStateFlow()

    private val _messages = MutableSharedFlow<EngineMessage>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    private val _jsDialog = MutableStateFlow<JsDialog?>(null)
    val jsDialog: StateFlow<JsDialog?> = _jsDialog.asStateFlow()

    var pendingFileChooser: ValueCallback<Array<Uri>>? = null
        private set

    init {
        // The first tab is created by the ViewModel once saved tabs are known.
    }

    fun applySettings(newSettings: BrowserSettings, reload: Boolean = false) {
        val desktopChanged = settings.desktopMode != newSettings.desktopMode
        settings = newSettings
        tabs.values.forEach { tab ->
            applySettingsTo(tab)
        }
        if (reload && desktopChanged && _activeTab.value?.current?.isHome == false) {
            _activeTab.value?.webView?.reload()
        }
    }

    fun newTab(url: String? = null, select: Boolean = true): BrowserTab {
        val id = nextId++
        val webView = WebView(context)
        setupWebView(webView)
        val tab = BrowserTab(id, webView, ::publish)
        tab.update { it.copy(isDesktop = settings.desktopMode) }
        webView.webViewClient = MimoWebViewClient(tab)
        webView.webChromeClient = MimoChromeClient(tab)
        tabs[id] = tab
        if (select) {
            _activeTab.value = tab
        }
        publish()
        if (!url.isNullOrBlank()) {
            webView.loadUrl(url)
        } else {
            webView.loadUrl("about:blank")
        }
        return tab
    }

    fun restore(urls: List<String>, activeUrl: String? = null) {
        if (urls.isEmpty()) {
            if (tabs.isEmpty()) newTab()
            return
        }
        urls.take(8).forEachIndexed { index, url ->
            newTab(url, select = index == urls.lastIndex)
        }
        val activeIndex = activeUrl?.let { urls.take(8).indexOf(it) } ?: -1
        val active = tabs.values.elementAtOrNull(activeIndex)
        _activeTab.value = active ?: tabs.values.lastOrNull()
        publish()
    }

    fun selectTab(id: Long) {
        tabs[id]?.let {
            _activeTab.value = it
            publish()
        }
    }

    fun closeTab(id: Long) {
        val tab = tabs[id] ?: return
        val wasActive = _activeTab.value?.id == id
        val ids = tabs.keys.toList()
        val index = ids.indexOf(id)
        tabs.remove(id)
        tab.webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            clearCache(true)
            destroy()
        }
        if (tabs.isEmpty()) {
            _activeTab.value = null
            newTab()
        } else if (wasActive) {
            val next = tabs.keys.elementAtOrNull(index.coerceAtMost(tabs.size - 1))
                ?: tabs.keys.last()
            _activeTab.value = tabs[next]
        }
        publish()
    }

    fun closeAllTabs() {
        tabs.values.forEach { tab ->
            tab.webView.apply {
                stopLoading()
                loadUrl("about:blank")
                clearHistory()
                destroy()
            }
        }
        tabs.clear()
        _activeTab.value = null
        newTab()
        publish()
    }

    fun openUrl(url: String) {
        val tab = _activeTab.value
        if (tab == null) {
            newTab(url)
        } else {
            tab.webView.loadUrl(url)
        }
    }

    fun openFromIntent(url: String) {
        val tab = _activeTab.value
        if (tab != null && tab.current.isHome) {
            tab.webView.loadUrl(url)
        } else {
            newTab(url)
        }
    }

    fun goBack() {
        _activeTab.value?.webView?.let { if (it.canGoBack()) it.goBack() }
    }

    fun goForward() {
        _activeTab.value?.webView?.let { if (it.canGoForward()) it.goForward() }
    }

    fun reload() {
        _activeTab.value?.webView?.let {
            if (it.url == null) it.loadUrl("about:blank") else it.reload()
        }
    }

    fun stop() {
        _activeTab.value?.let { tab ->
            tab.webView.stopLoading()
            tab.update { it.copy(isLoading = false) }
        }
    }

    fun goHome() {
        _activeTab.value?.webView?.loadUrl("about:blank")
    }

    fun currentWebView(): WebView? = _activeTab.value?.webView

    fun currentState(): TabState? = _activeTab.value?.current

    fun provideFileChooserResult(uris: Array<Uri>?) {
        pendingFileChooser?.onReceiveValue(uris ?: emptyArray())
        pendingFileChooser = null
        _fileChooser.value = null
    }

    fun cancelFileChooser() {
        provideFileChooserResult(null)
    }

    fun dismissJsDialog() {
        when (val dialog = _jsDialog.value) {
            is JsDialog.Alert -> dialog.result.confirm()
            is JsDialog.Confirm -> dialog.result.cancel()
            is JsDialog.Prompt -> dialog.result.cancel()
            null -> Unit
        }
        _jsDialog.value = null
    }

    fun confirmJsDialog(text: String? = null) {
        when (val dialog = _jsDialog.value) {
            is JsDialog.Alert -> dialog.result.confirm()
            is JsDialog.Confirm -> dialog.result.confirm()
            is JsDialog.Prompt -> dialog.result.confirm(text.orEmpty())
            null -> Unit
        }
        _jsDialog.value = null
    }

    fun exitFullscreen() {
        _fullscreen.value?.callback?.onCustomViewHidden()
        _fullscreen.value = null
    }

    fun clearBrowsingData() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        WebViewDatabase.getInstance(context).apply {
            clearHttpAuthUsernamePassword()
            clearFormData()
        }
        tabs.values.forEach { it.webView.clearCache(true) }
    }

    fun destroy() {
        _fullscreen.value?.let { runCatching { it.callback.onCustomViewHidden() } }
        _fullscreen.value = null
        tabs.values.forEach { runCatching { it.webView.destroy() } }
        tabs.clear()
        _tabList.value = emptyList()
        _snapshots.value = emptyList()
        _activeTab.value = null
    }

    private fun publish() {
        _tabList.value = tabs.values.toList()
        _snapshots.value = tabs.values.map { it.current }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webView: WebView) {
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER
        webView.webViewClient = WebViewClient()
        webView.settings.apply {
            javaScriptEnabled = settings.javascriptEnabled
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            mediaPlaybackRequiresUserGesture = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = if (settings.desktopMode) DESKTOP_USER_AGENT else defaultUserAgent
            setGeolocationEnabled(false)
            textZoom = 100
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        webView.setDownloadListener(
            DownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                scope.launch {
                    val id = downloads.enqueue(
                        url = url,
                        userAgent = userAgent,
                        contentDisposition = contentDisposition,
                        mimeType = mimeType,
                        contentLength = contentLength,
                    )
                    _messages.tryEmit(
                        if (id != null) EngineMessage.DownloadStarted else EngineMessage.DownloadFailed,
                    )
                }
            },
        )
    }

    private fun applySettingsTo(tab: BrowserTab) {
        tab.webView.settings.apply {
            javaScriptEnabled = settings.javascriptEnabled
            userAgentString = if (settings.desktopMode) DESKTOP_USER_AGENT else defaultUserAgent
        }
        tab.update { it.copy(isDesktop = settings.desktopMode) }
        applyChromeBlur(tab)
    }

    fun setTopBlurVisible(visible: Boolean) {
        _activeTab.value?.webView?.evaluateJavascript(
            "if(window.__mimoTopBlur)window.__mimoTopBlur($visible);",
            null,
        )
    }

    private fun applyChromeBlur(tab: BrowserTab) {
        val enabled = settings.interfaceBlur && UrlKit.isHttp(tab.webView.url.orEmpty())
        tab.webView.evaluateJavascript(
            if (enabled) INSTALL_CHROME_BLUR_JS else REMOVE_CHROME_BLUR_JS,
        ) { result ->
            val applied = enabled && result?.contains("ok") == true
            tab.update { it.copy(chromeBlurred = applied) }
        }
    }

    private fun launchExternal(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase()
        if (scheme == null) return false
        val intent = when (scheme) {
            "http", "https" -> return false
            "intent" -> runCatching { Intent.parseUri(url, Intent.URI_INTENT_SCHEME) }.getOrNull()
            else -> Intent(Intent.ACTION_VIEW, uri)
        } ?: return false
        if (scheme != "intent") {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }

    private inner class MimoWebViewClient(private val tab: BrowserTab) : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest,
        ): Boolean {
            val url = request.url.toString()
            if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("about:")) {
                return false
            }
            return launchExternal(url)
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            tab.update {
                it.copy(
                    url = url.orEmpty(),
                    title = "",
                    favicon = null,
                    isLoading = true,
                    chromeBlurred = false,
                    progress = 0,
                    error = null,
                    isSecure = UrlKit.isSecure(url.orEmpty()),
                    canGoBack = view.canGoBack(),
                    canGoForward = view.canGoForward(),
                )
            }
        }

        override fun onPageFinished(view: WebView, url: String?) {
            val current = url ?: view.url.orEmpty()
            tab.update {
                it.copy(
                    url = current,
                    title = view.title.orEmpty().ifBlank { it.title },
                    isLoading = false,
                    progress = 100,
                    canGoBack = view.canGoBack(),
                    canGoForward = view.canGoForward(),
                    isSecure = UrlKit.isSecure(current),
                )
            }
            if (UrlKit.isHttp(current)) {
                val pageTitle = view.title.orEmpty()
                scope.launch(Dispatchers.IO) {
                    history.recordVisit(current, pageTitle)
                }
            }
            applyChromeBlur(tab)
        }

        override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
            tab.update {
                it.copy(
                    url = url ?: view.url.orEmpty(),
                    canGoBack = view.canGoBack(),
                    canGoForward = view.canGoForward(),
                    isSecure = UrlKit.isSecure(url.orEmpty()),
                )
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            if (!request.isForMainFrame) return
            tab.update {
                it.copy(
                    isLoading = false,
                    error = PageFailure(
                        url = request.url.toString(),
                        code = error.errorCode,
                        description = error.description?.toString().orEmpty(),
                    ),
                )
            }
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: android.net.http.SslError,
        ) {
            handler.cancel()
            tab.update {
                it.copy(
                    isLoading = false,
                    error = PageFailure(
                        url = error.url.orEmpty(),
                        code = -1,
                        description = "SSL",
                    ),
                )
            }
        }
    }

    private inner class MimoChromeClient(private val tab: BrowserTab) : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            tab.update {
                it.copy(
                    progress = newProgress,
                    isLoading = newProgress < 100,
                )
            }
        }

        override fun onReceivedTitle(view: WebView, title: String?) {
            tab.update { it.copy(title = title.orEmpty()) }
        }

        override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
            if (icon != null) tab.update { it.copy(favicon = icon) }
        }

        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: android.os.Message,
        ): Boolean {
            val newTab = newTab(null, select = true)
            val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
            transport.webView = newTab.webView
            resultMsg.sendToTarget()
            return true
        }

        override fun onCloseWindow(window: WebView) {
            tabs.values.firstOrNull { it.webView === window }?.let { closeTab(it.id) }
        }

        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            _fullscreen.value = FullscreenView(view, callback)
        }

        override fun onHideCustomView() {
            _fullscreen.value = null
        }

        override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams,
        ): Boolean {
            pendingFileChooser?.onReceiveValue(null)
            pendingFileChooser = filePathCallback
            _fileChooser.value = FileChooserRequest(fileChooserParams, filePathCallback)
            return true
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            request.deny()
        }

        override fun onGeolocationPermissionsShowPrompt(
            origin: String,
            callback: GeolocationPermissions.Callback,
        ) {
            callback.invoke(origin, false, false)
        }

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            _jsDialog.value = JsDialog.Alert(message, result)
            return true
        }

        override fun onJsConfirm(
            view: WebView,
            url: String,
            message: String,
            result: JsResult,
        ): Boolean {
            _jsDialog.value = JsDialog.Confirm(message, result)
            return true
        }

        override fun onJsPrompt(
            view: WebView,
            url: String,
            message: String,
            defaultValue: String?,
            result: JsPromptResult,
        ): Boolean {
            _jsDialog.value = JsDialog.Prompt(message, defaultValue.orEmpty(), result)
            return true
        }
    }

    companion object {
        private const val INSTALL_CHROME_BLUR_JS = """
(function(){
  if(window.__mimoTopBlur){
    var existing=document.getElementById('mimo-top-blur');
    if(existing) existing.style.display='block';
    return 'ok';
  }
  try {
    var dark=window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    var blurSupported=!!(window.CSS && CSS.supports && (CSS.supports('backdrop-filter','blur(2px)') || CSS.supports('-webkit-backdrop-filter','blur(2px)')));
    var base=dark
      ? (blurSupported?'rgba(18,21,19,0.16)':'rgba(18,21,19,0.66)')
      : (blurSupported?'rgba(250,245,237,0.11)':'rgba(250,245,237,0.60)');
    function strip(id,top,height,blur){
      var e=document.createElement('div'); e.id=id;
      var st=e.style;
      st.position='fixed'; st.left='0'; st.right='0'; st.height=height+'px';
      if(top){ st.top='0'; } else { st.bottom='0'; }
      st.pointerEvents='none'; st.zIndex='2147483647';
      st.background=base;
      st.backdropFilter='blur('+blur+'px) saturate(1.25)';
      st.webkitBackdropFilter='blur('+blur+'px) saturate(1.25)';
      (document.body||document.documentElement).appendChild(e);
      return e;
    }
    var topEl=strip('mimo-top-blur',true,110,48);
    strip('mimo-bottom-blur',false,112,36);
    window.__mimoTopBlur=function(v){ if(topEl) topEl.style.display=v?'block':'none'; };
    return 'ok';
  } catch(e) { return 'err'; }
})();
"""
        private const val REMOVE_CHROME_BLUR_JS = """
(function(){
  var s=document.getElementById('mimo-glass-style'); if(s) s.remove();
  ['mimo-top-blur','mimo-bottom-blur'].forEach(function(id){
    var e=document.getElementById(id); if(e) e.remove();
  });
  window.__mimoTopBlur=null;
})();
"""
        private const val DESKTOP_USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"
    }
}

sealed interface EngineMessage {
    data object DownloadStarted : EngineMessage
    data object DownloadFailed : EngineMessage
}

sealed interface JsDialog {
    val message: String

    data class Alert(override val message: String, val result: JsResult) : JsDialog
    data class Confirm(override val message: String, val result: JsResult) : JsDialog
    data class Prompt(
        override val message: String,
        val defaultValue: String,
        val result: JsPromptResult,
    ) : JsDialog
}

package app.mimo.browser.ui

import android.app.Activity
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.mimo.browser.AppContainer
import app.mimo.browser.MimoBrowserApp
import app.mimo.browser.R
import app.mimo.browser.preferences.DesignLanguage
import app.mimo.browser.preferences.ThemeMode
import app.mimo.browser.ui.browser.BrowserContent
import app.mimo.browser.ui.browser.BrowserOmnibox
import app.mimo.browser.ui.browser.BrowserTabsScreen
import app.mimo.browser.ui.browser.createChromeBlurEffect
import app.mimo.browser.ui.components.AnimatedMimoMark
import app.mimo.browser.ui.components.BrowserPanelMode
import app.mimo.browser.ui.components.BrowserSurfaceHost
import app.mimo.browser.ui.components.GlassBackdrop
import app.mimo.browser.ui.components.captureGlassBackdrop
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.library.BrowserLibraryScreen
import app.mimo.browser.ui.settings.BrowserSettingsScreen
import app.mimo.browser.ui.theme.MimoTheme
import app.mimo.browser.web.FullscreenView
import app.mimo.browser.web.JsDialog
import app.mimo.browser.web.UrlKit
import kotlinx.coroutines.launch

@Composable
fun MimoBrowserRoot(
    container: AppContainer,
    incomingUrl: String?,
    onIncomingUrlConsumed: () -> Unit,
) {
    val context = LocalContext.current
    val application = context.applicationContext as MimoBrowserApp
    val viewModel: BrowserViewModel = viewModel(
        factory = viewModelFactory {
            initializer { BrowserViewModel(application) }
        },
    )
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MimoTheme(
        darkTheme = dark,
        dynamicColor = settings.dynamicColor,
        hapticsEnabled = true,
        designLanguage = settings.designLanguage,
        interfaceBlur = settings.interfaceBlur,
    ) {
        BrowserRootContent(
            viewModel = viewModel,
            settings = settings,
            incomingUrl = incomingUrl,
            onIncomingUrlConsumed = onIncomingUrlConsumed,
        )
    }
}

@Composable
private fun BrowserRootContent(
    viewModel: BrowserViewModel,
    settings: app.mimo.browser.preferences.BrowserSettings,
    incomingUrl: String?,
    onIncomingUrlConsumed: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    val rootView = LocalView.current.rootView
    var glassBackdrop by remember { mutableStateOf<GlassBackdrop?>(null) }
    val downloadStartedLabel = stringResource(R.string.download_started)
    val downloadsLabel = stringResource(R.string.downloads)
    val downloadFailedLabel = stringResource(R.string.download_failed)
    val linkCopiedLabel = stringResource(R.string.link_copied)

    val ready by viewModel.ready.collectAsStateWithLifecycle()
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val libraryTab by viewModel.libraryTab.collectAsStateWithLifecycle()
    val libraryQuery by viewModel.libraryQuery.collectAsStateWithLifecycle()
    val omniboxOpen by viewModel.omniboxOpen.collectAsStateWithLifecycle()
    val tabsOpen by viewModel.tabsOpen.collectAsStateWithLifecycle()
    val menuOpen by viewModel.menuOpen.collectAsStateWithLifecycle()
    val omniboxQuery by viewModel.omniboxQuery.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeBrowserTab.collectAsStateWithLifecycle()
    val activeState by viewModel.activeState.collectAsStateWithLifecycle()
    val tabs by viewModel.snapshots.collectAsStateWithLifecycle()
    val bookmarked by viewModel.isBookmarked.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val downloads by viewModel.downloads.collectAsStateWithLifecycle()
    val bookmarkCount by viewModel.bookmarkCount.collectAsStateWithLifecycle()
    val historyCount by viewModel.historyCount.collectAsStateWithLifecycle()
    val downloadCount by viewModel.downloadCount.collectAsStateWithLifecycle()
    val fullscreen by viewModel.fullscreen.collectAsStateWithLifecycle()
    val fileChooser by viewModel.fileChooser.collectAsStateWithLifecycle()
    val jsDialog by viewModel.jsDialog.collectAsStateWithLifecycle()

    val dockVisible = destination == Destination.Browser && !omniboxOpen && !tabsOpen
    var addressBarBounds by remember { mutableStateOf<Rect?>(null) }
    var dockBounds by remember { mutableStateOf<Rect?>(null) }
    var holdPageBlur by remember { mutableStateOf(false) }
    LaunchedEffect(menuOpen, activeState?.isHome) {
        if (menuOpen) {
            holdPageBlur = true
        } else {
            kotlinx.coroutines.delay(260)
            holdPageBlur = false
        }
    }
    val pageBlurred = (menuOpen || holdPageBlur) && settings.interfaceBlur &&
        activeState?.isHome == false && activeState?.error == null &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val blurRadiusPx = with(LocalDensity.current) { 18.dp.toPx() }
    val density = LocalDensity.current.density
    val chromeBlur = settings.interfaceBlur &&
        activeState?.isHome == false && activeState?.error == null
    LaunchedEffect(
        pageBlurred,
        chromeBlur,
        activeTab?.id,
        addressBarBounds,
        dockBounds,
        density,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val webView = activeTab?.webView ?: return@LaunchedEffect
            if (webView.width <= 0 || webView.height <= 0) return@LaunchedEffect
            val effect = when {
                pageBlurred -> RenderEffect.createBlurEffect(
                    blurRadiusPx,
                    blurRadiusPx,
                    Shader.TileMode.CLAMP,
                )
                chromeBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                    createChromeBlurEffect(
                        width = webView.width,
                        height = webView.height,
                        density = density,
                        addressBar = addressBarBounds,
                        dock = dockBounds,
                    )
                else -> null
            }
            webView.setRenderEffect(effect)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                activeTab?.webView?.setRenderEffect(null)
            }
        }
    }

    LaunchedEffect(incomingUrl, ready) {
        if (incomingUrl != null && ready) {
            viewModel.handleIncomingUrl(incomingUrl)
            onIncomingUrlConsumed()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.engineMessages.collect { message ->
            when (message) {
                app.mimo.browser.web.EngineMessage.DownloadStarted -> {
                    val result = snackbarHost.showSnackbar(
                        message = downloadStartedLabel,
                        actionLabel = downloadsLabel,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.openLibrary(LibraryTab.DOWNLOADS)
                    }
                }
                app.mimo.browser.web.EngineMessage.DownloadFailed ->
                    snackbarHost.showSnackbar(downloadFailedLabel)
            }
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHost.showSnackbar(it) }
    }

    val chooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        viewModel.provideFileChooserResult(uris)
    }
    LaunchedEffect(fileChooser) {
        val request = fileChooser ?: return@LaunchedEffect
        val intent = runCatching { request.params.createIntent() }.getOrNull()
        if (intent != null) {
            runCatching { chooserLauncher.launch(intent) }
                .onFailure { viewModel.cancelFileChooser() }
        } else {
            viewModel.cancelFileChooser()
        }
    }

    BackHandler(
        enabled = fullscreen != null || jsDialog != null || omniboxOpen || tabsOpen || menuOpen ||
            destination != Destination.Browser || activeState?.isHome == false,
    ) {
        when {
            fullscreen != null -> viewModel.exitFullscreen()
            jsDialog != null -> viewModel.dismissJsDialog()
            omniboxOpen -> viewModel.closeOmnibox()
            tabsOpen -> viewModel.tabsOpen.value = false
            menuOpen -> viewModel.menuOpen.value = false
            destination != Destination.Browser -> viewModel.backToBrowser()
            activeState?.canGoBack == true -> viewModel.goBack()
            activeState != null -> viewModel.goHome()
        }
    }

    if (!ready) {
        MimoBackdrop {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedMimoMark(
                    Modifier.size(70.dp),
                    color = MaterialTheme.colorScheme.primary,
                    accent = MaterialTheme.colorScheme.secondary,
                )
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 118.dp)
                        .size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        BrowserContent(
            tab = activeTab,
            state = activeState,
            bookmarks = bookmarks,
            recentHistory = recentHistory,
            bookmarked = bookmarked,
            onOpenOmnibox = { viewModel.openOmnibox() },
            onOpenBookmark = { viewModel.openUrl(it.url) },
            onOpenHistory = { viewModel.openUrl(it.url) },
            onOpenLibrary = { viewModel.openLibrary(it) },
            onOpenSettings = { viewModel.openSettings() },
            onAddressBarBounds = { addressBarBounds = it },
            onReload = { viewModel.reload() },
            onStop = { viewModel.stop() },
            onBookmark = { viewModel.toggleBookmark() },
            onRetry = { viewModel.retry() },
            onGoHome = { viewModel.goHome() },
        )

        AnimatedVisibility(
            visible = dockVisible,
            enter = fadeIn(tween(180)) + scaleIn(
                initialScale = 0.92f,
                transformOrigin = TransformOrigin(0.5f, 1f),
                animationSpec = spring(
                    dampingRatio = 0.68f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) + slideInVertically(
                animationSpec = spring(
                    dampingRatio = 0.74f,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                initialOffsetY = { it / 3 },
            ),
            exit = fadeOut(tween(140)),
        ) {
            BrowserSurfaceHost(
                mode = if (menuOpen) BrowserPanelMode.MENU else BrowserPanelMode.DOCK,
                tabCount = tabs.size,
                bookmarkCount = bookmarkCount,
                historyCount = historyCount,
                downloadCount = downloadCount,
                canGoBack = activeState?.canGoBack == true,
                canGoForward = activeState?.canGoForward == true,
                desktopMode = settings.desktopMode,
                hasPage = activeState?.isHome == false,
                backdrop = glassBackdrop,
                onDockBounds = { dockBounds = it },
                pageBlurred = pageBlurred,
                pageGlass = settings.interfaceBlur &&
                    activeState?.isHome == false && activeState?.error == null,
                solidBackground = activeState?.error != null,
                onDismiss = { viewModel.menuOpen.value = false },
                onMenuClick = {
                    if (!menuOpen) {
                        glassBackdrop = if (activeState?.isHome == true) {
                            captureGlassBackdrop(rootView)
                        } else {
                            null
                        }
                        viewModel.menuOpen.value = true
                    }
                },
                onBack = { viewModel.goBack() },
                onForward = { viewModel.goForward() },
                onNewTab = { viewModel.newTab() },
                onTabs = { viewModel.tabsOpen.value = true },
                onBookmarks = { viewModel.openLibrary(LibraryTab.BOOKMARKS) },
                onHistory = { viewModel.openLibrary(LibraryTab.HISTORY) },
                onDownloads = { viewModel.openLibrary(LibraryTab.DOWNLOADS) },
                onShare = {
                    viewModel.menuOpen.value = false
                    activeState?.let { sharePage(context, it.url, it.displayTitle) }
                },
                onCopyLink = {
                    viewModel.menuOpen.value = false
                    val url = activeState?.url.orEmpty()
                    if (UrlKit.isHttp(url)) {
                        copyLink(context, url)
                        scope.launch { snackbarHost.showSnackbar(linkCopiedLabel) }
                    }
                },
                onToggleDesktop = {
                    viewModel.menuOpen.value = false
                    viewModel.setDesktopMode(!settings.desktopMode)
                },
                onOpenExternal = {
                    viewModel.menuOpen.value = false
                    activeState?.let { openExternal(context, it.url) }
                },
                onSettings = { viewModel.openSettings() },
            )
        }

        AnimatedVisibility(
            visible = destination == Destination.Library,
            enter = slideInVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 10 },
            ) + fadeIn(tween(180)),
            exit = slideOutVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                targetOffsetY = { it / 14 },
            ) + fadeOut(tween(160)),
        ) {
            BrowserLibraryScreen(
                tab = libraryTab,
                query = libraryQuery,
                bookmarks = bookmarks,
                history = history,
                downloads = downloads,
                onTabChange = { viewModel.selectLibraryTab(it) },
                onQueryChange = { viewModel.libraryQuery.value = it },
                onOpenUrl = { viewModel.openUrl(it) },
                onDeleteBookmark = { viewModel.deleteBookmark(it.id) },
                onDeleteHistory = { viewModel.deleteHistory(it.id) },
                onOpenDownload = { viewModel.openDownload(it) },
                onShareDownload = { viewModel.shareDownload(it) },
                onRemoveDownload = { viewModel.removeDownload(it) },
                onClearBookmarks = { viewModel.clearBookmarks() },
                onClearHistory = { viewModel.clearHistory() },
                onClearDownloads = { viewModel.clearDownloads() },
                onBack = { viewModel.backToBrowser() },
            )
        }

        AnimatedVisibility(
            visible = destination == Destination.Settings,
            enter = slideInVertically(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 10 },
            ) + fadeIn(tween(180)),
            exit = slideOutVertically(
                animationSpec = tween(220, easing = FastOutSlowInEasing),
                targetOffsetY = { it / 14 },
            ) + fadeOut(tween(160)),
        ) {
            BrowserSettingsScreen(
                settings = settings,
                historyCount = historyCount,
                bookmarkCount = bookmarkCount,
                downloadCount = downloadCount,
                onBack = { viewModel.backToBrowser() },
                onTheme = { viewModel.setTheme(it) },
                onDynamicColor = { viewModel.setDynamicColor(it) },
                onDesignLanguage = { viewModel.setDesignLanguage(it) },
                onSearchEngine = { viewModel.setSearchEngine(it) },
                onJavascript = { viewModel.setJavascript(it) },
                onDesktopMode = { viewModel.setDesktopMode(it) },
                onInterfaceBlur = { viewModel.setInterfaceBlur(it) },
                onClearHistory = { viewModel.clearHistory() },
                onClearBookmarks = { viewModel.clearBookmarks() },
                onClearDownloads = { viewModel.clearDownloads() },
                onClearWebData = { viewModel.clearWebData() },
            )
        }

        AnimatedVisibility(
            visible = tabsOpen,
            enter = fadeIn(tween(160)) + scaleIn(
                initialScale = 0.97f,
                animationSpec = tween(220, easing = FastOutSlowInEasing),
            ) + slideInVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 14 },
            ),
            exit = fadeOut(tween(130)) + slideOutVertically(
                animationSpec = tween(160, easing = FastOutSlowInEasing),
                targetOffsetY = { it / 18 },
            ),
        ) {
            BrowserTabsScreen(
                tabs = tabs,
                activeId = activeState?.id,
                onSelect = { viewModel.selectTab(it) },
                onClose = { viewModel.closeTab(it) },
                onNewTab = { viewModel.newTab() },
                onCloseAll = { viewModel.closeAllTabs() },
                onDismiss = { viewModel.tabsOpen.value = false },
            )
        }

        AnimatedVisibility(
            visible = omniboxOpen,
            enter = fadeIn(tween(160)) + slideInVertically(
                animationSpec = tween(240, easing = FastOutSlowInEasing),
                initialOffsetY = { -it / 12 },
            ),
            exit = fadeOut(tween(130)) + slideOutVertically(
                animationSpec = tween(180, easing = FastOutSlowInEasing),
                targetOffsetY = { -it / 16 },
            ),
        ) {
            BrowserOmnibox(
                query = omniboxQuery,
                suggestions = suggestions,
                onQueryChange = { viewModel.omniboxQuery.value = it },
                onSubmit = { viewModel.submitOmnibox() },
                onOpenSuggestion = { viewModel.openSuggestion(it) },
                onOpenUrl = { viewModel.openUrl(it) },
                onClose = { viewModel.closeOmnibox() },
            )
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = if (dockVisible) 96.dp else 20.dp),
        )

        jsDialog?.let { dialog ->
            JsDialogView(dialog = dialog, onConfirm = { viewModel.confirmJsDialog(it) }, onDismiss = { viewModel.dismissJsDialog() })
        }

        fullscreen?.let { full ->
            FullscreenOverlay(full = full, onClose = { viewModel.exitFullscreen() })
        }
    }
}

@Composable
private fun JsDialogView(
    dialog: JsDialog,
    onConfirm: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var promptValue by remember(dialog) {
        mutableStateOf((dialog as? JsDialog.Prompt)?.defaultValue.orEmpty())
    }
    val title = when (dialog) {
        is JsDialog.Alert -> "Сообщение"
        is JsDialog.Confirm -> "Подтверждение"
        is JsDialog.Prompt -> "Введите значение"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text(dialog.message)
                if (dialog is JsDialog.Prompt) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = promptValue,
                        onValueChange = { promptValue = it },
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(promptValue) }) { Text("ОК") }
        },
        dismissButton = if (dialog is JsDialog.Alert) null else {
            { TextButton(onClick = onDismiss) { Text("Отмена") } }
        },
    )
}

@Composable
private fun FullscreenOverlay(full: FullscreenView, onClose: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = context.findActivity()
    DisposableEffect(full) {
        val controller = activity?.let { WindowCompat.getInsetsController(it.window, view) }
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        key(full.view) {
            AndroidView(
                factory = { ctx -> FrameLayout(ctx) },
                modifier = Modifier.fillMaxSize(),
                update = { host ->
                    if (full.view.parent !== host) {
                        (full.view.parent as? ViewGroup)?.removeView(full.view)
                        host.removeAllViews()
                        host.addView(
                            full.view,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            ),
                        )
                    }
                },
            )
        }
        Surface(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(44.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    MimoIcons.Close,
                    contentDescription = "Закрыть",
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

private fun sharePage(context: Context, url: String, title: String) {
    if (!UrlKit.isHttp(url)) return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, url)
        putExtra(Intent.EXTRA_SUBJECT, title)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Поделиться"))
    }
}

private fun copyLink(context: Context, url: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("url", url))
}

private fun openExternal(context: Context, url: String) {
    if (!UrlKit.isHttp(url)) return
    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Открыть в браузере"))
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

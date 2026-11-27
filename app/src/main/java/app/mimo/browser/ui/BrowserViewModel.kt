package app.mimo.browser.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.mimo.browser.MimoBrowserApp
import app.mimo.browser.R
import app.mimo.browser.data.BookmarkEntity
import app.mimo.browser.data.DownloadEntity
import app.mimo.browser.data.HistoryEntity
import app.mimo.browser.preferences.BrowserSettings
import app.mimo.browser.preferences.DesignLanguage
import app.mimo.browser.preferences.SearchEngine
import app.mimo.browser.preferences.ThemeMode
import app.mimo.browser.web.BrowserEngine
import app.mimo.browser.web.BrowserTab
import app.mimo.browser.web.FileChooserRequest
import app.mimo.browser.web.FullscreenView
import app.mimo.browser.web.JsDialog
import app.mimo.browser.web.TabState
import app.mimo.browser.web.UrlKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LibraryTab { BOOKMARKS, HISTORY, DOWNLOADS }

sealed interface Destination {
    data object Browser : Destination
    data object Library : Destination
    data object Settings : Destination
}

enum class SuggestionKind { SEARCH, URL, BOOKMARK, HISTORY }

data class OmniboxSuggestion(
    val kind: SuggestionKind,
    val title: String,
    val subtitle: String,
    val url: String,
)

private data class SavedTabsState(val urls: List<String>, val activeUrl: String?)

@OptIn(FlowPreview::class)
class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as MimoBrowserApp).container
    private val app = application
    private val engine = BrowserEngine(
        context = application,
        history = container.history,
        downloads = container.downloads,
        preferences = container.preferences,
        scope = viewModelScope,
    )

    val ready = MutableStateFlow(false)
    val settings = MutableStateFlow(BrowserSettings())
    val destination = MutableStateFlow<Destination>(Destination.Browser)
    val libraryTab = MutableStateFlow(LibraryTab.BOOKMARKS)
    val omniboxOpen = MutableStateFlow(false)
    val tabsOpen = MutableStateFlow(false)
    val menuOpen = MutableStateFlow(false)
    val omniboxQuery = MutableStateFlow("")
    val libraryQuery = MutableStateFlow("")

    val activeBrowserTab: StateFlow<BrowserTab?> = engine.activeTab
    val tabList: StateFlow<List<BrowserTab>> = engine.tabList
    val snapshots: StateFlow<List<TabState>> = engine.snapshots
    val fullscreen: StateFlow<FullscreenView?> = engine.fullscreen
    val fileChooser: StateFlow<FileChooserRequest?> = engine.fileChooser
    val jsDialog: StateFlow<JsDialog?> = engine.jsDialog
    val engineMessages = engine.messages

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    val bookmarks: StateFlow<List<BookmarkEntity>> = container.bookmarks.all
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val history: StateFlow<List<HistoryEntity>> = container.history.all
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val downloads: StateFlow<List<DownloadEntity>> = container.downloads.all
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recentHistory: StateFlow<List<HistoryEntity>> = container.history.recent
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val bookmarkCount: StateFlow<Int> = container.bookmarks.count
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val historyCount: StateFlow<Int> = container.history.count
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val downloadCount: StateFlow<Int> = container.downloads.count
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val activeState: StateFlow<TabState?> = combine(engine.snapshots, engine.activeTab) { list, tab ->
        list.firstOrNull { it.id == tab?.id } ?: tab?.current
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isBookmarked: StateFlow<Boolean> = activeState
        .map { it?.url }
        .distinctUntilChanged()
        .flatMapLatest { url ->
            if (url.isNullOrBlank() || !UrlKit.isHttp(url)) flowOf(false)
            else container.bookmarks.isBookmarked(url)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val suggestions: StateFlow<List<OmniboxSuggestion>> = omniboxQuery
        .debounce(100)
        .mapLatest { raw -> buildSuggestions(raw) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(2_000), emptyList())

    init {
        viewModelScope.launch {
            val first = container.preferences.settings.first()
            settings.value = first
            engine.applySettings(first)
            val saved = container.preferences.savedTabs.first()
            engine.restore(saved.urls, saved.activeUrl)
            ready.value = true
            observeSettings()
            persistTabs()
        }
        viewModelScope.launch { container.downloads.refresh() }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            container.preferences.settings.collect { updated ->
                settings.value = updated
                engine.applySettings(updated, reload = true)
            }
        }
    }

    private fun persistTabs() {
        viewModelScope.launch {
            combine(engine.snapshots, engine.activeTab) { tabs, activeTab ->
                SavedTabsState(
                    urls = tabs.map { if (it.isHome) "" else it.url }.filter { it.isNotBlank() },
                    activeUrl = activeTab?.current?.url?.takeIf { it.isNotBlank() },
                )
            }
                .debounce(700)
                .distinctUntilChanged()
                .collect { state -> container.preferences.saveTabs(state.urls, state.activeUrl) }
        }
    }

    private fun buildSuggestions(raw: String): List<OmniboxSuggestion> {
        val query = raw.trim()
        val enginePreference = settings.value.searchEngine
        if (query.isEmpty()) {
            return (bookmarks.value.take(4).map {
                OmniboxSuggestion(SuggestionKind.BOOKMARK, it.title, UrlKit.host(it.url), it.url)
            } + recentHistory.value.take(6).map {
                OmniboxSuggestion(SuggestionKind.HISTORY, it.title, UrlKit.host(it.url), it.url)
            }).distinctBy { it.url }.take(8)
        }
        val result = buildList {
            val direct = UrlKit.toNavigationUrl(query, enginePreference)
            if (UrlKit.looksLikeHost(query) || query.startsWith("http://") || query.startsWith("https://")) {
                add(
                    OmniboxSuggestion(
                        SuggestionKind.URL,
                        query,
                        UrlKit.host(direct),
                        direct,
                    ),
                )
            } else {
                add(
                    OmniboxSuggestion(
                        SuggestionKind.SEARCH,
                        query,
                        enginePreference.displayName,
                        direct,
                    ),
                )
            }
            bookmarks.value
                .filter { it.title.contains(query, true) || it.url.contains(query, true) }
                .take(4)
                .forEach {
                    add(OmniboxSuggestion(SuggestionKind.BOOKMARK, it.title, UrlKit.host(it.url), it.url))
                }
            recentHistory.value
                .filter { it.title.contains(query, true) || it.url.contains(query, true) }
                .take(4)
                .forEach {
                    add(OmniboxSuggestion(SuggestionKind.HISTORY, it.title, UrlKit.host(it.url), it.url))
                }
        }
        return result.distinctBy { it.url }.take(8)
    }

    fun handleIncomingUrl(url: String) {
        if (!UrlKit.isHttp(url)) return
        readyToNavigate {
            engine.openFromIntent(url)
            destination.value = Destination.Browser
            closeOverlays()
        }
    }

    fun openSuggestion(suggestion: OmniboxSuggestion) {
        omniboxQuery.value = ""
        closeOmnibox()
        engine.openUrl(suggestion.url)
        destination.value = Destination.Browser
    }

    fun submitOmnibox() {
        val raw = omniboxQuery.value.trim()
        if (raw.isEmpty()) return
        omniboxQuery.value = ""
        closeOmnibox()
        engine.openUrl(UrlKit.toNavigationUrl(raw, settings.value.searchEngine))
        destination.value = Destination.Browser
    }

    fun openOmnibox(prefill: String? = null) {
        omniboxQuery.value = prefill.orEmpty()
        menuOpen.value = false
        tabsOpen.value = false
        omniboxOpen.value = true
    }

    fun closeOmnibox() {
        omniboxOpen.value = false
        omniboxQuery.value = ""
    }

    fun openLibrary(tab: LibraryTab) {
        libraryTab.value = tab
        libraryQuery.value = ""
        closeOverlays()
        destination.value = Destination.Library
        if (tab == LibraryTab.DOWNLOADS) refreshDownloads()
    }

    fun openSettings() {
        closeOverlays()
        destination.value = Destination.Settings
    }

    fun selectLibraryTab(tab: LibraryTab) {
        libraryTab.value = tab
        if (tab == LibraryTab.DOWNLOADS) refreshDownloads()
    }

    fun backToBrowser() {
        destination.value = Destination.Browser
        libraryQuery.value = ""
    }

    fun closeOverlays() {
        menuOpen.value = false
        tabsOpen.value = false
        omniboxOpen.value = false
    }

    fun newTab(url: String? = null) {
        engine.newTab(url)
        destination.value = Destination.Browser
        closeOverlays()
    }

    fun openUrl(url: String) {
        if (!UrlKit.isHttp(url)) return
        engine.openUrl(url)
        destination.value = Destination.Browser
        closeOverlays()
    }

    fun selectTab(id: Long) {
        engine.selectTab(id)
        tabsOpen.value = false
    }

    fun closeTab(id: Long) = engine.closeTab(id)

    fun closeAllTabs() {
        engine.closeAllTabs()
        tabsOpen.value = false
    }

    fun goBack() = engine.goBack()
    fun goForward() = engine.goForward()
    fun reload() = engine.reload()
    fun stop() = engine.stop()
    fun goHome() = engine.goHome()

    fun setTopBlurVisible(visible: Boolean) = engine.setTopBlurVisible(visible)

    fun retry() {
        val state = activeState.value ?: return
        state.error?.url?.let { engine.openUrl(it) }
    }

    fun toggleBookmark() {
        val state = activeState.value ?: return
        if (!UrlKit.isHttp(state.url)) return
        viewModelScope.launch {
            val added = container.bookmarks.toggle(state.url, state.displayTitle)
            _messages.tryEmit(app.getString(if (added) R.string.bookmark_added else R.string.bookmark_removed))
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { container.preferences.setTheme(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { container.preferences.setDynamicColor(enabled) }
    }

    fun setDesignLanguage(language: DesignLanguage) {
        viewModelScope.launch { container.preferences.setDesignLanguage(language) }
    }

    fun setSearchEngine(engine: SearchEngine) {
        viewModelScope.launch { container.preferences.setSearchEngine(engine) }
    }

    fun setJavascript(enabled: Boolean) {
        viewModelScope.launch { container.preferences.setJavascript(enabled) }
    }

    fun setDesktopMode(enabled: Boolean) {
        viewModelScope.launch { container.preferences.setDesktopMode(enabled) }
    }

    fun setInterfaceBlur(enabled: Boolean) {
        viewModelScope.launch { container.preferences.setInterfaceBlur(enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            container.history.clear()
            _messages.tryEmit(app.getString(R.string.history_cleared))
        }
    }

    fun clearBookmarks() {
        viewModelScope.launch {
            container.bookmarks.clear()
            _messages.tryEmit(app.getString(R.string.bookmarks_cleared))
        }
    }

    fun clearDownloads() {
        viewModelScope.launch {
            container.downloads.clear()
            _messages.tryEmit(app.getString(R.string.downloads_cleared))
        }
    }

    fun clearWebData() {
        viewModelScope.launch(Dispatchers.Main) {
            engine.clearBrowsingData()
            _messages.tryEmit(app.getString(R.string.web_data_cleared))
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch { container.history.delete(id) }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch { container.bookmarks.remove(id) }
    }

    fun removeDownload(download: DownloadEntity) {
        viewModelScope.launch { container.downloads.remove(download) }
    }

    fun openDownload(download: DownloadEntity) {
        val repo = container.downloads
        if (!repo.fileExists(download)) {
            _messages.tryEmit(app.getString(R.string.download_file_missing))
            return
        }
        repo.open(download)
    }

    fun shareDownload(download: DownloadEntity) {
        container.downloads.share(download)
    }

    fun refreshDownloads() {
        viewModelScope.launch { container.downloads.refresh() }
    }

    fun provideFileChooserResult(uris: Array<android.net.Uri>?) = engine.provideFileChooserResult(uris)

    fun cancelFileChooser() = engine.cancelFileChooser()

    fun confirmJsDialog(text: String? = null) = engine.confirmJsDialog(text)

    fun dismissJsDialog() = engine.dismissJsDialog()

    fun exitFullscreen() = engine.exitFullscreen()

    fun copyUrlToClipboard(): String? {
        val state = activeState.value ?: return null
        return state.url.takeIf { UrlKit.isHttp(it) }
    }

    private inline fun readyToNavigate(crossinline block: () -> Unit) {
        if (ready.value) block() else viewModelScope.launch {
            while (!ready.value) delay(30)
            block()
        }
    }

    override fun onCleared() {
        engine.destroy()
    }
}

val SearchEngine.displayName: String
    get() = when (this) {
        SearchEngine.GOOGLE -> "Google"
        SearchEngine.DUCKDUCKGO -> "DuckDuckGo"
        SearchEngine.YANDEX -> "Яндекс"
        SearchEngine.BING -> "Bing"
    }

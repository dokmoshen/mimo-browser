package app.mimo.browser.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URLEncoder

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class DesignLanguage { EXPRESSIVE, NOTHING }

enum class SearchEngine(val queryPrefix: String) {
    GOOGLE("https://www.google.com/search?q="),
    DUCKDUCKGO("https://html.duckduckgo.com/html/?q="),
    YANDEX("https://yandex.ru/search/?text="),
    BING("https://www.bing.com/search?q=");

    fun searchUrl(query: String): String =
        queryPrefix + URLEncoder.encode(query, "UTF-8").replace("+", "%20")
}

data class SavedTabs(
    val urls: List<String> = emptyList(),
    val activeUrl: String? = null,
)

data class BrowserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val designLanguage: DesignLanguage = DesignLanguage.EXPRESSIVE,
    val searchEngine: SearchEngine = SearchEngine.DUCKDUCKGO,
    val javascriptEnabled: Boolean = true,
    val desktopMode: Boolean = false,
    val interfaceBlur: Boolean = true,
)

private val Context.browserDataStore by preferencesDataStore(name = "browser_settings")

class BrowserPreferences(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val dynamic = booleanPreferencesKey("dynamic")
        val design = stringPreferencesKey("design")
        val engine = stringPreferencesKey("engine")
        val javascript = booleanPreferencesKey("javascript")
        val desktop = booleanPreferencesKey("desktop")
        val blur = booleanPreferencesKey("blur")
        val openTabs = stringPreferencesKey("open_tabs")
    }

    val settings: Flow<BrowserSettings> = context.browserDataStore.data.map { prefs ->
        BrowserSettings(
            themeMode = prefs[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[Keys.dynamic] ?: false,
            designLanguage = prefs[Keys.design]?.let {
                runCatching { DesignLanguage.valueOf(it) }.getOrNull()
            } ?: DesignLanguage.EXPRESSIVE,
            searchEngine = prefs[Keys.engine]?.let {
                runCatching { SearchEngine.valueOf(it) }.getOrNull()
            } ?: SearchEngine.DUCKDUCKGO,
            javascriptEnabled = prefs[Keys.javascript] ?: true,
            desktopMode = prefs[Keys.desktop] ?: false,
            interfaceBlur = prefs[Keys.blur] ?: true,
        )
    }

    val savedTabs: Flow<SavedTabs> = context.browserDataStore.data.map { prefs ->
        val lines = prefs[Keys.openTabs]
            ?.split('\n')
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val urls = lines.map { it.removePrefix("*") }.filter { it.startsWith("http") }
        val active = lines.firstOrNull { it.startsWith("*") }?.removePrefix("*")
        SavedTabs(urls = urls, activeUrl = active)
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.browserDataStore.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.dynamic] = enabled }
    }

    suspend fun setDesignLanguage(language: DesignLanguage) {
        context.browserDataStore.edit { it[Keys.design] = language.name }
    }

    suspend fun setSearchEngine(engine: SearchEngine) {
        context.browserDataStore.edit { it[Keys.engine] = engine.name }
    }

    suspend fun setJavascript(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.javascript] = enabled }
    }

    suspend fun setDesktopMode(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.desktop] = enabled }
    }

    suspend fun setInterfaceBlur(enabled: Boolean) {
        context.browserDataStore.edit { it[Keys.blur] = enabled }
    }

    suspend fun saveTabs(urls: List<String>, activeUrl: String?) {
        context.browserDataStore.edit { prefs ->
            val clean = urls.filter { it.startsWith("http") }.take(20)
            if (clean.isEmpty()) {
                prefs.remove(Keys.openTabs)
            } else {
                prefs[Keys.openTabs] = clean.joinToString("\n") { url ->
                    if (url == activeUrl) "*$url" else url
                }
            }
        }
    }
}

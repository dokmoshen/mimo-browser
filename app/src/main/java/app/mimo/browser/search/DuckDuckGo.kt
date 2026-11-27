package app.mimo.browser.search

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class DuckResult(
    val title: String,
    val url: String,
    val displayUrl: String,
    val snippet: String,
)

sealed interface DuckSearchState {
    val query: String
    val pageUrl: String

    data class Loading(
        override val query: String,
        override val pageUrl: String,
    ) : DuckSearchState

    data class Ready(
        override val query: String,
        override val pageUrl: String,
        val results: List<DuckResult>,
    ) : DuckSearchState

    data class Failed(
        override val query: String,
        override val pageUrl: String,
        val reason: String? = null,
    ) : DuckSearchState
}

/**
 * DuckDuckGo's no-JavaScript HTML endpoint, queried with a POST so it returns
 * the plain result markup. Only titles, addresses and snippets are kept; the
 * engine page itself is never shown.
 */
object DuckDuckGo {

    private const val HTML_SEARCH = "https://html.duckduckgo.com/html/"
    private const val DESKTOP_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36"

    fun searchUrl(query: String): String =
        HTML_SEARCH + "?q=" + encode(query)

    fun webSearchUrl(query: String): String =
        "https://duckduckgo.com/?q=" + encode(query)

    fun isSearchUrl(url: String): Boolean {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return false
        val host = uri.host?.lowercase()?.removePrefix("www.") ?: return false
        val path = uri.path.orEmpty()
        return host == "html.duckduckgo.com" &&
            (path == "/html/" || path == "/html")
    }

    fun queryOf(url: String): String? {
        if (!isSearchUrl(url)) return null
        return runCatching { Uri.parse(url).getQueryParameter("q") }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }

    suspend fun search(query: String): List<DuckResult> = withContext(Dispatchers.IO) {
        val connection = (URL(HTML_SEARCH).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 25_000
            doOutput = true
            instanceFollowRedirects = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            setRequestProperty("Accept", "text/html,application/xhtml+xml")
            setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.7")
            setRequestProperty("Referer", HTML_SEARCH)
            setRequestProperty("User-Agent", DESKTOP_UA)
        }
        try {
            val body = ("q=" + URLEncoder.encode(query, "UTF-8")).toByteArray(Charsets.UTF_8)
            connection.setFixedLengthStreamingMode(body.size)
            connection.outputStream.use { it.write(body) }
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("DuckDuckGo HTTP $code")
            val html = connection.inputStream
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            parse(html)
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(html: String): List<DuckResult> {
        val document = Jsoup.parse(html)
        if (document.selectFirst("#challenge-form, .anomaly-modal") != null) {
            throw IOException("DuckDuckGo requested a browser challenge")
        }
        val nodes = document.select("div.result")
        if (nodes.isEmpty()) {
            if (document.selectFirst(".no-results") != null) return emptyList()
            throw IOException("Unexpected DuckDuckGo page")
        }
        return nodes.mapNotNull { node ->
            if (node.hasClass("result--ad")) return@mapNotNull null
            val link = node.selectFirst("a.result__a") ?: return@mapNotNull null
            val title = link.text().trim()
            if (title.isEmpty()) return@mapNotNull null
            val url = normalizeUrl(link.attr("href")) ?: return@mapNotNull null
            DuckResult(
                title = title,
                url = url,
                displayUrl = node.selectFirst(".result__url")?.text()?.trim().orEmpty(),
                snippet = node.selectFirst(".result__snippet")?.text()?.trim().orEmpty(),
            )
        }.distinctBy { it.url }.take(30)
    }

    private fun normalizeUrl(raw: String): String? {
        var href = raw.trim()
        if (href.isEmpty()) return null
        if (href.startsWith("//")) href = "https:$href"
        if (href.startsWith("/")) href = "https://duckduckgo.com$href"
        val uri = runCatching { Uri.parse(href) }.getOrNull() ?: return null
        val redirect = uri.getQueryParameter("uddg")?.takeIf { it.isNotBlank() }
        val finalUrl = redirect ?: href
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) return null
        if (finalUrl.contains("duckduckgo.com/y.js")) return null
        return finalUrl
    }

    private fun encode(query: String): String =
        URLEncoder.encode(query, "UTF-8").replace("+", "%20")
}

package app.mimo.browser.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class LinkPreviewData(
    val title: String?,
    val description: String?,
)

/**
 * Small, best-effort metadata fetch for the long-press link sheet. It reads
 * only a limited amount of HTML and never executes anything from the page.
 */
object LinkPreviewLoader {

    private const val MAX_BYTES = 512 * 1024
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/140.0 Mobile Safari/537.36"

    suspend fun load(url: String): LinkPreviewData? = withContext(Dispatchers.IO) {
        if (!UrlKit.isHttp(url)) return@withContext null
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8_000
                readTimeout = 10_000
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,application/xhtml+xml")
            }
            try {
                val code = connection.responseCode
                if (code !in 200..299) return@runCatching null
                val type = connection.contentType.orEmpty().lowercase()
                if (type.isNotEmpty() && !type.contains("html")) return@runCatching null
                val html = connection.inputStream.use { input ->
                    val output = ByteArrayOutputStream()
                    val buffer = ByteArray(16 * 1024)
                    var total = 0
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        val allowed = (MAX_BYTES - total).coerceAtMost(read)
                        if (allowed > 0) {
                            output.write(buffer, 0, allowed)
                            total += allowed
                        }
                        if (total >= MAX_BYTES) break
                    }
                    output.toString(Charsets.UTF_8.name())
                }
                val document = Jsoup.parse(html)
                val title = (
                    document.selectFirst("meta[property=og:title]")?.attr("content")
                        ?: document.title()
                    ).trim().ifBlank { null }
                val description = (
                    document.selectFirst("meta[property=og:description]")?.attr("content")
                        ?: document.selectFirst("meta[name=description]")?.attr("content")
                    ).orEmpty().trim().ifBlank { null }
                LinkPreviewData(title = title, description = description)
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }
}

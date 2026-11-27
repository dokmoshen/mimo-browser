package app.mimo.browser.web

import android.net.Uri
import app.mimo.browser.preferences.SearchEngine

object UrlKit {

    fun toNavigationUrl(input: String, engine: SearchEngine): String {
        val text = input.trim()
        if (text.isEmpty()) return ""
        if (text.startsWith("http://") || text.startsWith("https://")) return text
        if (text.startsWith("about:") || text.startsWith("file:") || text.startsWith("content:")) {
            return text
        }
        if (text.contains("://")) return text
        if (looksLikeHost(text)) return "https://$text"
        return engine.searchUrl(text)
    }

    fun looksLikeHost(input: String): Boolean {
        if (input.isEmpty() || input.any { it.isWhitespace() }) return false
        val authority = input
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
        val host = authority.substringBefore(':')
        if (host.equals("localhost", ignoreCase = true)) return true
        if (Regex("^\\d{1,3}(\\.\\d{1,3}){3}$").matches(host)) return true
        if (!host.contains('.')) return false
        val tld = host.substringAfterLast('.')
        if (tld.length < 2) return false
        return Regex("^[\\p{L}\\p{N}-]+(\\.[\\p{L}\\p{N}-]+)+$").matches(host)
    }

    fun isHttp(url: String): Boolean =
        url.startsWith("http://") || url.startsWith("https://")

    fun isSecure(url: String): Boolean = url.startsWith("https://")

    fun isHome(url: String): Boolean =
        url.isBlank() || url == "about:blank" || url.startsWith("about:home")

    fun host(url: String): String = runCatching {
        Uri.parse(url).host?.removePrefix("www.")?.ifBlank { null } ?: ""
    }.getOrDefault("")

    fun displayUrl(url: String): String {
        if (isHome(url)) return ""
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return url
        val host = uri.host?.removePrefix("www.")
        if (host.isNullOrBlank()) return url
        val path = uri.encodedPath
            ?.takeIf { it != "/" }
            ?.let { runCatching { Uri.decode(it) }.getOrDefault(it) }
            ?: ""
        val query = uri.encodedQuery?.takeIf { it.isNotBlank() }?.let { "?$it" } ?: ""
        return host + path + query
    }

    fun titleOr(url: String, title: String): String =
        title.trim().ifBlank { host(url).ifBlank { url } }

    fun monogram(url: String): String {
        val host = host(url)
        val first = host.firstOrNull { it.isLetterOrDigit() } ?: '?'
        return first.uppercase()
    }

    fun paletteIndex(url: String): Int {
        val host = host(url)
        if (host.isEmpty()) return 0
        var hash = 0
        host.forEach { hash = hash * 31 + it.code }
        return (hash and 0x7fffffff) % 8
    }
}

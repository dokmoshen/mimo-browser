package app.mimo.browser.web

import app.mimo.browser.preferences.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlKitTest {

    @Test
    fun domainInputBecomesHttpsUrl() {
        assertEquals(
            "https://example.com/path",
            UrlKit.toNavigationUrl("example.com/path", SearchEngine.DUCKDUCKGO),
        )
    }

    @Test
    fun textInputBecomesSearch() {
        assertEquals(
            SearchEngine.DUCKDUCKGO.searchUrl("mimo browser"),
            UrlKit.toNavigationUrl("mimo browser", SearchEngine.DUCKDUCKGO),
        )
    }

    @Test
    fun schemeIsPreserved() {
        assertEquals(
            "http://example.com",
            UrlKit.toNavigationUrl("http://example.com", SearchEngine.GOOGLE),
        )
    }

    @Test
    fun detectsHosts() {
        assertTrue(UrlKit.looksLikeHost("example.com"))
        assertTrue(UrlKit.looksLikeHost("ru.wikipedia.org/wiki/Mimo"))
        assertTrue(UrlKit.looksLikeHost("192.168.1.1:8080"))
        assertTrue(UrlKit.looksLikeHost("localhost:8000"))
    }

    @Test
    fun rejectsQueries() {
        assertFalse(UrlKit.looksLikeHost("mimo browser"))
        assertFalse(UrlKit.looksLikeHost("hello"))
        assertFalse(UrlKit.looksLikeHost(""))
    }
}

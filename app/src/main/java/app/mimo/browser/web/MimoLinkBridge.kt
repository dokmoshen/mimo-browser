package app.mimo.browser.web

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import androidx.annotation.Keep

/** Receives link long-presses from the page's own touch events. */
@Keep
class MimoLinkBridge(
    private val onLongPress: (String) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @Keep
    @JavascriptInterface
    fun onLongPress(url: String) {
        val clean = url.trim()
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) return
        mainHandler.post { onLongPress(clean) }
    }
}

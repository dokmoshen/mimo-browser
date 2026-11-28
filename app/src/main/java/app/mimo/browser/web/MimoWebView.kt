package app.mimo.browser.web

import android.content.Context
import android.view.ContextMenu
import android.webkit.WebView

/**
 * WebView builds its own context menu for long-pressed links. Intercepting it
 * here lets Mimo show its own link preview instead of the platform menu.
 */
class MimoWebView(context: Context) : WebView(context) {

    var onLinkLongClick: ((String) -> Boolean)? = null

    override fun onCreateContextMenu(menu: ContextMenu) {
        val result = hitTestResult
        val url = result.extra
        if (
            result.type == HitTestResult.SRC_ANCHOR_TYPE &&
            !url.isNullOrBlank() &&
            onLinkLongClick?.invoke(url) == true
        ) {
            menu.clear()
            return
        }
        super.onCreateContextMenu(menu)
    }
}

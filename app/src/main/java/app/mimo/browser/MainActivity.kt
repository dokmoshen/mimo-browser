package app.mimo.browser

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.mimo.browser.ui.MimoBrowserRoot

class MainActivity : ComponentActivity() {

    private var incomingUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        super.onCreate(savedInstanceState)
        incomingUrl = intent.browserUrl()
        val container = (application as MimoBrowserApp).container
        setContent {
            MimoBrowserRoot(
                container = container,
                incomingUrl = incomingUrl,
                onIncomingUrlConsumed = { incomingUrl = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingUrl = intent.browserUrl()
    }

    private fun Intent.browserUrl(): String? {
        if (action != Intent.ACTION_VIEW) return null
        val data = data ?: return null
        val scheme = data.scheme?.lowercase() ?: return null
        return if (scheme == "http" || scheme == "https") data.toString() else null
    }
}

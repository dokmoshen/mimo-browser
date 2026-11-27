package app.mimo.browser.ui.browser

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.FrameLayout
import app.mimo.browser.data.BookmarkEntity
import app.mimo.browser.data.HistoryEntity
import app.mimo.browser.ui.LibraryTab
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserArt
import app.mimo.browser.ui.components.BrowserIllustration
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.web.BrowserTab
import app.mimo.browser.web.TabState
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserContent(
    tab: BrowserTab?,
    state: TabState?,
    bookmarks: List<BookmarkEntity>,
    recentHistory: List<HistoryEntity>,
    bookmarked: Boolean,
    onChromeVisibilityChanged: (Boolean) -> Unit = {},
    onOpenOmnibox: () -> Unit,
    onOpenBookmark: (BookmarkEntity) -> Unit,
    onOpenHistory: (HistoryEntity) -> Unit,
    onOpenLibrary: (LibraryTab) -> Unit,
    onOpenSettings: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onBookmark: () -> Unit,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
) {
    if (tab == null || state == null) {
        MimoBackdrop {
            Box(Modifier.fillMaxSize())
        }
        return
    }

    var chromeVisible by remember(tab.id) { mutableStateOf(true) }

    DisposableEffect(tab.id) {
        val webView = tab.webView
        var lastY = webView.scrollY
        webView.setOnScrollChangeListener { _, _, y, _, _ ->
            val dy = y - lastY
            if (y <= 8) {
                chromeVisible = true
            } else if (dy > 10 && y > 48) {
                chromeVisible = false
            } else if (dy < -10) {
                chromeVisible = true
            }
            lastY = y
        }
        onDispose {
            webView.setOnScrollChangeListener(null)
        }
    }
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) chromeVisible = true
    }
    LaunchedEffect(state.error) {
        if (state.error != null) chromeVisible = true
    }
    LaunchedEffect(chromeVisible) {
        onChromeVisibilityChanged(chromeVisible)
    }

    if (state.isHome) {
        MimoBackdrop {
            BrowserHomeScreen(
                bookmarks = bookmarks,
                recentHistory = recentHistory,
                onOpenOmnibox = onOpenOmnibox,
                onOpenBookmark = onOpenBookmark,
                onOpenHistory = onOpenHistory,
                onOpenLibrary = onOpenLibrary,
                onOpenSettings = onOpenSettings,
            )
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        androidx.compose.runtime.key(tab.id) {
            WebViewHost(tab = tab, modifier = Modifier.fillMaxSize())
        }

        if (!LocalInterfaceBlur.current || state.error != null) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.80f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.38f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }

        val addressVisible = chromeVisible && state.error == null
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            BrowserAddressBar(
                state = state,
                bookmarked = bookmarked,
                visible = addressVisible,
                onOpenOmnibox = onOpenOmnibox,
                onReload = onReload,
                onStop = onStop,
                onBookmark = onBookmark,
            )
        }

        AnimatedVisibility(
            visible = state.error != null,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(160)),
            modifier = Modifier.fillMaxSize(),
        ) {
            if (state.error != null) {
                PageErrorOverlay(
                    state = state,
                    onRetry = onRetry,
                    onGoHome = onGoHome,
                    onOpenOmnibox = onOpenOmnibox,
                )
            }
        }
    }
}

@Composable
private fun WebViewHost(tab: BrowserTab, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            FrameLayout(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        modifier = modifier,
        update = { host ->
            if (tab.webView.parent !== host) {
                (tab.webView.parent as? ViewGroup)?.removeView(tab.webView)
                host.removeAllViews()
                host.addView(
                    tab.webView,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    ),
                )
            }
        },
    )
    DisposableEffect(tab.id) {
        onDispose {
            (tab.webView.parent as? ViewGroup)?.removeView(tab.webView)
        }
    }
}

@Composable
private fun PageErrorOverlay(
    state: TabState,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    onOpenOmnibox: () -> Unit,
) {
    val haptics = LocalMimoHaptics.current
    MimoBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.7f))
            BrowserArt(
                kind = BrowserIllustration.GLOBE,
                modifier = Modifier.width(200.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Не удалось открыть страницу",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                UrlKit.host(state.error?.url.orEmpty()).ifBlank { state.error?.url.orEmpty() },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Проверьте соединение и попробуйте ещё раз.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(22.dp))
            Row {
                Surface(
                    onClick = {
                        haptics.press()
                        onRetry()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        "Повторить",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    onClick = {
                        haptics.tap()
                        onGoHome()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Text(
                        "Домой",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Surface(
                onClick = {
                    haptics.tap()
                    onOpenOmnibox()
                },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    "Открыть другой адрес",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun AddressPlaceholder() {
    Spacer(Modifier.height(0.dp))
}

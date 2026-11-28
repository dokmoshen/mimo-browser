package app.mimo.browser.ui.browser

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.delay
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.FrameLayout
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserArt
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.BrowserIllustration
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.search.DuckDuckGo
import app.mimo.browser.search.DuckSearchState
import app.mimo.browser.web.BrowserTab
import app.mimo.browser.web.TabState
import app.mimo.browser.web.LinkPreviewLoader
import app.mimo.browser.web.UrlKit

@Composable
fun BrowserContent(
    tab: BrowserTab?,
    state: TabState?,
    bookmarked: Boolean,
    omniboxOpen: Boolean,
    nativeSearch: DuckSearchState?,
    tabCount: Int,
    onOpenOmnibox: () -> Unit,
    onTabs: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenInNewTab: (String) -> Unit,
    onOpenSearchResult: (String) -> Unit,
    onEditSearchQuery: (String) -> Unit,
    onRetrySearch: () -> Unit,
    onOpenSearchFallback: () -> Unit,
    onGoBack: () -> Unit,
    onAddressBarBounds: (androidx.compose.ui.geometry.Rect?) -> Unit = {},
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
    val haptics = LocalMimoHaptics.current
    val density = LocalDensity.current
    val pull = remember(tab.id) { mutableFloatStateOf(0f) }
    val pullRefresh = remember(tab.id) { mutableStateOf(false) }
    val pullLimit = with(density) { 132.dp.toPx() }
    val pullThreshold = with(density) { 68.dp.toPx() }
    val longPressSlop = with(density) { 8.dp.toPx() }
    val reloadState = rememberUpdatedState(onReload)
    val loadingState = rememberUpdatedState(state.isLoading)
    var linkPreview by remember(tab.id) { mutableStateOf<LinkPreviewState?>(null) }

    DisposableEffect(tab.id) {
        val webView = tab.webView
        var downX = 0f
        var downY = 0f
        var tracking = false
        var armed = false
        val handler = Handler(Looper.getMainLooper())
        var longPressMoved = false
        val longPressRunnable = Runnable {
            if (longPressMoved) return@Runnable
            val result = webView.hitTestResult
            val url = result.extra.orEmpty()
            if (result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE && UrlKit.isHttp(url)) {
                if (linkPreview?.url != url) {
                    haptics.longPress()
                    linkPreview = LinkPreviewState(url = url)
                }
            }
        }
        val touchListener = android.view.View.OnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    longPressMoved = false
                    handler.removeCallbacks(longPressRunnable)
                    handler.postDelayed(longPressRunnable, 550)
                    tracking = webView.scrollY <= 0 && !pullRefresh.value && !loadingState.value
                    armed = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (abs(event.x - downX) > longPressSlop || abs(event.y - downY) > longPressSlop) {
                        longPressMoved = true
                        handler.removeCallbacks(longPressRunnable)
                    }
                    if (tracking) {
                        val dx = event.x - downX
                        val dy = event.y - downY
                        if (dy > 0f && dy > abs(dx) * 1.35f) {
                            val value = (dy * 0.55f).coerceAtMost(pullLimit)
                            pull.floatValue = value
                            if (!armed && value >= pullThreshold) {
                                armed = true
                                haptics.tick()
                            }
                        } else if (dy <= 0f) {
                            pull.floatValue = 0f
                            armed = false
                        }
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPressRunnable)
                    val triggered = tracking && pull.floatValue >= pullThreshold &&
                        event.actionMasked == MotionEvent.ACTION_UP
                    pull.floatValue = 0f
                    tracking = false
                    armed = false
                    if (triggered) {
                        haptics.confirm()
                        pullRefresh.value = true
                        reloadState.value()
                    }
                }
            }
            false
        }
        webView.setOnTouchListener(touchListener)
        onDispose {
            handler.removeCallbacks(longPressRunnable)
            webView.setOnTouchListener(null)
        }
    }

    LaunchedEffect(linkPreview?.url) {
        val current = linkPreview ?: return@LaunchedEffect
        val data = LinkPreviewLoader.load(current.url)
        if (linkPreview?.url == current.url) {
            linkPreview = current.copy(
                title = data?.title,
                description = data?.description,
                loading = false,
            )
        }
    }

    DisposableEffect(tab.id) {
        val webView = tab.webView
        fun showPreview(url: String): Boolean {
            if (!UrlKit.isHttp(url)) return false
            if (linkPreview?.url == url) return true
            haptics.longPress()
            linkPreview = LinkPreviewState(url = url)
            return true
        }
        val bridge = app.mimo.browser.web.MimoLinkBridge { url -> showPreview(url) }
        webView.addJavascriptInterface(bridge, "MimoLink")
        val mimoWebView = webView as? app.mimo.browser.web.MimoWebView
        mimoWebView?.onLinkLongClick = { url -> showPreview(url) }
        onDispose {
            mimoWebView?.onLinkLongClick = null
            webView.removeJavascriptInterface("MimoLink")
        }
    }

    LaunchedEffect(pullRefresh.value) {
        if (!pullRefresh.value) return@LaunchedEffect
        delay(700)
        val started = SystemClock.uptimeMillis()
        while (loadingState.value && SystemClock.uptimeMillis() - started < 5_000) {
            delay(120)
        }
        delay(240)
        pullRefresh.value = false
    }

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

    if (state.isHome) {
        MimoBackdrop {
            BrowserHomeScreen(
                onOpenOmnibox = onOpenOmnibox,
                omniboxOpen = omniboxOpen,
            )
        }
        return
    }

    val duckQuery = DuckDuckGo.queryOf(state.url)
    if (duckQuery != null) {
        val searchState = nativeSearch?.takeIf { it.pageUrl == state.url }
            ?: DuckSearchState.Loading(duckQuery, state.url)
        BrowserDuckResultsScreen(
            state = searchState,
            tabCount = tabCount,
            onBack = {
                if (state.canGoBack) onGoBack() else onGoHome()
            },
            onTabs = onTabs,
            onEditQuery = onEditSearchQuery,
            onOpenResult = onOpenSearchResult,
            onRetry = onRetrySearch,
            onOpenFallback = onOpenSearchFallback,
        )
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

        PullRefreshLayer(
            pull = pull,
            refreshing = pullRefresh,
            threshold = pullThreshold,
        )

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
                onBoundsChanged = onAddressBarBounds,
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

        linkPreview?.let { preview ->
            BrowserLinkPreviewSheet(
                state = preview,
                onDismiss = { linkPreview = null },
                onOpen = { url ->
                    linkPreview = null
                    onOpenUrl(url)
                },
                onOpenInNewTab = { url ->
                    linkPreview = null
                    onOpenInNewTab(url)
                },
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.PullRefreshLayer(
    pull: androidx.compose.runtime.MutableFloatState,
    refreshing: androidx.compose.runtime.MutableState<Boolean>,
    threshold: Float,
) {
    val raw = pull.floatValue
    val progress = (raw / threshold).coerceIn(0f, 1f)
    val visible = refreshing.value || raw > 1f
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(140)) + scaleIn(
            initialScale = 0.55f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessMedium,
            ),
        ),
        exit = fadeOut(tween(160)) + scaleOut(targetScale = 0.7f, animationSpec = tween(140)),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .statusBarsPadding()
            .padding(top = 72.dp),
    ) {
        val spin = if (refreshing.value) {
            val transition = rememberInfiniteTransition(label = "pullRefreshSpin")
            val value by transition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1100, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "pullRefreshRotation",
            )
            value
        } else {
            progress * 220f
        }
        val scale = if (refreshing.value) 1f else 0.74f + progress * 0.26f
        Surface(
            modifier = Modifier
                .size(50.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            ),
            shadowElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                val ringColor = MaterialTheme.colorScheme.primary
                val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = 2.4.dp.toPx()
                    val inset = stroke / 2f + 6.dp.toPx()
                    val arcSize = Size(
                        width = size.width - inset * 2f,
                        height = size.height - inset * 2f,
                    )
                    val topLeft = Offset(inset, inset)
                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = if (refreshing.value) -90f + spin else -90f,
                        sweepAngle = if (refreshing.value) 280f else 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                MimoLogo(
                    Modifier
                        .size(23.dp)
                        .graphicsLayer { rotationZ = spin },
                    color = MaterialTheme.colorScheme.primary,
                    accent = MaterialTheme.colorScheme.secondary,
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

package app.mimo.browser.ui.browser

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.ui.theme.LocalMimoDark
import app.mimo.browser.web.TabState

@Composable
fun BrowserAddressBar(
    state: TabState,
    bookmarked: Boolean,
    visible: Boolean,
    onBoundsChanged: (Rect?) -> Unit = {},
    onOpenOmnibox: () -> Unit,
    onReload: () -> Unit,
    onStop: () -> Unit,
    onBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically(
            animationSpec = tween(240, easing = FastOutSlowInEasing),
            initialOffsetY = { -it / 2 },
        ),
        exit = fadeOut(tween(140)) + slideOutVertically(
            animationSpec = tween(200, easing = FastOutSlowInEasing),
            targetOffsetY = { -it / 2 },
        ),
        modifier = modifier.onGloballyPositioned { coordinates ->
            onBoundsChanged(if (visible) coordinates.boundsInRoot() else null)
        },
    ) {
        val haptics = LocalMimoHaptics.current
        val interaction = remember { MutableInteractionSource() }
        val glass = LocalInterfaceBlur.current && state.error == null && !state.isHome
        val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
        val animatedProgress by animateFloatAsState(
            targetValue = (state.progress / 100f).coerceIn(0f, 1f),
            animationSpec = tween(320, easing = FastOutSlowInEasing),
            label = "pageProgress",
        )
        var showDone by remember { mutableStateOf(false) }
        var wasLoading by remember { mutableStateOf(false) }
        LaunchedEffect(state.isLoading) {
            if (state.isLoading) {
                wasLoading = true
                showDone = false
            } else if (wasLoading) {
                showDone = true
                delay(680)
                showDone = false
                wasLoading = false
            }
        }
        val starScale = remember { Animatable(1f) }
        val sparkle = remember { Animatable(0f) }
        var lastBookmarked by remember { mutableStateOf(bookmarked) }
        LaunchedEffect(bookmarked) {
            if (bookmarked == lastBookmarked) return@LaunchedEffect
            lastBookmarked = bookmarked
            starScale.snapTo(if (bookmarked) 1.55f else 0.72f)
            starScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.36f,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
            if (bookmarked) {
                sparkle.snapTo(0f)
                sparkle.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
                sparkle.snapTo(0f)
            }
        }
        Surface(
            onClick = {
                haptics.tap()
                onOpenOmnibox()
            },
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .pressScale(interaction, 0.965f),
            shape = CircleShape,
            color = if (glass) Color.Transparent else surfaceColor.copy(alpha = 1f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            ),
            shadowElevation = 0.dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                if (glass) {
                    val dark = LocalMimoDark.current
                    val strong = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    Box(
                        Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        surfaceColor.copy(
                                            alpha = if (strong) {
                                                if (dark) 0.90f else 0.94f
                                            } else if (dark) {
                                                0.34f
                                            } else {
                                                0.40f
                                            },
                                        ),
                                        surfaceColor.copy(
                                            alpha = if (strong) {
                                                if (dark) 0.94f else 0.97f
                                            } else if (dark) {
                                                0.46f
                                            } else {
                                                0.52f
                                            },
                                        ),
                                    ),
                                ),
                            ),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState = state.isSecure,
                        transitionSpec = {
                            (scaleIn(
                                initialScale = 0.55f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium,
                                ),
                            ) + fadeIn(tween(140))).togetherWith(
                                scaleOut(targetScale = 0.55f, animationSpec = tween(110)) +
                                    fadeOut(tween(110)),
                            )
                        },
                        label = "securityIcon",
                    ) { secure ->
                        Icon(
                            if (secure) MimoIcons.Lock else BrowserIcons.Globe,
                            contentDescription = if (secure) "Защищённое соединение" else "Обычное соединение",
                            tint = if (secure) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    AnimatedContent(
                        targetState = state.displayUrl(urlOnlyHost = true),
                        transitionSpec = {
                            (slideInVertically(
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                initialOffsetY = { it / 2 },
                            ) + fadeIn(tween(160))).togetherWith(
                                slideOutVertically(
                                    animationSpec = tween(130, easing = FastOutSlowInEasing),
                                    targetOffsetY = { -it / 2 },
                                ) + fadeOut(tween(120)),
                            )
                        },
                        modifier = Modifier.weight(1f),
                        label = "urlText",
                    ) { url ->
                        Text(
                            text = url,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (state.isLoading) {
                        IconButton(
                            onClick = {
                                haptics.tap()
                                onStop()
                            },
                            modifier = Modifier.size(38.dp),
                        ) {
                            CircularProgressIndicator(
                                progress = { animatedProgress.coerceAtLeast(0.04f) },
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            )
                        }
                    } else {
                        AnimatedContent(
                            targetState = showDone,
                            transitionSpec = {
                                (scaleIn(
                                    initialScale = 0.45f,
                                    animationSpec = spring(
                                        dampingRatio = 0.34f,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                ) + fadeIn(tween(120))).togetherWith(
                                    scaleOut(targetScale = 0.7f, animationSpec = tween(90)) +
                                        fadeOut(tween(90)),
                                )
                            },
                            label = "pageLoadDone",
                        ) { done ->
                            if (done) {
                                val doneScale = remember { Animatable(0.4f) }
                                LaunchedEffect(Unit) {
                                    doneScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = 0.35f,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                }
                                Box(
                                    modifier = Modifier.size(38.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        MimoIcons.Check,
                                        contentDescription = "Загружено",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(19.dp)
                                            .graphicsLayer {
                                                scaleX = doneScale.value
                                                scaleY = doneScale.value
                                            },
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        haptics.tap()
                                        onReload()
                                    },
                                    modifier = Modifier.size(38.dp),
                                ) {
                                    Icon(
                                        BrowserIcons.Refresh,
                                        contentDescription = "Обновить",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp),
                                    )
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = {
                            haptics.press()
                            onBookmark()
                        },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val sparkColor = MaterialTheme.colorScheme.secondary
                            if (sparkle.value > 0f) {
                                Canvas(Modifier.size(32.dp)) {
                                    val progress = sparkle.value
                                    drawCircle(
                                        color = sparkColor.copy(
                                            alpha = ((1f - progress) * 0.5f).coerceIn(0f, 1f),
                                        ),
                                        radius = size.minDimension * (0.22f + progress * 0.28f),
                                        style = Stroke(
                                            width = 1.4.dp.toPx(),
                                            cap = StrokeCap.Round,
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                    )
                                }
                            }
                            Icon(
                                if (bookmarked) BrowserIcons.StarFilled else BrowserIcons.Star,
                                contentDescription = "Закладка",
                                tint = if (bookmarked) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer {
                                        scaleX = starScale.value
                                        scaleY = starScale.value
                                    },
                            )
                        }
                    }
                }
                if (state.isLoading) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.dp)
                            .padding(horizontal = 22.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = androidx.compose.ui.graphics.Color.Transparent,
                    )
                }
            }
        }
    }
}

private fun TabState.displayUrl(urlOnlyHost: Boolean): String {
    val full = app.mimo.browser.web.UrlKit.displayUrl(url)
    return if (urlOnlyHost) {
        val host = app.mimo.browser.web.UrlKit.host(url)
        val rest = full.removePrefix(host)
        val tail = rest.take(70)
        buildString {
            append(host)
            append(tail)
        }.ifBlank { full }
    } else {
        full
    }
}

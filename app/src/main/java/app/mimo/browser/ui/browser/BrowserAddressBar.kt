package app.mimo.browser.ui.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.web.TabState

@Composable
fun BrowserAddressBar(
    state: TabState,
    bookmarked: Boolean,
    visible: Boolean,
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
        modifier = modifier,
    ) {
        val haptics = LocalMimoHaptics.current
        val interaction = remember { MutableInteractionSource() }
        val glass = LocalInterfaceBlur.current && state.chromeBlurred
        val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
        Surface(
            onClick = {
                haptics.tap()
                onOpenOmnibox()
            },
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .pressScale(interaction, 0.985f),
            shape = CircleShape,
            color = if (glass) surfaceColor.copy(alpha = 0.30f) else surfaceColor.copy(alpha = 1f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            ),
            shadowElevation = 0.dp,
        ) {
            Box(Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (state.isSecure) MimoIcons.Lock else BrowserIcons.Globe,
                        contentDescription = if (state.isSecure) "Защищённое соединение" else "Обычное соединение",
                        tint = if (state.isSecure) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = state.displayUrl(urlOnlyHost = true),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.isLoading) {
                        IconButton(
                            onClick = {
                                haptics.tap()
                                onStop()
                            },
                            modifier = Modifier.size(38.dp),
                        ) {
                            CircularProgressIndicator(
                                progress = { (state.progress.coerceAtLeast(4)) / 100f },
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
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
                    IconButton(
                        onClick = {
                            haptics.press()
                            onBookmark()
                        },
                        modifier = Modifier.size(38.dp),
                    ) {
                        Icon(
                            if (bookmarked) BrowserIcons.StarFilled else BrowserIcons.Star,
                            contentDescription = "Закладка",
                            tint = if (bookmarked) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (state.isLoading) {
                    LinearProgressIndicator(
                        progress = { state.progress.coerceIn(0, 100) / 100f },
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

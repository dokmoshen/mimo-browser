package app.mimo.browser.ui.browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mimo.browser.ui.components.BrowserIcons
import app.mimo.browser.ui.components.MimoBackdrop
import app.mimo.browser.ui.components.MimoIcons
import app.mimo.browser.ui.components.MimoLogo
import app.mimo.browser.ui.components.pressScale
import app.mimo.browser.ui.components.staggeredReveal
import app.mimo.browser.ui.theme.Unbounded
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun BrowserHomeScreen(
    onOpenOmnibox: () -> Unit,
    omniboxOpen: Boolean,
    tabCount: Int,
    isPrivate: Boolean,
    onTabs: () -> Unit,
    onNewPrivateTab: () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val flight = remember { Animatable(0f) }
    var flightJob by remember { mutableStateOf<Job?>(null) }
    var searchBounds by remember { mutableStateOf<Rect?>(null) }
    val statusBarTop = WindowInsets.statusBars.getTop(density).toFloat()

    fun flyToTop() {
        if (flightJob != null) return
        flightJob = scope.launch {
            flight.snapTo(0f)
            flight.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 380,
                    easing = FastOutSlowInEasing,
                ),
            )
            onOpenOmnibox()
        }
    }

    LaunchedEffect(omniboxOpen) {
        if (!omniboxOpen && flight.value > 0f) {
            flightJob?.cancel()
            flightJob = null
            flight.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 320,
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    MimoBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-36).dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val brandFade = { (1f - flight.value * 1.8f).coerceIn(0f, 1f) }
            if (isPrivate) {
                Surface(
                    modifier = Modifier
                        .staggeredReveal(index = 0, offsetY = 16.dp)
                        .graphicsLayer { alpha = brandFade() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            MimoIcons.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "Инкогнито · история не сохраняется",
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
            MimoLogo(
                modifier = Modifier
                    .size(76.dp)
                    .staggeredReveal(
                        index = 0,
                        offsetY = 22.dp,
                        initialScale = 0.9f,
                    )
                    .graphicsLayer { alpha = brandFade() },
                color = MaterialTheme.colorScheme.primary,
                accent = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .staggeredReveal(
                        index = 1,
                        offsetY = 16.dp,
                    )
                    .graphicsLayer { alpha = brandFade() },
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    "mimo",
                    fontFamily = Unbounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "browser",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(Modifier.height(36.dp))
            Box(
                Modifier
                    .staggeredReveal(
                        index = 2,
                        offsetY = 24.dp,
                    )
                    .onGloballyPositioned { searchBounds = it.boundsInRoot() }
                    .graphicsLayer {
                        val progress = flight.value
                        val bounds = searchBounds
                        if (bounds != null && bounds.width > 0f && bounds.height > 0f) {
                            val targetWidth = bounds.width - 24.dp.toPx()
                            val targetCenterX = bounds.center.x + 20.dp.toPx()
                            val targetCenterY = statusBarTop + 36.dp.toPx()
                            translationX = (targetCenterX - bounds.center.x) * progress
                            translationY = (targetCenterY - bounds.center.y) * progress
                            scaleX = 1f + (targetWidth / bounds.width - 1f) * progress
                            scaleY = 1f + (56.dp.toPx() / bounds.height - 1f) * progress
                        }
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    },
            ) {
                HomeSearchBar(onClick = ::flyToTop)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 10.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = onNewPrivateTab,
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.tertiary,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                shadowElevation = 2.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        MimoIcons.VisibilityOff,
                        contentDescription = "Новая вкладка инкогнито",
                        modifier = Modifier.size(21.dp),
                    )
                }
            }
            Surface(
                onClick = onTabs,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier
                        .height(46.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        BrowserIcons.Tabs,
                        contentDescription = "Вкладки",
                        modifier = Modifier.size(21.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        if (tabCount > 99) "99" else tabCount.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSearchBar(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .pressScale(interaction, 0.965f),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
        ),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        MimoIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Text(
                "Поиск или адрес",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        BrowserIcons.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

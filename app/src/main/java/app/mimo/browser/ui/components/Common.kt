package app.mimo.browser.ui.components

import android.animation.ValueAnimator
import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import app.mimo.browser.preferences.DesignLanguage
import app.mimo.browser.ui.LocalMimoHaptics
import app.mimo.browser.ui.theme.LocalAmbientTint
import app.mimo.browser.ui.theme.LocalDesignLanguage
import app.mimo.browser.ui.theme.LocalInterfaceBlur
import app.mimo.browser.ui.theme.LocalMimoDark
import app.mimo.browser.ui.theme.Unbounded
import app.mimo.browser.web.UrlKit

@Composable
fun backdropTopColor(): Color =
    if (LocalMimoDark.current) Color(0xFF161A17) else Color(0xFFFDF8F0)

@Composable
fun backdropBottomColor(): Color =
    if (LocalMimoDark.current) Color(0xFF0E110F) else Color(0xFFF4EEE3)

@Composable
fun MimoBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = LocalMimoDark.current
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignLanguage.current
    val blur = LocalInterfaceBlur.current
    val ambient = LocalAmbientTint.current
    val ambientLight = ambient?.luminance() ?: 0.5f
    val top = if (design == DesignLanguage.NOTHING) scheme.background else backdropTopColor()
    val bottom = if (design == DesignLanguage.NOTHING) scheme.background else backdropBottomColor()
    val glowPrimary = scheme.primary.copy(alpha = if (dark) 0.16f else 0.10f)
    val glowSecondary = scheme.secondary.copy(alpha = if (dark) 0.12f else 0.08f)
    val glowTertiary = scheme.tertiary.copy(alpha = if (dark) 0.12f else 0.07f)
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.verticalGradient(listOf(top, bottom)))
            if (design == DesignLanguage.NOTHING) {
                val step = 26.dp.toPx()
                val dot = scheme.onBackground.copy(
                    alpha = if (dark) 0.085f + 0.035f * ambientLight
                    else 0.055f + 0.040f * (1f - ambientLight),
                )
                var x = step / 2f
                while (x < size.width) {
                    var y = step / 2f
                    while (y < size.height) {
                        drawCircle(dot, radius = 1.1.dp.toPx(), center = Offset(x, y))
                        y += step
                    }
                    x += step
                }
            } else {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowPrimary, Color.Transparent),
                        center = Offset(size.width * 0.08f, size.height * 0.04f),
                        radius = size.width * 0.95f,
                    ),
                    radius = size.width * 0.95f,
                    center = Offset(size.width * 0.08f, size.height * 0.04f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowSecondary, Color.Transparent),
                        center = Offset(size.width * 0.96f, size.height * 0.18f),
                        radius = size.width * 0.75f,
                    ),
                    radius = size.width * 0.75f,
                    center = Offset(size.width * 0.96f, size.height * 0.18f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowTertiary, Color.Transparent),
                        center = Offset(size.width * 0.16f, size.height * 0.92f),
                        radius = size.width * 0.85f,
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.16f, size.height * 0.92f),
                )
            }
        }
        if (blur) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .blur(42.dp),
            ) {
                val radius = size.width * 0.9f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scheme.primary.copy(alpha = if (dark) 0.22f else 0.14f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.12f),
                        radius = radius,
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.12f, size.height * 0.12f),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            scheme.secondary.copy(alpha = if (dark) 0.16f else 0.10f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.92f, size.height * 0.78f),
                        radius = radius,
                    ),
                    radius = radius,
                    center = Offset(size.width * 0.92f, size.height * 0.78f),
                )
            }
        }
        PaperGrain(Modifier.fillMaxSize())
        content()
    }
}

@Composable
fun MimoLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    accent: Color = MaterialTheme.colorScheme.secondary,
) {
    Canvas(modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = size.minDimension * 0.09f
        val long = size.minDimension * 0.34f
        val diagonal = size.minDimension * 0.24f
        drawLine(color, Offset(center.x, center.y - long), Offset(center.x, center.y + long), stroke, StrokeCap.Round)
        drawLine(color, Offset(center.x - long, center.y), Offset(center.x + long, center.y), stroke, StrokeCap.Round)
        drawLine(
            color,
            Offset(center.x - diagonal, center.y - diagonal),
            Offset(center.x + diagonal, center.y + diagonal),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(center.x + diagonal, center.y - diagonal),
            Offset(center.x - diagonal, center.y + diagonal),
            stroke,
            StrokeCap.Round,
        )
        drawCircle(accent, radius = size.minDimension * 0.105f, center = center)
    }
}

@Composable
fun AnimatedMimoMark(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    accent: Color = MaterialTheme.colorScheme.secondary,
    animate: Boolean = true,
    breatheEnabled: Boolean = true,
) {
    val draw = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(animate) {
        if (animate) {
            draw.snapTo(0f)
            draw.animateTo(1f, tween(760, easing = FastOutSlowInEasing))
        } else {
            draw.snapTo(1f)
        }
    }
    val breathe = if (breatheEnabled) {
        val transition = rememberInfiniteTransition(label = "mimoBreathe")
        val value by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.035f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "mimoBreatheScale",
        )
        value
    } else {
        1f
    }
    Canvas(
        modifier.graphicsLayer {
            scaleX = breathe
            scaleY = breathe
        },
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = size.minDimension * 0.09f
        val long = size.minDimension * 0.34f
        val diagonal = size.minDimension * 0.24f
        val progress = draw.value

        fun strokeLine(from: Offset, to: Offset, start: Float) {
            val local = ((progress - start) / 0.55f).coerceIn(0f, 1f)
            if (local <= 0f) return
            drawLine(
                color = color,
                start = from,
                end = from + (to - from) * local,
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }

        strokeLine(Offset(center.x, center.y - long), Offset(center.x, center.y + long), 0f)
        strokeLine(Offset(center.x - long, center.y), Offset(center.x + long, center.y), 0.12f)
        strokeLine(
            Offset(center.x - diagonal, center.y - diagonal),
            Offset(center.x + diagonal, center.y + diagonal),
            0.24f,
        )
        strokeLine(
            Offset(center.x + diagonal, center.y - diagonal),
            Offset(center.x - diagonal, center.y + diagonal),
            0.36f,
        )
        val dotProgress = ((progress - 0.62f) / 0.38f).coerceIn(0f, 1f)
        if (dotProgress > 0f) {
            drawCircle(
                accent,
                radius = size.minDimension * 0.105f * dotProgress,
                center = center,
            )
        }
    }
}

@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.97f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * A one-shot entrance used for staggered paper-like reveals. It only changes
 * graphics (alpha, offset, scale, tilt), so lists never reflow while it runs.
 */
@Composable
fun Modifier.staggeredReveal(
    index: Int,
    baseDelayMs: Int = 0,
    stepMs: Int = 35,
    offsetY: Dp = 16.dp,
    initialScale: Float = 0.985f,
    startRotation: Float = 0f,
    maxStaggerIndex: Int = 8,
): Modifier {
    val progress = remember { Animatable(0f) }
    val animatorsEnabled = remember { ValueAnimator.areAnimatorsEnabled() }
    LaunchedEffect(Unit) {
        if (!animatorsEnabled) {
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        delay((baseDelayMs + index.coerceAtMost(maxStaggerIndex) * stepMs).toLong())
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessMediumLow,
            ),
        )
    }
    return graphicsLayer {
        val value = progress.value
        alpha = value.coerceIn(0f, 1f)
        translationY = (1f - value) * offsetY.toPx()
        val scale = initialScale + (1f - initialScale) * value
        scaleX = scale
        scaleY = scale
        rotationZ = startRotation * (1f - value)
    }
}

@Composable
fun CircleActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    size: Int = 48,
) {
    val haptics = LocalMimoHaptics.current
    Surface(
        onClick = {
            haptics.tap()
            onClick()
        },
        modifier = modifier.size(size.dp),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 1.dp,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, modifier = Modifier.size((size * 0.46f).dp))
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable
fun MimoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    val haptics = LocalMimoHaptics.current
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "chipContainer",
    )
    val content by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chipContent",
    )
    Surface(
        onClick = {
            haptics.toggle(!selected)
            onClick()
        },
        modifier = modifier,
        shape = CircleShape,
        color = container,
        border = if (selected) null else BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
        }
    }
}

@Composable
fun BrowserEmptyState(
    title: String,
    body: String,
    kind: BrowserIllustration,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrowserArt(kind = kind, modifier = Modifier.width(210.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            val haptics = LocalMimoHaptics.current
            Surface(
                onClick = {
                    haptics.press()
                    onAction()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Text(
                    actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        }
    }
}

private val LightBadges = listOf(
    Color(0xFFDDE9D2) to Color(0xFF2C3A27),
    Color(0xFFF7DCDA) to Color(0xFF43292A),
    Color(0xFFCFEAE3) to Color(0xFF1F3A34),
    Color(0xFFF6E9B8) to Color(0xFF3E381E),
    Color(0xFFD7E7F5) to Color(0xFF233345),
    Color(0xFFFBE1CC) to Color(0xFF483120),
    Color(0xFFE1E1F7) to Color(0xFF302E50),
    Color(0xFFEFDCF2) to Color(0xFF3C2D41),
)

private val DarkBadges = listOf(
    Color(0xFF2C3A27) to Color(0xFFDDE9D2),
    Color(0xFF43292A) to Color(0xFFF7DCDA),
    Color(0xFF1F3A34) to Color(0xFFCFEAE3),
    Color(0xFF3E381E) to Color(0xFFF6E9B8),
    Color(0xFF233345) to Color(0xFFD7E7F5),
    Color(0xFF483120) to Color(0xFFFBE1CC),
    Color(0xFF302E50) to Color(0xFFE1E1F7),
    Color(0xFF3C2D41) to Color(0xFFEFDCF2),
)

@Composable
fun SiteBadge(
    url: String,
    modifier: Modifier = Modifier,
    favicon: Bitmap? = null,
    size: Dp = 40.dp,
) {
    val dark = LocalMimoDark.current
    val design = LocalDesignLanguage.current
    val palette = if (dark) DarkBadges else LightBadges
    val pair = palette[UrlKit.paletteIndex(url).mod(palette.size)]
    val shape = if (design == DesignLanguage.NOTHING) {
        RoundedCornerShape(6.dp)
    } else {
        RoundedCornerShape(size * 0.34f)
    }
    Surface(
        modifier = modifier.size(size),
        shape = shape,
        color = if (design == DesignLanguage.NOTHING) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            pair.first
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (favicon != null) {
                Image(
                    bitmap = favicon.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (size > 34.dp) 8.dp else 6.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Text(
                    text = UrlKit.monogram(url),
                    fontFamily = if (design == DesignLanguage.NOTHING) FontFamily.Monospace else Unbounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.38f).sp,
                    color = if (design == DesignLanguage.NOTHING) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        pair.second
                    },
                )
            }
        }
    }
}

@Composable
fun GlassPanelScrim(onDismiss: () -> Unit, visible: Boolean) {
    if (!visible) return
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(0.dp),
    )
}

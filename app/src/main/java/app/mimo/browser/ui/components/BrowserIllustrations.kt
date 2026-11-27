package app.mimo.browser.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import app.mimo.browser.ui.theme.LocalDesignLanguage
import app.mimo.browser.preferences.DesignLanguage

enum class BrowserIllustration { GLOBE, TABS, DOWNLOAD, BOOKMARKS, HISTORY }

/**
 * Hand-drawn scenes for empty states. They reuse the same soft geometry as the
 * Mimo logo: orbits, rounded cards and a single warm accent.
 */
@Composable
fun BrowserArt(
    kind: BrowserIllustration,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val nothing = LocalDesignLanguage.current == DesignLanguage.NOTHING
    val primary = scheme.primary
    val secondary = scheme.secondary
    val tertiary = scheme.tertiary
    val surface = scheme.surfaceContainerHigh
    val outline = scheme.outlineVariant
    val container = if (nothing) scheme.surfaceContainerHigh else scheme.primaryContainer
    Canvas(
        modifier
            .fillMaxWidth()
            .aspectRatio(1.45f),
    ) {
        val w = size.width
        val h = size.height
        val corner = if (nothing) 8.dp.toPx() else 34.dp.toPx()

        // Soft stage.
        drawRoundRect(
            color = container.copy(alpha = if (nothing) 0.7f else 0.45f),
            topLeft = Offset(w * 0.06f, h * 0.12f),
            size = Size(w * 0.88f, h * 0.78f),
            cornerRadius = CornerRadius(corner),
        )
        if (!nothing) {
            drawCircle(
                color = secondary.copy(alpha = 0.16f),
                radius = w * 0.17f,
                center = Offset(w * 0.82f, h * 0.28f),
            )
            drawCircle(
                color = tertiary.copy(alpha = 0.14f),
                radius = w * 0.13f,
                center = Offset(w * 0.16f, h * 0.74f),
            )
        }

        when (kind) {
            BrowserIllustration.GLOBE -> drawGlobe(w, h, primary, secondary, surface, outline)
            BrowserIllustration.TABS -> drawTabs(w, h, primary, secondary, tertiary, surface, outline, nothing)
            BrowserIllustration.DOWNLOAD -> drawDownload(w, h, primary, secondary, surface, outline)
            BrowserIllustration.BOOKMARKS -> drawBookmarks(w, h, primary, secondary, surface, outline)
            BrowserIllustration.HISTORY -> drawHistory(w, h, primary, secondary, surface, outline)
        }

        // A tiny Mimo star in the corner.
        val cx = w * 0.86f
        val cy = h * 0.80f
        val arm = w * 0.035f
        drawLine(primary.copy(alpha = 0.6f), Offset(cx - arm, cy), Offset(cx + arm, cy), 2.dp.toPx(), StrokeCap.Round)
        drawLine(primary.copy(alpha = 0.6f), Offset(cx, cy - arm), Offset(cx, cy + arm), 2.dp.toPx(), StrokeCap.Round)
        drawCircle(secondary, radius = w * 0.011f, center = Offset(cx, cy))
    }
}

private fun DrawScope.drawGlobe(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    surface: Color,
    outline: Color,
) {
    val center = Offset(w * 0.44f, h * 0.50f)
    val radius = w * 0.21f
    drawCircle(primary.copy(alpha = 0.14f), radius, center)
    drawCircle(primary, radius, center, style = Stroke(2.4f.dp.toPx()))
    drawOval(
        color = primary.copy(alpha = 0.55f),
        topLeft = Offset(center.x - radius * 0.45f, center.y - radius),
        size = Size(radius * 0.9f, radius * 2f),
        style = Stroke(1.6f.dp.toPx()),
    )
    drawOval(
        color = primary.copy(alpha = 0.40f),
        topLeft = Offset(center.x - radius, center.y - radius * 0.45f),
        size = Size(radius * 2f, radius * 0.9f),
        style = Stroke(1.6f.dp.toPx()),
    )
    drawArc(
        color = primary.copy(alpha = 0.45f),
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius * 0.45f),
        size = Size(radius * 2f, radius * 0.9f),
        style = Stroke(1.4f.dp.toPx()),
    )
    drawCircle(secondary, radius * 0.10f, Offset(center.x + radius * 0.45f, center.y + radius * 0.30f))
    drawCircle(primary.copy(alpha = 0.5f), 3.dp.toPx(), Offset(center.x - radius * 0.55f, center.y - radius * 0.25f))

    // Orbit ring.
    drawArc(
        color = secondary.copy(alpha = 0.55f),
        startAngle = 155f,
        sweepAngle = 210f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 1.5f, center.y - radius * 0.55f),
        size = Size(radius * 3f, radius * 1.1f),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
    )

    // A small secure card floating nearby.
    val cardLeft = w * 0.66f
    val cardTop = h * 0.56f
    drawRoundRect(
        color = surface,
        topLeft = Offset(cardLeft, cardTop),
        size = Size(w * 0.20f, h * 0.22f),
        cornerRadius = CornerRadius(12.dp.toPx()),
    )
    drawRoundRect(
        color = outline.copy(alpha = 0.5f),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(w * 0.20f, h * 0.22f),
        cornerRadius = CornerRadius(12.dp.toPx()),
        style = Stroke(1.dp.toPx()),
    )
    drawRoundRect(
        color = primary,
        topLeft = Offset(cardLeft + w * 0.075f, cardTop + h * 0.06f),
        size = Size(w * 0.05f, h * 0.055f),
        cornerRadius = CornerRadius(4.dp.toPx()),
        style = Stroke(1.8.dp.toPx()),
    )
    drawCircle(primary, 2.dp.toPx(), Offset(cardLeft + w * 0.10f, cardTop + h * 0.10f))
}

private fun DrawScope.drawTabs(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    surface: Color,
    outline: Color,
    nothing: Boolean,
) {
    val cardW = w * 0.34f
    val cardH = h * 0.44f
    val radius = CornerRadius(if (nothing) 8.dp.toPx() else 18.dp.toPx())

    fun card(x: Float, y: Float, angle: Float, header: Color, active: Boolean) {
        rotate(angle, Offset(x + cardW / 2f, y + cardH / 2f)) {
            drawRoundRect(
                color = if (active) surface else surface.copy(alpha = 0.85f),
                topLeft = Offset(x, y),
                size = Size(cardW, cardH),
                cornerRadius = radius,
            )
            drawRoundRect(
                color = if (active) primary else outline.copy(alpha = 0.6f),
                topLeft = Offset(x, y),
                size = Size(cardW, cardH),
                cornerRadius = radius,
                style = Stroke(if (active) 2.dp.toPx() else 1.dp.toPx()),
            )
            drawRoundRect(
                color = header,
                topLeft = Offset(x + 1.dp.toPx(), y + 1.dp.toPx()),
                size = Size(cardW - 2.dp.toPx(), cardH * 0.28f),
                cornerRadius = radius,
            )
            drawCircle(primary.copy(alpha = 0.35f), 2.2.dp.toPx(), Offset(x + cardW * 0.22f, y + cardH * 0.16f))
            drawLine(
                color = primary.copy(alpha = 0.30f),
                start = Offset(x + cardW * 0.18f, y + cardH * 0.48f),
                end = Offset(x + cardW * 0.82f, y + cardH * 0.48f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = primary.copy(alpha = 0.20f),
                start = Offset(x + cardW * 0.18f, y + cardH * 0.60f),
                end = Offset(x + cardW * 0.64f, y + cardH * 0.60f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = primary.copy(alpha = 0.20f),
                start = Offset(x + cardW * 0.18f, y + cardH * 0.72f),
                end = Offset(x + cardW * 0.72f, y + cardH * 0.72f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }

    card(w * 0.20f, h * 0.27f, -12f, secondary.copy(alpha = 0.35f), false)
    card(w * 0.35f, h * 0.22f, 7f, tertiary.copy(alpha = 0.35f), false)
    card(w * 0.48f, h * 0.30f, 0f, primary.copy(alpha = 0.22f), true)

    // Plus bubble.
    val bx = w * 0.72f
    val by = h * 0.24f
    drawCircle(primary, w * 0.055f, Offset(bx, by))
    drawLine(Color.White, Offset(bx - w * 0.022f, by), Offset(bx + w * 0.022f, by), 2.4.dp.toPx(), StrokeCap.Round)
    drawLine(Color.White, Offset(bx, by - w * 0.022f), Offset(bx, by + w * 0.022f), 2.4.dp.toPx(), StrokeCap.Round)
}

private fun DrawScope.drawDownload(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    surface: Color,
    outline: Color,
) {
    val cx = w * 0.44f
    val cy = h * 0.40f
    drawCircle(primary.copy(alpha = 0.10f), w * 0.19f, Offset(cx, cy))
    drawArc(
        color = primary.copy(alpha = 0.35f),
        startAngle = 20f,
        sweepAngle = 320f,
        useCenter = false,
        topLeft = Offset(cx - w * 0.23f, cy - w * 0.23f),
        size = Size(w * 0.46f, w * 0.46f),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
    )
    drawLine(
        color = primary,
        start = Offset(cx, cy - h * 0.13f),
        end = Offset(cx, cy + h * 0.13f),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = primary,
        start = Offset(cx - w * 0.055f, cy + h * 0.065f),
        end = Offset(cx, cy + h * 0.13f),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = primary,
        start = Offset(cx + w * 0.055f, cy + h * 0.065f),
        end = Offset(cx, cy + h * 0.13f),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round,
    )

    // Tray.
    val trayLeft = w * 0.29f
    val trayTop = h * 0.58f
    drawRoundRect(
        color = surface,
        topLeft = Offset(trayLeft, trayTop),
        size = Size(w * 0.30f, h * 0.16f),
        cornerRadius = CornerRadius(10.dp.toPx()),
    )
    drawRoundRect(
        color = outline.copy(alpha = 0.7f),
        topLeft = Offset(trayLeft, trayTop),
        size = Size(w * 0.30f, h * 0.16f),
        cornerRadius = CornerRadius(10.dp.toPx()),
        style = Stroke(1.4.dp.toPx()),
    )
    drawLine(
        color = primary.copy(alpha = 0.6f),
        start = Offset(trayLeft + w * 0.06f, trayTop + h * 0.08f),
        end = Offset(trayLeft + w * 0.24f, trayTop + h * 0.08f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
    )

    drawCircle(secondary, w * 0.04f, Offset(w * 0.70f, h * 0.30f))
    drawCircle(secondary.copy(alpha = 0.5f), w * 0.02f, Offset(w * 0.76f, h * 0.66f))
}

private fun DrawScope.drawBookmarks(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    surface: Color,
    outline: Color,
) {
    val cx = w * 0.44f
    val top = h * 0.20f
    val bw = w * 0.16f
    val bh = h * 0.48f

    fun bookmark(x: Float, y: Float, color: Color, filled: Boolean) {
        val path = Path().apply {
            moveTo(x, y)
            lineTo(x + bw, y)
            lineTo(x + bw, y + bh)
            lineTo(x + bw / 2f, y + bh - h * 0.09f)
            lineTo(x, y + bh)
            close()
        }
        drawPath(path, color = if (filled) color else surface)
        drawPath(
            path,
            color = if (filled) color else outline.copy(alpha = 0.7f),
            style = Stroke(1.8.dp.toPx()),
        )
    }

    bookmark(cx - bw * 1.6f, top + h * 0.06f, secondary.copy(alpha = 0.5f), false)
    bookmark(cx - bw * 0.5f, top, primary, true)

    // Star beside the stack.
    val starCenter = Offset(w * 0.70f, h * 0.62f)
    val r = w * 0.06f
    val path = Path().apply {
        for (i in 0 until 10) {
            val angle = (-90f + i * 36f) * (Math.PI / 180f).toFloat()
            val rad = if (i % 2 == 0) r else r * 0.46f
            val point = Offset(
                starCenter.x + rad * kotlin.math.cos(angle),
                starCenter.y + rad * kotlin.math.sin(angle),
            )
            if (i == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
        close()
    }
    drawPath(path, color = secondary)
    drawLine(
        color = primary.copy(alpha = 0.35f),
        start = Offset(w * 0.24f, h * 0.78f),
        end = Offset(w * 0.60f, h * 0.78f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawHistory(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    surface: Color,
    outline: Color,
) {
    val center = Offset(w * 0.46f, h * 0.50f)
    val radius = w * 0.20f
    drawCircle(surface, radius, center)
    drawCircle(primary, radius, center, style = Stroke(2.4.dp.toPx()))
    drawLine(
        color = primary,
        start = center,
        end = Offset(center.x, center.y - radius * 0.55f),
        strokeWidth = 2.6.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawLine(
        color = primary,
        start = center,
        end = Offset(center.x + radius * 0.42f, center.y + radius * 0.18f),
        strokeWidth = 2.6.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawCircle(secondary, radius * 0.09f, center)
    drawArc(
        color = secondary.copy(alpha = 0.65f),
        startAngle = 160f,
        sweepAngle = 220f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 1.55f, center.y - radius * 0.55f),
        size = Size(radius * 3.1f, radius * 1.1f),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
    )
    drawCircle(secondary, 3.dp.toPx(), Offset(center.x + radius * 1.42f, center.y + radius * 0.12f))
    drawRoundRect(
        color = surface,
        topLeft = Offset(w * 0.68f, h * 0.60f),
        size = Size(w * 0.18f, h * 0.13f),
        cornerRadius = CornerRadius(9.dp.toPx()),
    )
    drawRoundRect(
        color = outline.copy(alpha = 0.55f),
        topLeft = Offset(w * 0.68f, h * 0.60f),
        size = Size(w * 0.18f, h * 0.13f),
        cornerRadius = CornerRadius(9.dp.toPx()),
        style = Stroke(1.dp.toPx()),
    )
    drawLine(
        color = primary.copy(alpha = 0.5f),
        start = Offset(w * 0.71f, h * 0.665f),
        end = Offset(w * 0.83f, h * 0.665f),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

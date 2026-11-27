package app.mimo.browser.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * The main window's root view. Popups compose in their own window, so they
 * cannot capture the app behind them from `LocalView`; they read this instead.
 */
val LocalGlassRoot = staticCompositionLocalOf<View?> { null }

/** A low-resolution snapshot used to paint a frosted panel background. */
data class GlassBackdrop(
    val bitmap: Bitmap,
    val originX: Int,
    val originY: Int,
    val scale: Float,
)

fun captureGlassBackdrop(root: View): GlassBackdrop? {
    if (root.width <= 0 || root.height <= 0) return null
    return runCatching {
        val scale = 0.18f
        val width = (root.width * scale).toInt().coerceAtLeast(1)
        val height = (root.height * scale).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.scale(scale, scale)
        root.draw(canvas)
        val location = IntArray(2)
        root.getLocationOnScreen(location)
        GlassBackdrop(bitmap, location[0], location[1], scale)
    }.getOrNull()
}

@Composable
fun GlassBackdropLayer(
    backdrop: GlassBackdrop,
    panel: Rect,
    blurRadius: Dp,
    tint: Brush,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
            drawBackdrop(backdrop, panel)
        }
        Box(Modifier.fillMaxSize().background(tint))
    }
}

private fun DrawScope.drawBackdrop(backdrop: GlassBackdrop, panel: Rect) {
    val bitmap = backdrop.bitmap
    val left = ((panel.left - backdrop.originX) * backdrop.scale).toInt()
    val top = ((panel.top - backdrop.originY) * backdrop.scale).toInt()
    val width = (panel.width * backdrop.scale).toInt().coerceAtLeast(1)
    val height = (panel.height * backdrop.scale).toInt().coerceAtLeast(1)
    val srcX = left.coerceIn(0, (bitmap.width - 1).coerceAtLeast(0))
    val srcY = top.coerceIn(0, (bitmap.height - 1).coerceAtLeast(0))
    val srcWidth = width.coerceAtMost(bitmap.width - srcX).coerceAtLeast(1)
    val srcHeight = height.coerceAtMost(bitmap.height - srcY).coerceAtLeast(1)
    drawImage(
        image = bitmap.asImageBitmap(),
        srcOffset = IntOffset(srcX, srcY),
        srcSize = IntSize(srcWidth, srcHeight),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(size.width.toInt(), size.height.toInt()),
        filterQuality = FilterQuality.High,
    )
}

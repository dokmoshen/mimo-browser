package app.mimo.browser.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import app.mimo.browser.preferences.DesignLanguage
import app.mimo.browser.ui.theme.LocalDesignLanguage
import app.mimo.browser.ui.theme.LocalMimoDark
import kotlin.random.Random

/**
 * A very subtle, deterministic paper grain. It lives between the backdrop and
 * the content, so cards and text stay crisp while the surface stops looking
 * like a flat rectangle.
 */
@Composable
fun PaperGrain(
    modifier: Modifier = Modifier,
    alpha: Float = 0.55f,
) {
    if (LocalDesignLanguage.current == DesignLanguage.NOTHING) return
    val dark = LocalMimoDark.current
    val image = remember(dark) { grainImage(dark) }
    val brush = remember(image) {
        ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated))
    }
    Canvas(modifier) {
        drawRect(brush = brush, alpha = alpha)
    }
}

private fun grainImage(dark: Boolean): ImageBitmap {
    val size = 160
    val bitmap = Bitmap.createBitmap(
        size,
        size,
        Bitmap.Config.ARGB_8888,
    )
    val random = Random(if (dark) 71 else 37)
    val color = if (dark) 0x00FFFFFF else 0x00000000
    val pixels = IntArray(size * size) { index ->
        val x = index % size
        val y = index / size
        // Sparse speckles, slightly denser in irregular tiny clusters.
        val cluster = ((x * 13 + y * 7) % 29 == 0)
        val chance = if (cluster) 9 else 26
        val speck = random.nextInt(100) < chance
        if (!speck) {
            0
        } else {
            val alpha = random.nextInt(3, 13)
            (alpha shl 24) or (color and 0x00FFFFFF)
        }
    }
    bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
    return bitmap.asImageBitmap()
}

package app.mimo.browser.ui.browser

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect

/**
 * Blurs only the two areas behind the floating address bar and dock, leaving
 * the rest of the page sharp. The rectangles come from the real Compose
 * surfaces, so the blur can never drift away from the pill it belongs to.
 *
 * The blur runs as two chained separable passes (horizontal, then vertical),
 * which looks smooth without the grid artifacts of one sparse 2D kernel.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun createChromeBlurEffect(
    width: Int,
    height: Int,
    density: Float,
    addressBar: Rect?,
    dock: Rect?,
): RenderEffect? {
    if (width <= 0 || height <= 0) return null
    if (addressBar == null && dock == null) return null
    return runCatching {
        val horizontal = RuntimeShader(CHROME_BLUR_AGSL).apply {
            setFloatUniform("resolution", width.toFloat(), height.toFloat())
            setRects(addressBar, dock)
            setFloatUniform("feather", 7f * density)
            setFloatUniform("radius", 1.5f * density)
            setFloatUniform("direction", 1f, 0f)
        }
        val vertical = RuntimeShader(CHROME_BLUR_AGSL).apply {
            setFloatUniform("resolution", width.toFloat(), height.toFloat())
            setRects(addressBar, dock)
            setFloatUniform("feather", 7f * density)
            setFloatUniform("radius", 1.5f * density)
            setFloatUniform("direction", 0f, 1f)
        }
        val first = RenderEffect.createRuntimeShaderEffect(horizontal, "content")
        RenderEffect.createChainEffect(
            RenderEffect.createRuntimeShaderEffect(vertical, "content"),
            first,
        )
    }.getOrNull()
}

private fun RuntimeShader.setRects(addressBar: Rect?, dock: Rect?) {
    setFloatUniform(
        "topRect",
        addressBar?.left ?: 0f,
        addressBar?.top ?: 0f,
        addressBar?.right ?: 0f,
        addressBar?.bottom ?: 0f,
    )
    setFloatUniform(
        "bottomRect",
        dock?.left ?: 0f,
        dock?.top ?: 0f,
        dock?.right ?: 0f,
        dock?.bottom ?: 0f,
    )
    setFloatUniform("hasTop", if (addressBar != null) 1f else 0f)
    setFloatUniform("hasBottom", if (dock != null) 1f else 0f)
}

private const val CHROME_BLUR_AGSL = """
uniform shader content;
uniform float2 resolution;
uniform float4 topRect;
uniform float4 bottomRect;
uniform float feather;
uniform float radius;
uniform float2 direction;
uniform float hasTop;
uniform float hasBottom;

float roundedMask(float2 p, float4 r, float feather) {
    float2 halfSize = (r.zw - r.xy) * 0.5;
    float corner = min(halfSize.x, halfSize.y);
    float2 center = r.xy + halfSize;
    float2 q = abs(p - center) - (halfSize - float2(corner));
    float edge = length(max(q, float2(0.0))) +
        min(max(q.x, q.y), 0.0) - corner;
    return 1.0 - smoothstep(-feather, feather, edge);
}

half4 main(float2 coord) {
    half4 sharp = content.eval(coord);
    float mask = 0.0;
    if (hasTop > 0.5) {
        mask = max(mask, roundedMask(coord, topRect, feather));
    }
    if (hasBottom > 0.5) {
        mask = max(mask, roundedMask(coord, bottomRect, feather));
    }
    if (mask <= 0.004) return sharp;

    half4 sum = half4(0.0);
    float total = 0.0;
    for (int i = -20; i <= 20; i++) {
        float index = float(i);
        float2 offset = direction * index * radius;
        float2 p = clamp(coord + offset, float2(0.0), resolution);
        float weight = exp(-index * index / 70.0);
        sum += content.eval(p) * weight;
        total += weight;
    }
    return mix(sharp, sum / total, mask);
}
"""

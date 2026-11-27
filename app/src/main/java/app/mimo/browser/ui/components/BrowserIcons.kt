package app.mimo.browser.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Browser-specific line glyphs, drawn in the same two house weights as the
 * Mimo icons. Geometry is based on the ISC-licensed Lucide set.
 */
private enum class BrowserGlyphWeight(
    val strokeWidth: Float,
    val cap: StrokeCap,
    val join: StrokeJoin,
) {
    SOFT(1.85f, StrokeCap.Round, StrokeJoin.Round),
    TECHNICAL(1.5f, StrokeCap.Butt, StrokeJoin.Miter),
}

interface BrowserIconSet {
    val ArrowForward: ImageVector
    val Refresh: ImageVector
    val Globe: ImageVector
    val Home: ImageVector
    val Tabs: ImageVector
    val Star: ImageVector
    val StarFilled: ImageVector
    val Bookmark: ImageVector
    val BookmarkFilled: ImageVector
    val Shield: ImageVector
    val ExternalLink: ImageVector
    val Desktop: ImageVector
    val ChevronDown: ImageVector
    val File: ImageVector
    val Folder: ImageVector
    val DownloadCloud: ImageVector
}

val LocalBrowserIcons = staticCompositionLocalOf<BrowserIconSet> { SoftBrowserIcons }

object BrowserIcons {
    val ArrowForward: ImageVector @Composable get() = LocalBrowserIcons.current.ArrowForward
    val Refresh: ImageVector @Composable get() = LocalBrowserIcons.current.Refresh
    val Globe: ImageVector @Composable get() = LocalBrowserIcons.current.Globe
    val Home: ImageVector @Composable get() = LocalBrowserIcons.current.Home
    val Tabs: ImageVector @Composable get() = LocalBrowserIcons.current.Tabs
    val Star: ImageVector @Composable get() = LocalBrowserIcons.current.Star
    val StarFilled: ImageVector @Composable get() = LocalBrowserIcons.current.StarFilled
    val Bookmark: ImageVector @Composable get() = LocalBrowserIcons.current.Bookmark
    val BookmarkFilled: ImageVector @Composable get() = LocalBrowserIcons.current.BookmarkFilled
    val Shield: ImageVector @Composable get() = LocalBrowserIcons.current.Shield
    val ExternalLink: ImageVector @Composable get() = LocalBrowserIcons.current.ExternalLink
    val Desktop: ImageVector @Composable get() = LocalBrowserIcons.current.Desktop
    val ChevronDown: ImageVector @Composable get() = LocalBrowserIcons.current.ChevronDown
    val File: ImageVector @Composable get() = LocalBrowserIcons.current.File
    val Folder: ImageVector @Composable get() = LocalBrowserIcons.current.Folder
    val DownloadCloud: ImageVector @Composable get() = LocalBrowserIcons.current.DownloadCloud
}

private object BrowserGlyphs {
    private val paths: Map<String, List<String>> = mapOf(
        "ArrowForward" to listOf(
            "M5 12h14",
            "m12 5 7 7-7 7",
        ),
        "Refresh" to listOf(
            "M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8",
            "M21 3v5h-5",
        ),
        "Globe" to listOf(
            "M 2.0 12.0 a 10.0 10.0 0 1 0 20.0 0 a 10.0 10.0 0 1 0 -20.0 0",
            "M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20",
            "M2 12h20",
        ),
        "Home" to listOf(
            "m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
            "M9 22V12h6v10",
        ),
        "Tabs" to listOf(
            "M7 3h12a2 2 0 0 1 2 2v12",
            "M5 7h10a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2Z",
        ),
        "Star" to listOf(
            "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z",
        ),
        "StarFilled" to listOf(
            "M11.525 2.295a.53.53 0 0 1 .95 0l2.31 4.679a2.123 2.123 0 0 0 1.595 1.16l5.166.756a.53.53 0 0 1 .294.904l-3.736 3.638a2.123 2.123 0 0 0-.611 1.878l.882 5.14a.53.53 0 0 1-.771.56l-4.618-2.428a2.122 2.122 0 0 0-1.973 0L6.396 21.01a.53.53 0 0 1-.77-.56l.881-5.139a2.122 2.122 0 0 0-.611-1.879L2.16 9.795a.53.53 0 0 1 .294-.906l5.165-.755a2.122 2.122 0 0 0 1.597-1.16z",
        ),
        "Bookmark" to listOf(
            "M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z",
        ),
        "BookmarkFilled" to listOf(
            "M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z",
        ),
        "Shield" to listOf(
            "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z",
            "m9 12 2 2 4-4",
        ),
        "ExternalLink" to listOf(
            "M15 3h6v6",
            "M10 14 21 3",
            "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6",
        ),
        "Desktop" to listOf(
            "M 2.0 3.0 h 20.0 a 2.0 2.0 0 0 1 2.0 2.0 v 10.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -20.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -10.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z",
            "M8 21h8",
            "M12 17v4",
        ),
        "ChevronDown" to listOf(
            "m6 9 6 6 6-6",
        ),
        "File" to listOf(
            "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z",
            "M14 2v4a2 2 0 0 0 2 2h4",
        ),
        "Folder" to listOf(
            "M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z",
        ),
        "DownloadCloud" to listOf(
            "M12 13v8l-4-4",
            "m12 21 4-4",
            "M4.393 15.269A7 7 0 1 1 15.71 8h1.79a4.5 4.5 0 0 1 2.436 8.284",
        ),
    )

    private val filled = setOf("StarFilled", "BookmarkFilled")

    private val softCache = HashMap<String, ImageVector>()
    private val technicalCache = HashMap<String, ImageVector>()

    fun soft(name: String): ImageVector = softCache.getOrPut(name) { build(name, BrowserGlyphWeight.SOFT) }

    fun technical(name: String): ImageVector =
        technicalCache.getOrPut(name) { build(name, BrowserGlyphWeight.TECHNICAL) }

    private fun build(name: String, weight: BrowserGlyphWeight): ImageVector {
        val builder = ImageVector.Builder(
            name = "MimoBrowser$name${weight.name}",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        val isFilled = name in filled
        paths[name].orEmpty().forEach { data ->
            val nodes = PathParser().parsePathString(data).toNodes()
            if (isFilled) {
                builder.addPath(
                    pathData = nodes,
                    fill = SolidColor(Color.Black),
                )
            } else {
                builder.addPath(
                    pathData = nodes,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = weight.strokeWidth,
                    strokeLineCap = weight.cap,
                    strokeLineJoin = weight.join,
                )
            }
        }
        return builder.build()
    }
}

internal object SoftBrowserIcons : BrowserIconSet {
    override val ArrowForward: ImageVector get() = BrowserGlyphs.soft("ArrowForward")
    override val Refresh: ImageVector get() = BrowserGlyphs.soft("Refresh")
    override val Globe: ImageVector get() = BrowserGlyphs.soft("Globe")
    override val Home: ImageVector get() = BrowserGlyphs.soft("Home")
    override val Tabs: ImageVector get() = BrowserGlyphs.soft("Tabs")
    override val Star: ImageVector get() = BrowserGlyphs.soft("Star")
    override val StarFilled: ImageVector get() = BrowserGlyphs.soft("StarFilled")
    override val Bookmark: ImageVector get() = BrowserGlyphs.soft("Bookmark")
    override val BookmarkFilled: ImageVector get() = BrowserGlyphs.soft("BookmarkFilled")
    override val Shield: ImageVector get() = BrowserGlyphs.soft("Shield")
    override val ExternalLink: ImageVector get() = BrowserGlyphs.soft("ExternalLink")
    override val Desktop: ImageVector get() = BrowserGlyphs.soft("Desktop")
    override val ChevronDown: ImageVector get() = BrowserGlyphs.soft("ChevronDown")
    override val File: ImageVector get() = BrowserGlyphs.soft("File")
    override val Folder: ImageVector get() = BrowserGlyphs.soft("Folder")
    override val DownloadCloud: ImageVector get() = BrowserGlyphs.soft("DownloadCloud")
}

internal object TechnicalBrowserIcons : BrowserIconSet {
    override val ArrowForward: ImageVector get() = BrowserGlyphs.technical("ArrowForward")
    override val Refresh: ImageVector get() = BrowserGlyphs.technical("Refresh")
    override val Globe: ImageVector get() = BrowserGlyphs.technical("Globe")
    override val Home: ImageVector get() = BrowserGlyphs.technical("Home")
    override val Tabs: ImageVector get() = BrowserGlyphs.technical("Tabs")
    override val Star: ImageVector get() = BrowserGlyphs.technical("Star")
    override val StarFilled: ImageVector get() = BrowserGlyphs.technical("StarFilled")
    override val Bookmark: ImageVector get() = BrowserGlyphs.technical("Bookmark")
    override val BookmarkFilled: ImageVector get() = BrowserGlyphs.technical("BookmarkFilled")
    override val Shield: ImageVector get() = BrowserGlyphs.technical("Shield")
    override val ExternalLink: ImageVector get() = BrowserGlyphs.technical("ExternalLink")
    override val Desktop: ImageVector get() = BrowserGlyphs.technical("Desktop")
    override val ChevronDown: ImageVector get() = BrowserGlyphs.technical("ChevronDown")
    override val File: ImageVector get() = BrowserGlyphs.technical("File")
    override val Folder: ImageVector get() = BrowserGlyphs.technical("Folder")
    override val DownloadCloud: ImageVector get() = BrowserGlyphs.technical("DownloadCloud")
}

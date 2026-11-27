package app.mimo.browser.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Mimo line glyphs. Geometry is based on the ISC-licensed Lucide set and is
 * rendered in two house weights: soft for Expressive, technical for Nothing.
 */
internal enum class MimoGlyphWeight(
    val strokeWidth: Float,
    val cap: StrokeCap,
    val join: StrokeJoin,
) {
    SOFT(1.85f, StrokeCap.Round, StrokeJoin.Round),
    TECHNICAL(1.5f, StrokeCap.Butt, StrokeJoin.Miter),
}

internal object MimoGlyphs {
    private val paths: Map<String, List<String>> = mapOf(
        "Add" to listOf(
            """M5 12h14""",
            """M12 5v14""",
        ),
        "Alarm" to listOf(
            """M 4.0 13.0 a 8.0 8.0 0 1 0 16.0 0 a 8.0 8.0 0 1 0 -16.0 0""",
            """M12 9v4l2 2""",
            """M5 3 2 6""",
            """m22 6-3-3""",
            """M6.38 18.7 4 21""",
            """M17.64 18.67 20 21""",
        ),
        "Archive" to listOf(
            """M 3.0 3.0 h 18.0 a 1.0 1.0 0 0 1 1.0 1.0 v 3.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -18.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -3.0 a 1.0 1.0 0 0 1 1.0 -1.0 Z""",
            """M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8""",
            """M10 12h4""",
        ),
        "ArrowBack" to listOf(
            """m12 19-7-7 7-7""",
            """M19 12H5""",
        ),
        "ArrowDownward" to listOf(
            """M12 5v14""",
            """m19 12-7 7-7-7""",
        ),
        "ArrowUpward" to listOf(
            """m5 12 7-7 7 7""",
            """M12 19V5""",
        ),
        "AudioFile" to listOf(
            """M17.5 22h.5a2 2 0 0 0 2-2V7l-5-5H6a2 2 0 0 0-2 2v3""",
            """M14 2v4a2 2 0 0 0 2 2h4""",
            """M2 19a2 2 0 1 1 4 0v1a2 2 0 1 1-4 0v-4a6 6 0 0 1 12 0v4a2 2 0 1 1-4 0v-1a2 2 0 1 1 4 0""",
        ),
        "AutoAwesome" to listOf(
            """M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z""",
            """M20 3v4""",
            """M22 5h-4""",
            """M4 17v2""",
            """M5 18H3""",
        ),
        "AutoFixHigh" to listOf(
            """m21.64 3.64-1.28-1.28a1.21 1.21 0 0 0-1.72 0L2.36 18.64a1.21 1.21 0 0 0 0 1.72l1.28 1.28a1.2 1.2 0 0 0 1.72 0L21.64 5.36a1.2 1.2 0 0 0 0-1.72""",
            """m14 7 3 3""",
            """M5 6v4""",
            """M19 14v4""",
            """M10 2v2""",
            """M7 8H3""",
            """M21 16h-4""",
            """M11 3H9""",
        ),
        "BlurOn" to listOf(
            """M 9.0 12.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
            """M3 7V5a2 2 0 0 1 2-2h2""",
            """M17 3h2a2 2 0 0 1 2 2v2""",
            """M21 17v2a2 2 0 0 1-2 2h-2""",
            """M7 21H5a2 2 0 0 1-2-2v-2""",
        ),
        "BrightnessAuto" to listOf(
            """M12 8a2.83 2.83 0 0 0 4 4 4 4 0 1 1-4-4""",
            """M12 2v2""",
            """M12 20v2""",
            """m4.9 4.9 1.4 1.4""",
            """m17.7 17.7 1.4 1.4""",
            """M2 12h2""",
            """M20 12h2""",
            """m6.3 17.7-1.4 1.4""",
            """m19.1 4.9-1.4 1.4""",
        ),
        "Brush" to listOf(
            """m9.06 11.9 8.07-8.06a2.85 2.85 0 1 1 4.03 4.03l-8.06 8.08""",
            """M7.07 14.94c-1.66 0-3 1.35-3 3.02 0 1.33-2.5 1.52-2 2.02 1.08 1.1 2.49 2.02 4 2.02 2.2 0 4-1.8 4-4.04a3.01 3.01 0 0 0-3-3.02z""",
        ),
        "CalendarMonth" to listOf(
            """M8 2v4""",
            """M16 2v4""",
            """M 5.0 4.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 14.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -14.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M3 10h18""",
        ),
        "Check" to listOf(
            """M20 6 9 17l-5-5""",
        ),
        "CheckCircle" to listOf(
            """M 2.0 12.0 a 10.0 10.0 0 1 0 20.0 0 a 10.0 10.0 0 1 0 -20.0 0""",
            """m9 12 2 2 4-4""",
        ),
        "Checklist" to listOf(
            """m3 17 2 2 4-4""",
            """m3 7 2 2 4-4""",
            """M13 6h8""",
            """M13 12h8""",
            """M13 18h8""",
        ),
        "ChevronRight" to listOf(
            """m9 18 6-6-6-6""",
        ),
        "Close" to listOf(
            """M18 6 6 18""",
            """m6 6 12 12""",
        ),
        "ColorLens" to listOf(
            """M 13.0 6.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M 17.0 10.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M 8.0 7.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M 6.0 12.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z""",
        ),
        "ContentCopy" to listOf(
            """M 10.0 8.0 h 10.0 a 2.0 2.0 0 0 1 2.0 2.0 v 10.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -10.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -10.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2""",
        ),
        "DarkMode" to listOf(
            """M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z""",
        ),
        "Delete" to listOf(
            """M3 6h18""",
            """M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6""",
            """M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2""",
            """M 10 11 L 10 17""",
            """M 14 11 L 14 17""",
        ),
        "DeleteForever" to listOf(
            """M3 6h18""",
            """M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6""",
            """M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2""",
        ),
        "DeleteSweep" to listOf(
            """M3 6h18""",
            """M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6""",
            """M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2""",
            """M 10 11 L 10 17""",
            """M 14 11 L 14 17""",
        ),
        "Edit" to listOf(
            """M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z""",
            """m15 5 4 4""",
        ),
        "FileDownload" to listOf(
            """M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4""",
            """M """,
            """M 12 15 L 12 3""",
        ),
        "FileOpen" to listOf(
            """m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2""",
        ),
        "Fingerprint" to listOf(
            """M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4""",
            """M14 13.12c0 2.38 0 6.38-1 8.88""",
            """M17.29 21.02c.12-.6.43-2.3.5-3.02""",
            """M2 12a10 10 0 0 1 18-6""",
            """M2 16h.01""",
            """M21.8 16c.2-2 .131-5.354 0-6""",
            """M5 19.5C5.5 18 6 15 6 12a6 6 0 0 1 .34-2""",
            """M8.65 22c.21-.66.45-1.32.57-2""",
            """M9 6.8a6 6 0 0 1 9 5.2v2""",
        ),
        "FontDownload" to listOf(
            """M """,
            """M 9 20 L 15 20""",
            """M 12 4 L 12 20""",
        ),
        "FormatBold" to listOf(
            """M6 12h9a4 4 0 0 1 0 8H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1h7a4 4 0 0 1 0 8""",
        ),
        "FormatIndentIncrease" to listOf(
            """M21 12H11""",
            """M21 18H11""",
            """M21 6H11""",
            """m3 8 4 4-4 4""",
        ),
        "FormatItalic" to listOf(
            """M 19 4 L 10 4""",
            """M 14 20 L 5 20""",
            """M 15 4 L 9 20""",
        ),
        "FormatListBulleted" to listOf(
            """M3 12h.01""",
            """M3 18h.01""",
            """M3 6h.01""",
            """M8 12h13""",
            """M8 18h13""",
            """M8 6h13""",
        ),
        "FormatListNumbered" to listOf(
            """M10 12h11""",
            """M10 18h11""",
            """M10 6h11""",
            """M4 10h2""",
            """M4 6h1v4""",
            """M6 18H4c0-1 2-2 2-3s-1-1.5-2-1""",
        ),
        "FormatStrikethrough" to listOf(
            """M16 4H9a3 3 0 0 0-2.83 4""",
            """M14 12a4 4 0 0 1 0 8H6""",
            """M 4 12 L 20 12""",
        ),
        "FormatUnderlined" to listOf(
            """M6 4v6a6 6 0 0 0 12 0V4""",
            """M 4 20 L 20 20""",
        ),
        "GraphicEq" to listOf(
            """M2 10v3""",
            """M6 6v11""",
            """M10 3v18""",
            """M14 8v7""",
            """M18 5v13""",
            """M22 10v3""",
        ),
        "GridView" to listOf(
            """M12 3v18""",
            """M3 12h18""",
            """M 5.0 3.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 14.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -14.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
        ),
        "Highlight" to listOf(
            """m9 11-6 6v3h9l3-3""",
            """m22 12-4.6 4.6a2 2 0 0 1-2.8 0l-5.2-5.2a2 2 0 0 1 0-2.8L14 4""",
        ),
        "History" to listOf(
            """M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8""",
            """M3 3v5h5""",
            """M12 7v5l4 2""",
        ),
        "Image" to listOf(
            """M 5.0 3.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 14.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -14.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M 7.0 9.0 a 2.0 2.0 0 1 0 4.0 0 a 2.0 2.0 0 1 0 -4.0 0""",
            """m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21""",
        ),
        "Info" to listOf(
            """M 2.0 12.0 a 10.0 10.0 0 1 0 20.0 0 a 10.0 10.0 0 1 0 -20.0 0""",
            """M12 16v-4""",
            """M12 8h.01""",
        ),
        "Inventory2" to listOf(
            """M11 21.73a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73z""",
            """M12 22V12""",
            """m3.3 7 7.703 4.734a2 2 0 0 0 1.994 0L20.7 7""",
            """m7.5 4.27 9 5.15""",
        ),
        "Label" to listOf(
            """M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z""",
            """M 7.0 7.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
        ),
        "LightMode" to listOf(
            """M 8.0 12.0 a 4.0 4.0 0 1 0 8.0 0 a 4.0 4.0 0 1 0 -8.0 0""",
            """M12 2v2""",
            """M12 20v2""",
            """m4.93 4.93 1.41 1.41""",
            """m17.66 17.66 1.41 1.41""",
            """M2 12h2""",
            """M20 12h2""",
            """m6.34 17.66-1.41 1.41""",
            """m19.07 4.93-1.41 1.41""",
        ),
        "Lock" to listOf(
            """M 5.0 11.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 7.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -7.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M7 11V7a5 5 0 0 1 10 0v4""",
        ),
        "LockOpen" to listOf(
            """M 5.0 11.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 7.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -7.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M7 11V7a5 5 0 0 1 9.9-1""",
        ),
        "Menu" to listOf(
            """M 4 12 L 20 12""",
            """M 4 6 L 20 6""",
            """M 4 18 L 20 18""",
        ),
        "MenuBook" to listOf(
            """M12 7v14""",
            """M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z""",
        ),
        "Mic" to listOf(
            """M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z""",
            """M19 10v2a7 7 0 0 1-14 0v-2""",
            """M 12 19 L 12 22""",
        ),
        "MoreHoriz" to listOf(
            """M 11.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0""",
            """M 18.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0""",
            """M 4.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0""",
        ),
        "MoreVert" to listOf(
            """M 11.0 12.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0""",
            """M 11.0 5.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0""",
            """M 11.0 19.0 a 1.0 1.0 0 1 0 2.0 0 a 1.0 1.0 0 1 0 -2.0 0""",
        ),
        "Notes" to listOf(
            """M2 6h4""",
            """M2 10h4""",
            """M2 14h4""",
            """M2 18h4""",
            """M 6.0 2.0 h 12.0 a 2.0 2.0 0 0 1 2.0 2.0 v 16.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -12.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -16.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M16 2v20""",
        ),
        "Notifications" to listOf(
            """M10.268 21a2 2 0 0 0 3.464 0""",
            """M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326""",
        ),
        "NotificationsActive" to listOf(
            """M10.268 21a2 2 0 0 0 3.464 0""",
            """M22 8c0-2.3-.8-4.3-2-6""",
            """M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326""",
            """M4 2C2.8 3.7 2 5.7 2 8""",
        ),
        "NotificationsNone" to listOf(
            """M10.268 21a2 2 0 0 0 3.464 0""",
            """M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326""",
        ),
        "Palette" to listOf(
            """M 13.0 6.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M 17.0 10.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M 8.0 7.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M 6.0 12.5 a 0.5 0.5 0 1 0 1.0 0 a 0.5 0.5 0 1 0 -1.0 0""",
            """M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z""",
        ),
        "Pause" to listOf(
            """M 15.0 4.0 h 2.0 a 1.0 1.0 0 0 1 1.0 1.0 v 14.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -2.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -14.0 a 1.0 1.0 0 0 1 1.0 -1.0 Z""",
            """M 7.0 4.0 h 2.0 a 1.0 1.0 0 0 1 1.0 1.0 v 14.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -2.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -14.0 a 1.0 1.0 0 0 1 1.0 -1.0 Z""",
        ),
        "PhotoCamera" to listOf(
            """M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z""",
            """M 9.0 13.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
        ),
        "PlayArrow" to listOf(
            """M 6 3 L 20 12 L 6 21 L 6 3 Z""",
        ),
        "PushPin" to listOf(
            """M12 21s-6.5-6.1-6.5-11a6.5 6.5 0 0 1 13 0c0 4.9-6.5 11-6.5 11z""",
            """M14.5 10a2.5 2.5 0 1 1-5 0 2.5 2.5 0 0 1 5 0z""",
        ),
        "Redo" to listOf(
            """m15 14 5-5-5-5""",
            """M20 9H9.5A5.5 5.5 0 0 0 4 14.5A5.5 5.5 0 0 0 9.5 20H13""",
        ),
        "Restore" to listOf(
            """M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8""",
            """M3 3v5h5""",
        ),
        "Schedule" to listOf(
            """M 2.0 12.0 a 10.0 10.0 0 1 0 20.0 0 a 10.0 10.0 0 1 0 -20.0 0""",
            """M """,
        ),
        "Search" to listOf(
            """M 3.0 11.0 a 8.0 8.0 0 1 0 16.0 0 a 8.0 8.0 0 1 0 -16.0 0""",
            """m21 21-4.3-4.3""",
        ),
        "SearchOff" to listOf(
            """m13.5 8.5-5 5""",
            """m8.5 8.5 5 5""",
            """M 3.0 11.0 a 8.0 8.0 0 1 0 16.0 0 a 8.0 8.0 0 1 0 -16.0 0""",
            """m21 21-4.3-4.3""",
        ),
        "SelectAll" to listOf(
            """M21 10.5V19a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h12.5""",
            """m9 11 3 3L22 4""",
        ),
        "Settings" to listOf(
            """M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z""",
            """M 9.0 12.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
        ),
        "Share" to listOf(
            """M 15.0 5.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
            """M 3.0 12.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
            """M 15.0 19.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
            """M 8.59 13.51 L 15.42 17.49""",
            """M 15.41 6.51 L 8.59 10.49""",
        ),
        "Sort" to listOf(
            """m21 16-4 4-4-4""",
            """M17 20V4""",
            """m3 8 4-4 4 4""",
            """M7 4v16""",
        ),
        "StickyNote2" to listOf(
            """M16 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V8Z""",
            """M15 3v4a2 2 0 0 0 2 2h4""",
        ),
        "Stop" to listOf(
            """M 5.0 3.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 14.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -14.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
        ),
        "Storage" to listOf(
            """M 3.0 5.0 a 9.0 3.0 0 1 0 18.0 0 a 9.0 3.0 0 1 0 -18.0 0""",
            """M3 5V19A9 3 0 0 0 21 19V5""",
            """M3 12A9 3 0 0 0 21 12""",
        ),
        "SystemUpdate" to listOf(
            """M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8""",
            """M21 3v5h-5""",
            """M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16""",
            """M8 16H3v5""",
        ),
        "Title" to listOf(
            """M """,
            """M 9 20 L 15 20""",
            """M 12 4 L 12 20""",
        ),
        "Tune" to listOf(
            """M 21 4 L 14 4""",
            """M 10 4 L 3 4""",
            """M 21 12 L 12 12""",
            """M 8 12 L 3 12""",
            """M 21 20 L 16 20""",
            """M 12 20 L 3 20""",
            """M 14 2 L 14 6""",
            """M 8 10 L 8 14""",
            """M 16 18 L 16 22""",
        ),
        "Unarchive" to listOf(
            """M 3.0 3.0 h 18.0 a 1.0 1.0 0 0 1 1.0 1.0 v 3.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -18.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -3.0 a 1.0 1.0 0 0 1 1.0 -1.0 Z""",
            """M4 8v11a2 2 0 0 0 2 2h2""",
            """M20 8v11a2 2 0 0 1-2 2h-2""",
            """m9 15 3-3 3 3""",
            """M12 12v9""",
        ),
        "Undo" to listOf(
            """M9 14 4 9l5-5""",
            """M4 9h10.5a5.5 5.5 0 0 1 5.5 5.5a5.5 5.5 0 0 1-5.5 5.5H11""",
        ),
        "ViewAgenda" to listOf(
            """M 5.0 3.0 h 14.0 a 2.0 2.0 0 0 1 2.0 2.0 v 14.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -14.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -14.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M21 9H3""",
            """M21 15H3""",
        ),
        "Visibility" to listOf(
            """M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0""",
            """M 9.0 12.0 a 3.0 3.0 0 1 0 6.0 0 a 3.0 3.0 0 1 0 -6.0 0""",
        ),
        "VisibilityOff" to listOf(
            """M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49""",
            """M14.084 14.158a3 3 0 0 1-4.242-4.242""",
            """M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143""",
            """m2 2 20 20""",
        ),
        "smartphone" to listOf(
            """M 7.0 2.0 h 10.0 a 2.0 2.0 0 0 1 2.0 2.0 v 16.0 a 2.0 2.0 0 0 1 -2.0 2.0 h -10.0 a 2.0 2.0 0 0 1 -2.0 -2.0 v -16.0 a 2.0 2.0 0 0 1 2.0 -2.0 Z""",
            """M12 18h.01""",
        ),
        "Vibration" to listOf(
            """m2 8 2 2-2 2 2 2-2 2""",
            """m22 8-2 2 2 2-2 2 2 2""",
            """M 9.0 5.0 h 6.0 a 1.0 1.0 0 0 1 1.0 1.0 v 12.0 a 1.0 1.0 0 0 1 -1.0 1.0 h -6.0 a 1.0 1.0 0 0 1 -1.0 -1.0 v -12.0 a 1.0 1.0 0 0 1 1.0 -1.0 Z""",
        ),
        "AutoFixOff" to listOf(
            """M15 4V2""",
            """M15 16v-2""",
            """M8 9h2""",
            """M20 9h2""",
            """M17.8 11.8 19 13""",
            """M15 9h.01""",
            """M17.8 6.2 19 5""",
            """m3 21 9-9""",
            """M12.2 6.2 11 5""",
        ),
    )

    private val softCache = HashMap<String, ImageVector>()
    private val technicalCache = HashMap<String, ImageVector>()

    fun soft(name: String): ImageVector =
        softCache.getOrPut(name) { build(name, MimoGlyphWeight.SOFT) }

    fun technical(name: String): ImageVector =
        technicalCache.getOrPut(name) { build(name, MimoGlyphWeight.TECHNICAL) }

    private fun build(name: String, weight: MimoGlyphWeight): ImageVector {
        val builder = ImageVector.Builder(
            name = "Mimo$name${weight.name}",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        paths[name].orEmpty().forEach { data ->
            builder.addPath(
                pathData = PathParser().parsePathString(data).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = weight.strokeWidth,
                strokeLineCap = weight.cap,
                strokeLineJoin = weight.join,
            )
        }
        return builder.build()
    }
}

internal object SoftMimoIcons : MimoIconSet {
    override val Add: ImageVector get() = MimoGlyphs.soft("Add")
    override val Alarm: ImageVector get() = MimoGlyphs.soft("Alarm")
    override val Archive: ImageVector get() = MimoGlyphs.soft("Archive")
    override val ArrowBack: ImageVector get() = MimoGlyphs.soft("ArrowBack")
    override val ArrowDownward: ImageVector get() = MimoGlyphs.soft("ArrowDownward")
    override val ArrowUpward: ImageVector get() = MimoGlyphs.soft("ArrowUpward")
    override val AudioFile: ImageVector get() = MimoGlyphs.soft("AudioFile")
    override val AutoAwesome: ImageVector get() = MimoGlyphs.soft("AutoAwesome")
    override val AutoFixHigh: ImageVector get() = MimoGlyphs.soft("AutoFixHigh")
    override val AutoFixOff: ImageVector get() = MimoGlyphs.soft("AutoFixOff")
    override val BlurOn: ImageVector get() = MimoGlyphs.soft("BlurOn")
    override val BrightnessAuto: ImageVector get() = MimoGlyphs.soft("BrightnessAuto")
    override val Brush: ImageVector get() = MimoGlyphs.soft("Brush")
    override val CalendarMonth: ImageVector get() = MimoGlyphs.soft("CalendarMonth")
    override val Check: ImageVector get() = MimoGlyphs.soft("Check")
    override val CheckCircle: ImageVector get() = MimoGlyphs.soft("CheckCircle")
    override val Checklist: ImageVector get() = MimoGlyphs.soft("Checklist")
    override val ChevronRight: ImageVector get() = MimoGlyphs.soft("ChevronRight")
    override val Close: ImageVector get() = MimoGlyphs.soft("Close")
    override val ColorLens: ImageVector get() = MimoGlyphs.soft("ColorLens")
    override val ContentCopy: ImageVector get() = MimoGlyphs.soft("ContentCopy")
    override val DarkMode: ImageVector get() = MimoGlyphs.soft("DarkMode")
    override val Delete: ImageVector get() = MimoGlyphs.soft("Delete")
    override val DeleteForever: ImageVector get() = MimoGlyphs.soft("DeleteForever")
    override val DeleteSweep: ImageVector get() = MimoGlyphs.soft("DeleteSweep")
    override val Edit: ImageVector get() = MimoGlyphs.soft("Edit")
    override val FileDownload: ImageVector get() = MimoGlyphs.soft("FileDownload")
    override val FileOpen: ImageVector get() = MimoGlyphs.soft("FileOpen")
    override val Fingerprint: ImageVector get() = MimoGlyphs.soft("Fingerprint")
    override val FontDownload: ImageVector get() = MimoGlyphs.soft("FontDownload")
    override val FormatBold: ImageVector get() = MimoGlyphs.soft("FormatBold")
    override val FormatIndentIncrease: ImageVector get() = MimoGlyphs.soft("FormatIndentIncrease")
    override val FormatItalic: ImageVector get() = MimoGlyphs.soft("FormatItalic")
    override val FormatListBulleted: ImageVector get() = MimoGlyphs.soft("FormatListBulleted")
    override val FormatListNumbered: ImageVector get() = MimoGlyphs.soft("FormatListNumbered")
    override val FormatStrikethrough: ImageVector get() = MimoGlyphs.soft("FormatStrikethrough")
    override val FormatUnderlined: ImageVector get() = MimoGlyphs.soft("FormatUnderlined")
    override val GraphicEq: ImageVector get() = MimoGlyphs.soft("GraphicEq")
    override val GridView: ImageVector get() = MimoGlyphs.soft("GridView")
    override val Highlight: ImageVector get() = MimoGlyphs.soft("Highlight")
    override val History: ImageVector get() = MimoGlyphs.soft("History")
    override val Image: ImageVector get() = MimoGlyphs.soft("Image")
    override val Info: ImageVector get() = MimoGlyphs.soft("Info")
    override val Inventory2: ImageVector get() = MimoGlyphs.soft("Inventory2")
    override val Label: ImageVector get() = MimoGlyphs.soft("Label")
    override val LightMode: ImageVector get() = MimoGlyphs.soft("LightMode")
    override val Lock: ImageVector get() = MimoGlyphs.soft("Lock")
    override val LockOpen: ImageVector get() = MimoGlyphs.soft("LockOpen")
    override val Menu: ImageVector get() = MimoGlyphs.soft("Menu")
    override val MenuBook: ImageVector get() = MimoGlyphs.soft("MenuBook")
    override val Mic: ImageVector get() = MimoGlyphs.soft("Mic")
    override val MoreHoriz: ImageVector get() = MimoGlyphs.soft("MoreHoriz")
    override val MoreVert: ImageVector get() = MimoGlyphs.soft("MoreVert")
    override val Notes: ImageVector get() = MimoGlyphs.soft("Notes")
    override val Notifications: ImageVector get() = MimoGlyphs.soft("Notifications")
    override val NotificationsActive: ImageVector get() = MimoGlyphs.soft("NotificationsActive")
    override val NotificationsNone: ImageVector get() = MimoGlyphs.soft("NotificationsNone")
    override val Palette: ImageVector get() = MimoGlyphs.soft("Palette")
    override val Pause: ImageVector get() = MimoGlyphs.soft("Pause")
    override val PhotoCamera: ImageVector get() = MimoGlyphs.soft("PhotoCamera")
    override val PlayArrow: ImageVector get() = MimoGlyphs.soft("PlayArrow")
    override val PushPin: ImageVector get() = MimoGlyphs.soft("PushPin")
    override val Redo: ImageVector get() = MimoGlyphs.soft("Redo")
    override val Restore: ImageVector get() = MimoGlyphs.soft("Restore")
    override val Schedule: ImageVector get() = MimoGlyphs.soft("Schedule")
    override val Search: ImageVector get() = MimoGlyphs.soft("Search")
    override val SearchOff: ImageVector get() = MimoGlyphs.soft("SearchOff")
    override val SelectAll: ImageVector get() = MimoGlyphs.soft("SelectAll")
    override val Settings: ImageVector get() = MimoGlyphs.soft("Settings")
    override val Share: ImageVector get() = MimoGlyphs.soft("Share")
    override val Sort: ImageVector get() = MimoGlyphs.soft("Sort")
    override val StickyNote2: ImageVector get() = MimoGlyphs.soft("StickyNote2")
    override val Stop: ImageVector get() = MimoGlyphs.soft("Stop")
    override val Storage: ImageVector get() = MimoGlyphs.soft("Storage")
    override val SystemUpdate: ImageVector get() = MimoGlyphs.soft("SystemUpdate")
    override val Title: ImageVector get() = MimoGlyphs.soft("Title")
    override val Tune: ImageVector get() = MimoGlyphs.soft("Tune")
    override val Unarchive: ImageVector get() = MimoGlyphs.soft("Unarchive")
    override val Undo: ImageVector get() = MimoGlyphs.soft("Undo")
    override val Vibration: ImageVector get() = MimoGlyphs.soft("Vibration")
    override val ViewAgenda: ImageVector get() = MimoGlyphs.soft("ViewAgenda")
    override val Visibility: ImageVector get() = MimoGlyphs.soft("Visibility")
    override val VisibilityOff: ImageVector get() = MimoGlyphs.soft("VisibilityOff")
}

internal object TechnicalMimoIcons : MimoIconSet {
    override val Add: ImageVector get() = MimoGlyphs.technical("Add")
    override val Alarm: ImageVector get() = MimoGlyphs.technical("Alarm")
    override val Archive: ImageVector get() = MimoGlyphs.technical("Archive")
    override val ArrowBack: ImageVector get() = MimoGlyphs.technical("ArrowBack")
    override val ArrowDownward: ImageVector get() = MimoGlyphs.technical("ArrowDownward")
    override val ArrowUpward: ImageVector get() = MimoGlyphs.technical("ArrowUpward")
    override val AudioFile: ImageVector get() = MimoGlyphs.technical("AudioFile")
    override val AutoAwesome: ImageVector get() = MimoGlyphs.technical("AutoAwesome")
    override val AutoFixHigh: ImageVector get() = MimoGlyphs.technical("AutoFixHigh")
    override val AutoFixOff: ImageVector get() = MimoGlyphs.technical("AutoFixOff")
    override val BlurOn: ImageVector get() = MimoGlyphs.technical("BlurOn")
    override val BrightnessAuto: ImageVector get() = MimoGlyphs.technical("BrightnessAuto")
    override val Brush: ImageVector get() = MimoGlyphs.technical("Brush")
    override val CalendarMonth: ImageVector get() = MimoGlyphs.technical("CalendarMonth")
    override val Check: ImageVector get() = MimoGlyphs.technical("Check")
    override val CheckCircle: ImageVector get() = MimoGlyphs.technical("CheckCircle")
    override val Checklist: ImageVector get() = MimoGlyphs.technical("Checklist")
    override val ChevronRight: ImageVector get() = MimoGlyphs.technical("ChevronRight")
    override val Close: ImageVector get() = MimoGlyphs.technical("Close")
    override val ColorLens: ImageVector get() = MimoGlyphs.technical("ColorLens")
    override val ContentCopy: ImageVector get() = MimoGlyphs.technical("ContentCopy")
    override val DarkMode: ImageVector get() = MimoGlyphs.technical("DarkMode")
    override val Delete: ImageVector get() = MimoGlyphs.technical("Delete")
    override val DeleteForever: ImageVector get() = MimoGlyphs.technical("DeleteForever")
    override val DeleteSweep: ImageVector get() = MimoGlyphs.technical("DeleteSweep")
    override val Edit: ImageVector get() = MimoGlyphs.technical("Edit")
    override val FileDownload: ImageVector get() = MimoGlyphs.technical("FileDownload")
    override val FileOpen: ImageVector get() = MimoGlyphs.technical("FileOpen")
    override val Fingerprint: ImageVector get() = MimoGlyphs.technical("Fingerprint")
    override val FontDownload: ImageVector get() = MimoGlyphs.technical("FontDownload")
    override val FormatBold: ImageVector get() = MimoGlyphs.technical("FormatBold")
    override val FormatIndentIncrease: ImageVector get() = MimoGlyphs.technical("FormatIndentIncrease")
    override val FormatItalic: ImageVector get() = MimoGlyphs.technical("FormatItalic")
    override val FormatListBulleted: ImageVector get() = MimoGlyphs.technical("FormatListBulleted")
    override val FormatListNumbered: ImageVector get() = MimoGlyphs.technical("FormatListNumbered")
    override val FormatStrikethrough: ImageVector get() = MimoGlyphs.technical("FormatStrikethrough")
    override val FormatUnderlined: ImageVector get() = MimoGlyphs.technical("FormatUnderlined")
    override val GraphicEq: ImageVector get() = MimoGlyphs.technical("GraphicEq")
    override val GridView: ImageVector get() = MimoGlyphs.technical("GridView")
    override val Highlight: ImageVector get() = MimoGlyphs.technical("Highlight")
    override val History: ImageVector get() = MimoGlyphs.technical("History")
    override val Image: ImageVector get() = MimoGlyphs.technical("Image")
    override val Info: ImageVector get() = MimoGlyphs.technical("Info")
    override val Inventory2: ImageVector get() = MimoGlyphs.technical("Inventory2")
    override val Label: ImageVector get() = MimoGlyphs.technical("Label")
    override val LightMode: ImageVector get() = MimoGlyphs.technical("LightMode")
    override val Lock: ImageVector get() = MimoGlyphs.technical("Lock")
    override val LockOpen: ImageVector get() = MimoGlyphs.technical("LockOpen")
    override val Menu: ImageVector get() = MimoGlyphs.technical("Menu")
    override val MenuBook: ImageVector get() = MimoGlyphs.technical("MenuBook")
    override val Mic: ImageVector get() = MimoGlyphs.technical("Mic")
    override val MoreHoriz: ImageVector get() = MimoGlyphs.technical("MoreHoriz")
    override val MoreVert: ImageVector get() = MimoGlyphs.technical("MoreVert")
    override val Notes: ImageVector get() = MimoGlyphs.technical("Notes")
    override val Notifications: ImageVector get() = MimoGlyphs.technical("Notifications")
    override val NotificationsActive: ImageVector get() = MimoGlyphs.technical("NotificationsActive")
    override val NotificationsNone: ImageVector get() = MimoGlyphs.technical("NotificationsNone")
    override val Palette: ImageVector get() = MimoGlyphs.technical("Palette")
    override val Pause: ImageVector get() = MimoGlyphs.technical("Pause")
    override val PhotoCamera: ImageVector get() = MimoGlyphs.technical("PhotoCamera")
    override val PlayArrow: ImageVector get() = MimoGlyphs.technical("PlayArrow")
    override val PushPin: ImageVector get() = MimoGlyphs.technical("PushPin")
    override val Redo: ImageVector get() = MimoGlyphs.technical("Redo")
    override val Restore: ImageVector get() = MimoGlyphs.technical("Restore")
    override val Schedule: ImageVector get() = MimoGlyphs.technical("Schedule")
    override val Search: ImageVector get() = MimoGlyphs.technical("Search")
    override val SearchOff: ImageVector get() = MimoGlyphs.technical("SearchOff")
    override val SelectAll: ImageVector get() = MimoGlyphs.technical("SelectAll")
    override val Settings: ImageVector get() = MimoGlyphs.technical("Settings")
    override val Share: ImageVector get() = MimoGlyphs.technical("Share")
    override val Sort: ImageVector get() = MimoGlyphs.technical("Sort")
    override val StickyNote2: ImageVector get() = MimoGlyphs.technical("StickyNote2")
    override val Stop: ImageVector get() = MimoGlyphs.technical("Stop")
    override val Storage: ImageVector get() = MimoGlyphs.technical("Storage")
    override val SystemUpdate: ImageVector get() = MimoGlyphs.technical("SystemUpdate")
    override val Title: ImageVector get() = MimoGlyphs.technical("Title")
    override val Tune: ImageVector get() = MimoGlyphs.technical("Tune")
    override val Unarchive: ImageVector get() = MimoGlyphs.technical("Unarchive")
    override val Undo: ImageVector get() = MimoGlyphs.technical("Undo")
    override val Vibration: ImageVector get() = MimoGlyphs.technical("Vibration")
    override val ViewAgenda: ImageVector get() = MimoGlyphs.technical("ViewAgenda")
    override val Visibility: ImageVector get() = MimoGlyphs.technical("Visibility")
    override val VisibilityOff: ImageVector get() = MimoGlyphs.technical("VisibilityOff")
}

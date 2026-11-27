package app.mimo.browser.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A semantic icon set. Geometry comes from Mimo line glyphs and is rendered in
 * a soft or technical weight by the active design language.
 */
interface MimoIconSet {
    val Add: ImageVector
    val Alarm: ImageVector
    val Archive: ImageVector
    val ArrowBack: ImageVector
    val ArrowDownward: ImageVector
    val ArrowUpward: ImageVector
    val AudioFile: ImageVector
    val AutoAwesome: ImageVector
    val AutoFixHigh: ImageVector
    val AutoFixOff: ImageVector
    val BlurOn: ImageVector
    val BrightnessAuto: ImageVector
    val Brush: ImageVector
    val CalendarMonth: ImageVector
    val Check: ImageVector
    val CheckCircle: ImageVector
    val Checklist: ImageVector
    val ChevronRight: ImageVector
    val Close: ImageVector
    val ColorLens: ImageVector
    val ContentCopy: ImageVector
    val DarkMode: ImageVector
    val Delete: ImageVector
    val DeleteForever: ImageVector
    val DeleteSweep: ImageVector
    val Edit: ImageVector
    val FileDownload: ImageVector
    val FileOpen: ImageVector
    val Fingerprint: ImageVector
    val FontDownload: ImageVector
    val FormatBold: ImageVector
    val FormatIndentIncrease: ImageVector
    val FormatItalic: ImageVector
    val FormatListBulleted: ImageVector
    val FormatListNumbered: ImageVector
    val FormatStrikethrough: ImageVector
    val FormatUnderlined: ImageVector
    val GraphicEq: ImageVector
    val GridView: ImageVector
    val Highlight: ImageVector
    val History: ImageVector
    val Image: ImageVector
    val Info: ImageVector
    val Inventory2: ImageVector
    val Label: ImageVector
    val LightMode: ImageVector
    val Lock: ImageVector
    val LockOpen: ImageVector
    val Menu: ImageVector
    val MenuBook: ImageVector
    val Mic: ImageVector
    val MoreHoriz: ImageVector
    val MoreVert: ImageVector
    val Notes: ImageVector
    val Notifications: ImageVector
    val NotificationsActive: ImageVector
    val NotificationsNone: ImageVector
    val Palette: ImageVector
    val Pause: ImageVector
    val PhotoCamera: ImageVector
    val PlayArrow: ImageVector
    val PushPin: ImageVector
    val Redo: ImageVector
    val Restore: ImageVector
    val Schedule: ImageVector
    val Search: ImageVector
    val SearchOff: ImageVector
    val SelectAll: ImageVector
    val Settings: ImageVector
    val Share: ImageVector
    val Sort: ImageVector
    val StickyNote2: ImageVector
    val Stop: ImageVector
    val Storage: ImageVector
    val SystemUpdate: ImageVector
    val Title: ImageVector
    val Tune: ImageVector
    val Unarchive: ImageVector
    val Undo: ImageVector
    val Vibration: ImageVector
    val ViewAgenda: ImageVector
    val Visibility: ImageVector
    val VisibilityOff: ImageVector
}

val LocalMimoIcons = staticCompositionLocalOf<MimoIconSet> { SoftMimoIcons }

object MimoIcons {
    val Add: ImageVector
        @Composable get() = LocalMimoIcons.current.Add
    val Alarm: ImageVector
        @Composable get() = LocalMimoIcons.current.Alarm
    val Archive: ImageVector
        @Composable get() = LocalMimoIcons.current.Archive
    val ArrowBack: ImageVector
        @Composable get() = LocalMimoIcons.current.ArrowBack
    val ArrowDownward: ImageVector
        @Composable get() = LocalMimoIcons.current.ArrowDownward
    val ArrowUpward: ImageVector
        @Composable get() = LocalMimoIcons.current.ArrowUpward
    val AudioFile: ImageVector
        @Composable get() = LocalMimoIcons.current.AudioFile
    val AutoAwesome: ImageVector
        @Composable get() = LocalMimoIcons.current.AutoAwesome
    val AutoFixHigh: ImageVector
        @Composable get() = LocalMimoIcons.current.AutoFixHigh
    val AutoFixOff: ImageVector
        @Composable get() = LocalMimoIcons.current.AutoFixOff
    val BlurOn: ImageVector
        @Composable get() = LocalMimoIcons.current.BlurOn
    val BrightnessAuto: ImageVector
        @Composable get() = LocalMimoIcons.current.BrightnessAuto
    val Brush: ImageVector
        @Composable get() = LocalMimoIcons.current.Brush
    val CalendarMonth: ImageVector
        @Composable get() = LocalMimoIcons.current.CalendarMonth
    val Check: ImageVector
        @Composable get() = LocalMimoIcons.current.Check
    val CheckCircle: ImageVector
        @Composable get() = LocalMimoIcons.current.CheckCircle
    val Checklist: ImageVector
        @Composable get() = LocalMimoIcons.current.Checklist
    val ChevronRight: ImageVector
        @Composable get() = LocalMimoIcons.current.ChevronRight
    val Close: ImageVector
        @Composable get() = LocalMimoIcons.current.Close
    val ColorLens: ImageVector
        @Composable get() = LocalMimoIcons.current.ColorLens
    val ContentCopy: ImageVector
        @Composable get() = LocalMimoIcons.current.ContentCopy
    val DarkMode: ImageVector
        @Composable get() = LocalMimoIcons.current.DarkMode
    val Delete: ImageVector
        @Composable get() = LocalMimoIcons.current.Delete
    val DeleteForever: ImageVector
        @Composable get() = LocalMimoIcons.current.DeleteForever
    val DeleteSweep: ImageVector
        @Composable get() = LocalMimoIcons.current.DeleteSweep
    val Edit: ImageVector
        @Composable get() = LocalMimoIcons.current.Edit
    val FileDownload: ImageVector
        @Composable get() = LocalMimoIcons.current.FileDownload
    val FileOpen: ImageVector
        @Composable get() = LocalMimoIcons.current.FileOpen
    val Fingerprint: ImageVector
        @Composable get() = LocalMimoIcons.current.Fingerprint
    val FontDownload: ImageVector
        @Composable get() = LocalMimoIcons.current.FontDownload
    val FormatBold: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatBold
    val FormatIndentIncrease: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatIndentIncrease
    val FormatItalic: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatItalic
    val FormatListBulleted: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatListBulleted
    val FormatListNumbered: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatListNumbered
    val FormatStrikethrough: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatStrikethrough
    val FormatUnderlined: ImageVector
        @Composable get() = LocalMimoIcons.current.FormatUnderlined
    val GraphicEq: ImageVector
        @Composable get() = LocalMimoIcons.current.GraphicEq
    val GridView: ImageVector
        @Composable get() = LocalMimoIcons.current.GridView
    val Highlight: ImageVector
        @Composable get() = LocalMimoIcons.current.Highlight
    val History: ImageVector
        @Composable get() = LocalMimoIcons.current.History
    val Image: ImageVector
        @Composable get() = LocalMimoIcons.current.Image
    val Info: ImageVector
        @Composable get() = LocalMimoIcons.current.Info
    val Inventory2: ImageVector
        @Composable get() = LocalMimoIcons.current.Inventory2
    val Label: ImageVector
        @Composable get() = LocalMimoIcons.current.Label
    val LightMode: ImageVector
        @Composable get() = LocalMimoIcons.current.LightMode
    val Lock: ImageVector
        @Composable get() = LocalMimoIcons.current.Lock
    val LockOpen: ImageVector
        @Composable get() = LocalMimoIcons.current.LockOpen
    val Menu: ImageVector
        @Composable get() = LocalMimoIcons.current.Menu
    val MenuBook: ImageVector
        @Composable get() = LocalMimoIcons.current.MenuBook
    val Mic: ImageVector
        @Composable get() = LocalMimoIcons.current.Mic
    val MoreHoriz: ImageVector
        @Composable get() = LocalMimoIcons.current.MoreHoriz
    val MoreVert: ImageVector
        @Composable get() = LocalMimoIcons.current.MoreVert
    val Notes: ImageVector
        @Composable get() = LocalMimoIcons.current.Notes
    val Notifications: ImageVector
        @Composable get() = LocalMimoIcons.current.Notifications
    val NotificationsActive: ImageVector
        @Composable get() = LocalMimoIcons.current.NotificationsActive
    val NotificationsNone: ImageVector
        @Composable get() = LocalMimoIcons.current.NotificationsNone
    val Palette: ImageVector
        @Composable get() = LocalMimoIcons.current.Palette
    val Pause: ImageVector
        @Composable get() = LocalMimoIcons.current.Pause
    val PhotoCamera: ImageVector
        @Composable get() = LocalMimoIcons.current.PhotoCamera
    val PlayArrow: ImageVector
        @Composable get() = LocalMimoIcons.current.PlayArrow
    val PushPin: ImageVector
        @Composable get() = LocalMimoIcons.current.PushPin
    val Redo: ImageVector
        @Composable get() = LocalMimoIcons.current.Redo
    val Restore: ImageVector
        @Composable get() = LocalMimoIcons.current.Restore
    val Schedule: ImageVector
        @Composable get() = LocalMimoIcons.current.Schedule
    val Search: ImageVector
        @Composable get() = LocalMimoIcons.current.Search
    val SearchOff: ImageVector
        @Composable get() = LocalMimoIcons.current.SearchOff
    val SelectAll: ImageVector
        @Composable get() = LocalMimoIcons.current.SelectAll
    val Settings: ImageVector
        @Composable get() = LocalMimoIcons.current.Settings
    val Share: ImageVector
        @Composable get() = LocalMimoIcons.current.Share
    val Sort: ImageVector
        @Composable get() = LocalMimoIcons.current.Sort
    val StickyNote2: ImageVector
        @Composable get() = LocalMimoIcons.current.StickyNote2
    val Stop: ImageVector
        @Composable get() = LocalMimoIcons.current.Stop
    val Storage: ImageVector
        @Composable get() = LocalMimoIcons.current.Storage
    val SystemUpdate: ImageVector
        @Composable get() = LocalMimoIcons.current.SystemUpdate
    val Title: ImageVector
        @Composable get() = LocalMimoIcons.current.Title
    val Tune: ImageVector
        @Composable get() = LocalMimoIcons.current.Tune
    val Unarchive: ImageVector
        @Composable get() = LocalMimoIcons.current.Unarchive
    val Undo: ImageVector
        @Composable get() = LocalMimoIcons.current.Undo
    val Vibration: ImageVector
        @Composable get() = LocalMimoIcons.current.Vibration
    val ViewAgenda: ImageVector
        @Composable get() = LocalMimoIcons.current.ViewAgenda
    val Visibility: ImageVector
        @Composable get() = LocalMimoIcons.current.Visibility
    val VisibilityOff: ImageVector
        @Composable get() = LocalMimoIcons.current.VisibilityOff
}

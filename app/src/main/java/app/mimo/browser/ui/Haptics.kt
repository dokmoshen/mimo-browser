package app.mimo.browser.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Small semantic layer over platform haptics. Keeping the feedback meanings in
 * one place makes the app feel consistent and lets the settings screen mute
 * every interaction at once.
 */
class MimoHaptics internal constructor(
    private val feedback: HapticFeedback?,
    private val enabled: Boolean,
) {
    fun tap() = perform(HapticFeedbackType.SegmentTick)

    fun press() = perform(HapticFeedbackType.ContextClick)

    fun tick() = perform(HapticFeedbackType.TextHandleMove)

    fun toggle(on: Boolean) = perform(
        if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff,
    )

    fun longPress() = perform(HapticFeedbackType.LongPress)

    fun confirm() = perform(HapticFeedbackType.Confirm)

    fun reject() = perform(HapticFeedbackType.Reject)

    fun gestureEnd() = perform(HapticFeedbackType.GestureEnd)

    private fun perform(type: HapticFeedbackType) {
        if (enabled) feedback?.performHapticFeedback(type)
    }

    companion object {
        val Disabled = MimoHaptics(feedback = null, enabled = false)
    }
}

val LocalMimoHaptics = staticCompositionLocalOf { MimoHaptics.Disabled }

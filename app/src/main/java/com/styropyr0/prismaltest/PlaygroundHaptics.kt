package com.styropyr0.prismaltest

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** App-wide haptics switch, controlled from the Profile tab. */
object PlaygroundHapticsSettings {
    var enabled by mutableStateOf(true)
}

/** Semantic haptic events used by the playground widgets. */
@Stable
class PlaygroundHaptics(private val feedback: HapticFeedback) {
    private fun perform(type: HapticFeedbackType) {
        if (PlaygroundHapticsSettings.enabled) feedback.performHapticFeedback(type)
    }

    /** A discrete selection changed (segments, selector items, stepper). */
    fun selectionTick() = perform(HapticFeedbackType.SegmentTick)

    /** Fine-grained step while scrubbing (ruler, sliders). */
    fun scrubTick() = perform(HapticFeedbackType.SegmentFrequentTick)

    /** A switch flipped. */
    fun toggle(on: Boolean) =
        perform(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)

    /** An action completed (button press). */
    fun confirm() = perform(HapticFeedbackType.Confirm)

    /** A drag began, or hit a boundary. */
    fun gestureStart() = perform(HapticFeedbackType.GestureThresholdActivate)
}

@Composable
fun rememberPlaygroundHaptics(): PlaygroundHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { PlaygroundHaptics(feedback) }
}

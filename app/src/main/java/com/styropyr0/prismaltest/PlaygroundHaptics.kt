package com.styropyr0.prismaltest

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView

object PlaygroundHapticsSettings {
    var enabled by mutableStateOf(true)
}

@Stable
class PlaygroundHaptics(private val view: View) {
    private fun perform(constant: Int) {
        if (PlaygroundHapticsSettings.enabled) view.performHapticFeedback(constant)
    }

    private fun perform(modern: Int, minSdk: Int, fallback: Int) {
        perform(if (Build.VERSION.SDK_INT >= minSdk) modern else fallback)
    }

    fun selectionTick() = perform(
        HapticFeedbackConstants.SEGMENT_TICK,
        Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        HapticFeedbackConstants.CLOCK_TICK
    )

    fun scrubTick() = perform(
        HapticFeedbackConstants.SEGMENT_FREQUENT_TICK,
        Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        HapticFeedbackConstants.CLOCK_TICK
    )

    fun toggle(on: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            perform(
                if (on) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF
            )
        } else {
            perform(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }

    fun confirm() = perform(
        HapticFeedbackConstants.CONFIRM,
        Build.VERSION_CODES.R,
        HapticFeedbackConstants.VIRTUAL_KEY
    )

    fun gestureStart() = perform(
        HapticFeedbackConstants.GESTURE_START,
        Build.VERSION_CODES.R,
        HapticFeedbackConstants.VIRTUAL_KEY
    )
}

@Composable
fun rememberPlaygroundHaptics(): PlaygroundHaptics {
    val view = LocalView.current
    return remember(view) { PlaygroundHaptics(view) }
}

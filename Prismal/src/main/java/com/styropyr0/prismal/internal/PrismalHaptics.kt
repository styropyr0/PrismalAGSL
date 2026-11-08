package com.styropyr0.prismal.internal

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView
import kotlin.math.roundToInt

@Stable
internal class PrismalHaptics(
    private val view: View,
    private val enabled: () -> Boolean,
) {
    private fun perform(modern: Int, minSdk: Int, fallback: Int) {
        if (!enabled()) return
        val constant = if (Build.VERSION.SDK_INT >= minSdk) modern else fallback
        runCatching { view.performHapticFeedback(constant) }
    }

    fun tick() = perform(
        HapticFeedbackConstants.SEGMENT_TICK,
        Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        HapticFeedbackConstants.CLOCK_TICK
    )

    fun scrub() = perform(
        HapticFeedbackConstants.SEGMENT_FREQUENT_TICK,
        Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        HapticFeedbackConstants.CLOCK_TICK
    )

    fun toggle(on: Boolean) = perform(
        if (on) HapticFeedbackConstants.TOGGLE_ON else HapticFeedbackConstants.TOGGLE_OFF,
        Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        HapticFeedbackConstants.VIRTUAL_KEY
    )

    fun confirm() = perform(
        HapticFeedbackConstants.CONFIRM,
        Build.VERSION_CODES.R,
        HapticFeedbackConstants.VIRTUAL_KEY
    )
}

@Composable
internal fun rememberPrismalHaptics(enabled: Boolean): PrismalHaptics {
    val view = LocalView.current
    val enabledState = rememberUpdatedState(enabled)
    return remember(view) { PrismalHaptics(view) { enabledState.value } }
}

internal fun hapticStep(value: Float, start: Float, end: Float, steps: Int): Int {
    val span = (end - start).coerceAtLeast(1e-6f)
    return (((value - start) / span) * steps).roundToInt()
}

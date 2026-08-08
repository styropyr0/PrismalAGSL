package com.styropyr0.prismal.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.styropyr0.prismal.sources.PrismalGlassLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Stable
class PrismalAdaptiveLuminanceState internal constructor(
    initialLuminance: Float
) {
    /** Normalized backdrop brightness in `[0, 1]`. */
    var luminance by mutableFloatStateOf(initialLuminance)
        internal set

    /** Suggested foreground color (black or white) for readable content on the glass. */
    var contentColor by mutableStateOf(
        if (initialLuminance > 0.5f) Color.Black else Color.White
    )
        internal set
}

/**
 * Monitors [source] backdrop layer brightness and exposes adaptive glass tuning values.
 *
 * Pass [enabled] = false to freeze at the neutral midpoint (0.5).
 */
@Composable
fun rememberPrismalAdaptiveLuminance(
    enabled: Boolean,
    source: PrismalGlassLayer,
    isLightTheme: Boolean
): PrismalAdaptiveLuminanceState {
    val initial = if (isLightTheme) 1f else 0f
    val state = remember { PrismalAdaptiveLuminanceState(initial) }

    LaunchedEffect(enabled, source) {
        if (!enabled) {
            state.luminance = 0.5f
            state.contentColor = Color.White
            return@LaunchedEffect
        }

        val buffer = IntArray(25)
        val single = IntArray(1)
        while (isActive) {
            try {
                val imageBitmap = source.graphicsLayer.toImageBitmap()
                val width = imageBitmap.width.coerceAtLeast(1)
                val height = imageBitmap.height.coerceAtLeast(1)
                var index = 0
                for (sy in 0 until 5) {
                    for (sx in 0 until 5) {
                        val x = ((sx + 0.5f) / 5f * width).toInt().coerceIn(0, width - 1)
                        val y = ((sy + 0.5f) / 5f * height).toInt().coerceIn(0, height - 1)
                        imageBitmap.readPixels(
                            buffer = single,
                            startX = x,
                            startY = y,
                            width = 1,
                            height = 1
                        )
                        buffer[index++] = single[0]
                    }
                }
                val averageLuminance =
                    buffer.sumOf { argb ->
                        val r = (argb shr 16 and 0xFF) / 255.0
                        val g = (argb shr 8 and 0xFF) / 255.0
                        val b = (argb and 0xFF) / 255.0
                        0.2126 * r + 0.7152 * g + 0.0722 * b
                    } / buffer.size
                state.luminance = averageLuminance.toFloat()
                state.contentColor = if (averageLuminance > 0.5) Color.Black else Color.White
            } catch (_: Exception) {
            }
            delay(500)
        }
    }

    return state
}

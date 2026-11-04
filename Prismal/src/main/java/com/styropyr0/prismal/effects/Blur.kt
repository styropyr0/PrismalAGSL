package com.styropyr0.prismal.effects

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import com.styropyr0.prismal.PrismalGlassEffectProvider
import com.styropyr0.prismal.PrismalGlassEffectScopeImpl
import com.styropyr0.prismal.isRenderEffectSupported
import kotlin.math.max

/**
 * Applies a Gaussian blur to the backdrop sample.
 *
 * @param radius Blur radius in pixels.
 * @param edgeTreatment Tile mode for pixels outside the sampled region.
 */
fun PrismalGlassEffectProvider.prismalBlur(
    @FloatRange(from = 0.0) radius: Float,
    edgeTreatment: TileMode = TileMode.Clamp
) {
    if (!isRenderEffectSupported()) {
        if (radius > 0f) {
            legacyFrostStrength = max(
                legacyFrostStrength,
                (radius / 24f).coerceIn(0f, 1f) * 0.42f
            )
        }
        return
    }
    if (radius <= 0f) return

    if (edgeTreatment != TileMode.Clamp || renderEffect != null) {
        if (radius > padding) padding = radius
    }

    val impl = this as? PrismalGlassEffectScopeImpl
    impl?.hasBlur = true
    val layerRadius = radius * (impl?.backdropScale ?: 1f)
    renderEffect = BlurEffect(renderEffect, layerRadius, layerRadius, edgeTreatment)
}

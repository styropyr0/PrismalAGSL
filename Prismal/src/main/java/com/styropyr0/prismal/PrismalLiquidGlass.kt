package com.styropyr0.prismal

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.effects.applyPrismalGlassEffects

/**
 * Calibrated liquid glass material preset, mirroring the canonical recipe from the View-system
 * Prismal library.
 *
 * Use [applyBase] as the starting point for custom glass surfaces built with
 * [drawPrismalGlass] or [drawPlainPrismalGlass], then override individual effect helpers.
 * Unsupported optical features are skipped automatically on lower API tiers.
 */
object PrismalLiquidGlass {
    /**
     * Applies the standard blur, vibrancy, and refraction recipe.
     *
     * Refraction and AGSL-only effects are applied only when
     * [PrismalGlass.supportsRefraction] is true.
     */
    fun PrismalGlassEffectProvider.applyBase(
        density: Density,
        adaptiveLuminance: Boolean = false,
        luminance: Float = 0.5f,
    ) {
        applyPrismalGlassEffects(
            density = density,
            adaptiveLuminance = adaptiveLuminance,
            luminance = luminance,
            blurRadiusPx = with(density) { 8.dp.toPx() },
            refractionHeightPx = with(density) { 16.dp.toPx() },
            refractionAmountPx = with(density) { 32.dp.toPx() },
            useVibrancy = true
        )
    }
}

/**
 * Builds a [PrismalGlassEffectProvider] block that starts from [PrismalLiquidGlass.applyBase]
 * and applies optional custom effect configuration.
 *
 * Example:
 * ```
 * drawPrismalGlass(
 *     backdrop = backdrop,
 *     shape = { PrismalRoundedRectangle(24.dp) },
 *     effects = prismalGlassEffects(density) {
 *         colorControls(saturation = 1.8f)
 *     }
 * )
 * ```
 */
fun prismalGlassEffects(
    density: Density,
    adaptiveLuminance: Boolean = false,
    luminance: Float = 0.5f,
    configure: PrismalGlassEffectProvider.() -> Unit = {},
): PrismalGlassEffectProvider.() -> Unit = {
    with(PrismalLiquidGlass) {
        applyBase(
            density = density,
            adaptiveLuminance = adaptiveLuminance,
            luminance = luminance
        )
    }
    configure()
}

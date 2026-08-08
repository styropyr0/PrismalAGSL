package com.styropyr0.prismal.effects

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.styropyr0.prismal.PrismalGlass
import com.styropyr0.prismal.PrismalGlassEffectProvider
import kotlin.math.sign

/**
 * Applies the standard Prismal glass effect chain used by glass components.
 *
 * Combines adaptive or manual blur, optional vibrancy / color controls, and edge lens
 * refraction into a single [PrismalGlassEffectProvider] block.
 *
 * @param density Current density for dp-to-px conversion.
 * @param adaptiveLuminance When true, blur and brightness are driven by [luminance].
 * @param luminance Normalized backdrop brightness in `[0, 1]` (used when adaptive).
 * @param blurRadiusPx Manual blur radius when adaptive mode is off.
 * @param refractionHeightPx Lens zone height at the shape edge.
 * @param refractionAmountPx Lens displacement strength.
 * @param brightness Manual brightness offset when adaptive mode is off.
 * @param saturation Manual saturation when adaptive mode is off.
 * @param depthEffect Passed through to [prismalLens].
 * @param chromaticAberration Passed through to [prismalLens].
 * @param useVibrancy When true and not adaptive, applies [vibrancy] instead of [colorControls].
 */
fun PrismalGlassEffectProvider.applyPrismalGlassEffects(
    density: Density,
    adaptiveLuminance: Boolean,
    luminance: Float,
    blurRadiusPx: Float,
    refractionHeightPx: Float,
    refractionAmountPx: Float,
    brightness: Float = 0f,
    saturation: Float = 1.5f,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false,
    useVibrancy: Boolean = true
) {
    if (adaptiveLuminance) {
        val l = (luminance * 2f - 1f).let { sign(it) * it * it }
        colorControls(
            brightness =
                if (l > 0f) lerp(0.1f, 0.5f, l)
                else lerp(0.1f, -0.2f, -l),
            contrast =
                if (l > 0f) lerp(1f, 0f, l)
                else 1f,
            saturation = 1.5f
        )
        val baseBlur = with(density) { 8.dp.toPx() }
        val maxBlur = with(density) { 16.dp.toPx() }
        val minBlur = with(density) { 2.dp.toPx() }
        prismalBlur(
            if (l > 0f) lerp(baseBlur, maxBlur, l)
            else lerp(baseBlur, minBlur, -l)
        )
    } else {
        if (useVibrancy) {
            vibrancy()
        } else {
            colorControls(brightness = brightness, saturation = saturation)
        }
        if (blurRadiusPx > 0f) {
            prismalBlur(blurRadiusPx)
        }
    }

    if (PrismalGlass.supportsRefraction && refractionHeightPx > 0f && refractionAmountPx > 0f) {
        prismalLens(
            refractionHeight = refractionHeightPx,
            refractionAmount = refractionAmountPx,
            depthEffect = depthEffect,
            chromaticAberration = chromaticAberration
        )
    }
}

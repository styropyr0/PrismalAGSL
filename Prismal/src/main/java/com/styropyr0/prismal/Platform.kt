package com.styropyr0.prismal

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Active rendering tier selected automatically from the device API level. */
enum class PrismalGlassPipeline {
    /** API 25–30: sampled backdrop with a frosted surface treatment. */
    Legacy,
    /** API 31–32: backdrop blur and color filters via [androidx.compose.ui.graphics.RenderEffect]. */
    Standard,
    /** API 33+: full liquid glass with AGSL refraction and shader effects. */
    Liquid
}

private var pipelineOverrideState by mutableStateOf<PrismalGlassPipeline?>(null)

private fun allowsPipeline(tier: PrismalGlassPipeline): Boolean {
    val override = pipelineOverrideState ?: return true
    return override.ordinal >= tier.ordinal
}

/**
 * Returns `true` when [androidx.compose.ui.graphics.RenderEffect] is available (API 31+)
 * and not disabled by [PrismalGlass.pipelineOverride].
 */
@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
fun isRenderEffectSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && allowsPipeline(PrismalGlassPipeline.Standard)

/**
 * Returns `true` when AGSL runtime shaders are available (API 33+) and not disabled by
 * [PrismalGlass.pipelineOverride].
 */
@ChecksSdkIntAtLeast(Build.VERSION_CODES.TIRAMISU)
fun isAGSLShaderSupported(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && allowsPipeline(PrismalGlassPipeline.Liquid)

/** Platform capability checks and pipeline selection for the Prismal glass stack. */
object PrismalGlass {

    /** Minimum supported SDK for the library (Android 7.0). */
    const val MIN_SDK = 25

    /** Highest pipeline tier this device's API level supports, ignoring [pipelineOverride]. */
    val devicePipeline: PrismalGlassPipeline
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> PrismalGlassPipeline.Liquid
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PrismalGlassPipeline.Standard
            else -> PrismalGlassPipeline.Legacy
        }

    /** Pipeline tiers that can run on this device, lowest first. */
    val availablePipelines: List<PrismalGlassPipeline>
        get() = PrismalGlassPipeline.entries.filter { it.ordinal <= devicePipeline.ordinal }

    /**
     * Forces a lower pipeline tier than the device supports, e.g. to preview how glass
     * degrades on older Android versions. `null` (default) uses [devicePipeline].
     *
     * The override can only lower the tier: requesting a tier above [devicePipeline] has
     * no effect beyond it. Some components build shader objects once, so recreate the glass
     * UI after changing this (for example by wrapping it in `key(PrismalGlass.pipeline)`).
     */
    var pipelineOverride: PrismalGlassPipeline?
        get() = pipelineOverrideState
        set(value) {
            pipelineOverrideState = value
        }

    /**
     * Returns the pipeline tier in use on this device, after [pipelineOverride].
     *
     * Callers do not need to branch on this for normal glass usage; effect helpers skip
     * unsupported features automatically.
     */
    val pipeline: PrismalGlassPipeline
        get() = when {
            isAGSLShaderSupported() -> PrismalGlassPipeline.Liquid
            isRenderEffectSupported() -> PrismalGlassPipeline.Standard
            else -> PrismalGlassPipeline.Legacy
        }

    /** Glass rendering is available on API [MIN_SDK]+ with automatic tier degradation. */
    @ChecksSdkIntAtLeast(api = MIN_SDK)
    @SuppressLint("ObsoleteSdkInt")
    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= MIN_SDK

    /** Backdrop blur via RenderEffect (API 31+). */
    val supportsBackdropBlur: Boolean
        get() = pipeline != PrismalGlassPipeline.Legacy

    /** Edge lens refraction and dispersion shaders (API 33+). */
    val supportsRefraction: Boolean
        get() = pipeline == PrismalGlassPipeline.Liquid

    /** Custom AGSL shader effects such as gradient glass (API 33+). */
    val supportsAgslEffects: Boolean
        get() = pipeline == PrismalGlassPipeline.Liquid
}

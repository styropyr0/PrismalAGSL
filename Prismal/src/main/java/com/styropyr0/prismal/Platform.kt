package com.styropyr0.prismal

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/** Returns `true` when [androidx.compose.ui.graphics.RenderEffect] is available (API 31+). */
@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
fun isRenderEffectSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Returns `true` when AGSL runtime shaders are available (API 33+). */
@ChecksSdkIntAtLeast(Build.VERSION_CODES.TIRAMISU)
fun isAGSLShaderSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/** Platform capability checks for the Prismal AGSL glass stack. */
object PrismalGlass {

    /** Returns `true` when the full Prismal AGSL pipeline can run on this device. */
    fun isSupported(): Boolean = isAGSLShaderSupported()
}

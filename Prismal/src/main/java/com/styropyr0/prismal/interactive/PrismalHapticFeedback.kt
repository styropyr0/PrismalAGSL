package com.styropyr0.prismal.interactive

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Default for the `hapticFeedback` parameter of every Prismal component. Provide `false` once near
 * the root of a composition to turn haptics off for all components below it.
 */
val LocalPrismalHapticFeedback: ProvidableCompositionLocal<Boolean> = compositionLocalOf { true }

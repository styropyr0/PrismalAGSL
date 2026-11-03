package com.styropyr0.prismaltest

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Whether this device can comfortably afford Prismal's higher-quality rendering paths
 * (e.g. per-frame relayout of glass droplets instead of cheap layer scaling).
 *
 * A device qualifies when it either declares a media performance class (Android 12+,
 * mid/high-end devices only) or has a high-end hardware profile: not a low-RAM device,
 * at least 6 GB of RAM and 8 CPU cores, on Android 13+ (where AGSL glass runs).
 */
@Composable
fun rememberIsHighPerformanceDevice(): Boolean {
    val context = LocalContext.current
    return remember(context) { isHighPerformanceDevice(context) }
}

fun isHighPerformanceDevice(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        Build.VERSION.MEDIA_PERFORMANCE_CLASS >= Build.VERSION_CODES.S
    ) {
        return true
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false

    val activityManager = context.getSystemService(ActivityManager::class.java) ?: return false
    if (activityManager.isLowRamDevice) return false

    val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
    val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
    val cores = Runtime.getRuntime().availableProcessors()

    return totalRamGb >= 5.5 && cores >= 8
}

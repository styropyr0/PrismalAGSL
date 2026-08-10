package com.styropyr0.prismaltest

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File

object GlassPlaygroundStorage {
    private const val PREFS_NAME = "glass_playground"
    private const val WALLPAPER_FILE_NAME = "playground_wallpaper"

    private const val KEY_BLUR_RADIUS = "blur_radius"
    private const val KEY_REFRACTION_HEIGHT = "refraction_height"
    private const val KEY_REFRACTION_AMOUNT = "refraction_amount"
    private const val KEY_BRIGHTNESS = "brightness"
    private const val KEY_SATURATION = "saturation"
    private const val KEY_CHROMATIC_ABERRATION = "chromatic_aberration"
    private const val KEY_CORNER_RADIUS = "corner_radius"
    private const val KEY_DEPTH_EFFECT = "depth_effect"
    private const val KEY_ADAPTIVE_LUMINANCE = "adaptive_luminance"
    private const val KEY_USE_VIBRANCY = "use_vibrancy"
    private const val KEY_SPECULAR_ENABLED = "specular_enabled"
    private const val KEY_SPECULAR_ALPHA = "specular_alpha"
    private const val KEY_SPECULAR_WIDTH = "specular_width"
    private const val KEY_SPECULAR_STYLE = "specular_style"
    private const val KEY_DEPTH_SHADOW_ENABLED = "depth_shadow_enabled"
    private const val KEY_DEPTH_SHADOW_RADIUS = "depth_shadow_radius"
    private const val KEY_DEPTH_SHADOW_ALPHA = "depth_shadow_alpha"
    private const val KEY_DEPTH_INSET_ENABLED = "depth_inset_enabled"
    private const val KEY_DEPTH_INSET_RADIUS = "depth_inset_radius"
    private const val KEY_DEPTH_INSET_ALPHA = "depth_inset_alpha"
    private const val KEY_SURFACE_TINT_ALPHA = "surface_tint_alpha"
    private const val KEY_GRADIENT_BLUR_FADE_END = "gradient_blur_fade_end"
    private const val KEY_GRADIENT_BOTTOM_WEIGHT = "gradient_bottom_weight"

    fun load(context: Context, params: GlassPlaygroundParams) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains(KEY_BLUR_RADIUS)) return

        params.blurRadiusDp = prefs.getFloat(KEY_BLUR_RADIUS, params.blurRadiusDp)
        params.refractionHeightDp = prefs.getFloat(KEY_REFRACTION_HEIGHT, params.refractionHeightDp)
        params.refractionAmountDp = prefs.getFloat(KEY_REFRACTION_AMOUNT, params.refractionAmountDp)
        params.brightness = prefs.getFloat(KEY_BRIGHTNESS, params.brightness)
        params.saturation = prefs.getFloat(KEY_SATURATION, params.saturation)
        params.chromaticAberration = prefs.getFloat(KEY_CHROMATIC_ABERRATION, params.chromaticAberration)
        params.cornerRadiusDp = prefs.getFloat(KEY_CORNER_RADIUS, params.cornerRadiusDp)
        params.depthEffect = prefs.getBoolean(KEY_DEPTH_EFFECT, params.depthEffect)
        params.adaptiveLuminance = prefs.getBoolean(KEY_ADAPTIVE_LUMINANCE, params.adaptiveLuminance)
        params.useVibrancy = prefs.getBoolean(KEY_USE_VIBRANCY, params.useVibrancy)
        params.specularEnabled = prefs.getBoolean(KEY_SPECULAR_ENABLED, params.specularEnabled)
        params.specularAlpha = prefs.getFloat(KEY_SPECULAR_ALPHA, params.specularAlpha)
        params.specularWidthDp = prefs.getFloat(KEY_SPECULAR_WIDTH, params.specularWidthDp)
        params.specularStyle = SpecularStyleOption.entries.getOrElse(
            prefs.getInt(KEY_SPECULAR_STYLE, SpecularStyleOption.Default.ordinal)
        ) { SpecularStyleOption.Default }
        params.depthShadowEnabled = prefs.getBoolean(KEY_DEPTH_SHADOW_ENABLED, params.depthShadowEnabled)
        params.depthShadowRadiusDp = prefs.getFloat(KEY_DEPTH_SHADOW_RADIUS, params.depthShadowRadiusDp)
        params.depthShadowAlpha = prefs.getFloat(KEY_DEPTH_SHADOW_ALPHA, params.depthShadowAlpha)
        params.depthInsetEnabled = prefs.getBoolean(KEY_DEPTH_INSET_ENABLED, params.depthInsetEnabled)
        params.depthInsetRadiusDp = prefs.getFloat(KEY_DEPTH_INSET_RADIUS, params.depthInsetRadiusDp)
        params.depthInsetAlpha = prefs.getFloat(KEY_DEPTH_INSET_ALPHA, params.depthInsetAlpha)
        params.surfaceTintAlpha = prefs.getFloat(KEY_SURFACE_TINT_ALPHA, params.surfaceTintAlpha)
        params.gradientBlurFadeEnd = prefs.getFloat(KEY_GRADIENT_BLUR_FADE_END, params.gradientBlurFadeEnd)
        params.gradientBottomWeight = prefs.getFloat(KEY_GRADIENT_BOTTOM_WEIGHT, params.gradientBottomWeight)
    }

    fun save(context: Context, params: GlassPlaygroundParams) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BLUR_RADIUS, params.blurRadiusDp)
            .putFloat(KEY_REFRACTION_HEIGHT, params.refractionHeightDp)
            .putFloat(KEY_REFRACTION_AMOUNT, params.refractionAmountDp)
            .putFloat(KEY_BRIGHTNESS, params.brightness)
            .putFloat(KEY_SATURATION, params.saturation)
            .putFloat(KEY_CHROMATIC_ABERRATION, params.chromaticAberration)
            .putFloat(KEY_CORNER_RADIUS, params.cornerRadiusDp)
            .putBoolean(KEY_DEPTH_EFFECT, params.depthEffect)
            .putBoolean(KEY_ADAPTIVE_LUMINANCE, params.adaptiveLuminance)
            .putBoolean(KEY_USE_VIBRANCY, params.useVibrancy)
            .putBoolean(KEY_SPECULAR_ENABLED, params.specularEnabled)
            .putFloat(KEY_SPECULAR_ALPHA, params.specularAlpha)
            .putFloat(KEY_SPECULAR_WIDTH, params.specularWidthDp)
            .putInt(KEY_SPECULAR_STYLE, params.specularStyle.ordinal)
            .putBoolean(KEY_DEPTH_SHADOW_ENABLED, params.depthShadowEnabled)
            .putFloat(KEY_DEPTH_SHADOW_RADIUS, params.depthShadowRadiusDp)
            .putFloat(KEY_DEPTH_SHADOW_ALPHA, params.depthShadowAlpha)
            .putBoolean(KEY_DEPTH_INSET_ENABLED, params.depthInsetEnabled)
            .putFloat(KEY_DEPTH_INSET_RADIUS, params.depthInsetRadiusDp)
            .putFloat(KEY_DEPTH_INSET_ALPHA, params.depthInsetAlpha)
            .putFloat(KEY_SURFACE_TINT_ALPHA, params.surfaceTintAlpha)
            .putFloat(KEY_GRADIENT_BLUR_FADE_END, params.gradientBlurFadeEnd)
            .putFloat(KEY_GRADIENT_BOTTOM_WEIGHT, params.gradientBottomWeight)
            .apply()
    }

    fun loadBackgroundImageUri(context: Context): Uri? {
        val wallpaperFile = wallpaperFile(context)
        return wallpaperFile.takeIf { it.exists() }?.toUri()
    }

    fun saveBackgroundImage(context: Context, sourceUri: Uri): Uri? {
        return try {
            val wallpaperFile = wallpaperFile(context)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                wallpaperFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            wallpaperFile.toUri()
        } catch (_: Exception) {
            null
        }
    }

    private fun wallpaperFile(context: Context): File {
        return File(context.filesDir, WALLPAPER_FILE_NAME)
    }
}

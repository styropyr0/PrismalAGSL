package com.styropyr0.prismaltest

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val GeistFontFamily = FontFamily(
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold)
)

private val GeistTypography: Typography
    get() {
        val baseline = Typography()
        return Typography(
            displayLarge = baseline.displayLarge.copy(fontFamily = GeistFontFamily),
            displayMedium = baseline.displayMedium.copy(fontFamily = GeistFontFamily),
            displaySmall = baseline.displaySmall.copy(fontFamily = GeistFontFamily),
            headlineLarge = baseline.headlineLarge.copy(fontFamily = GeistFontFamily),
            headlineMedium = baseline.headlineMedium.copy(fontFamily = GeistFontFamily),
            headlineSmall = baseline.headlineSmall.copy(fontFamily = GeistFontFamily),
            titleLarge = baseline.titleLarge.copy(fontFamily = GeistFontFamily),
            titleMedium = baseline.titleMedium.copy(fontFamily = GeistFontFamily),
            titleSmall = baseline.titleSmall.copy(fontFamily = GeistFontFamily),
            bodyLarge = baseline.bodyLarge.copy(fontFamily = GeistFontFamily),
            bodyMedium = baseline.bodyMedium.copy(fontFamily = GeistFontFamily),
            bodySmall = baseline.bodySmall.copy(fontFamily = GeistFontFamily),
            labelLarge = baseline.labelLarge.copy(fontFamily = GeistFontFamily),
            labelMedium = baseline.labelMedium.copy(fontFamily = GeistFontFamily),
            labelSmall = baseline.labelSmall.copy(fontFamily = GeistFontFamily)
        )
    }

@Composable
fun PrismalTestTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme(),
        typography = GeistTypography,
        content = content
    )
}

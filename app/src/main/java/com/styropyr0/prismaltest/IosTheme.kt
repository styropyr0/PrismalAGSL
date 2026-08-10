package com.styropyr0.prismaltest

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class IosColors(
    val label: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val quaternaryLabel: Color,
    val systemBlue: Color,
    val systemRed: Color,
    val systemGreen: Color,
    val separator: Color,
    val groupedBackground: Color,
    val secondaryGroupedBackground: Color,
    val fillTertiary: Color,
    val searchFieldBackground: Color,
)

val LocalIosColors = staticCompositionLocalOf<IosColors> {
    error("IosColors not provided")
}

object IosTheme {
    val colors: IosColors
        @Composable get() = LocalIosColors.current

    val largeTitle: TextStyle
        @Composable get() = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 34.sp,
            lineHeight = 41.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.37.sp
        )

    val title2: TextStyle
        @Composable get() = MaterialTheme.typography.titleLarge.copy(
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )

    val headline: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold
        )

    val body: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal
        )

    val callout: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 16.sp,
            lineHeight = 21.sp
        )

    val subheadline: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium.copy(
            fontSize = 15.sp,
            lineHeight = 20.sp
        )

    val footnote: TextStyle
        @Composable get() = MaterialTheme.typography.bodySmall.copy(
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

    val caption1: TextStyle
        @Composable get() = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

    val horizontalSelectorLabel: TextStyle
        @Composable get() = caption1.copy(
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )

    val sectionHeader: TextStyle
        @Composable get() = footnote.copy(
            fontWeight = FontWeight.Normal,
            letterSpacing = (-0.08).sp
        )

    val tabLabel: TextStyle
        @Composable get() = caption1.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
}

@Composable
fun iosColors(darkTheme: Boolean = isSystemInDarkTheme()): IosColors =
    if (darkTheme) {
        IosColors(
            label = Color(0xFFFFFFFF),
            secondaryLabel = Color(0x99EBEBF5),
            tertiaryLabel = Color(0x4DEBEBF5),
            quaternaryLabel = Color(0x2EEBEBF5),
            systemBlue = Color(0xFF0A84FF),
            systemRed = Color(0xFFFF453A),
            systemGreen = Color(0xFF30D158),
            separator = Color(0x99545458),
            groupedBackground = Color(0xFF000000),
            secondaryGroupedBackground = Color(0xFF1C1C1E),
            fillTertiary = Color(0xFF2C2C2E),
            searchFieldBackground = Color(0xFF1C1C1E)
        )
    } else {
        IosColors(
            label = Color(0xFF000000),
            secondaryLabel = Color(0x993C3C43),
            tertiaryLabel = Color(0x4D3C3C43),
            quaternaryLabel = Color(0x2E3C3C43),
            systemBlue = Color(0xFF1485FF),
            systemRed = Color(0xFFFF3B30),
            systemGreen = Color(0xFF34C759),
            separator = Color(0x493C3C43),
            groupedBackground = Color(0xFFF2F2F7),
            secondaryGroupedBackground = Color(0xFFFFFFFF),
            fillTertiary = Color(0xFFE5E5EA),
            searchFieldBackground = Color(0xFFE5E5EA)
        )
    }

@Composable
fun PrismalTestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val ios = iosColors(darkTheme)
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = ios.systemBlue,
            onPrimary = Color.White,
            background = ios.groupedBackground,
            surface = ios.secondaryGroupedBackground,
            onSurface = ios.label,
            onSurfaceVariant = ios.secondaryLabel,
            outline = ios.separator
        )
    } else {
        lightColorScheme(
            primary = ios.systemBlue,
            onPrimary = Color.White,
            background = ios.groupedBackground,
            surface = ios.secondaryGroupedBackground,
            onSurface = ios.label,
            onSurfaceVariant = ios.secondaryLabel,
            outline = ios.separator
        )
    }

    val baseline = Typography()
    val typography = Typography(
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

    CompositionLocalProvider(LocalIosColors provides ios) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

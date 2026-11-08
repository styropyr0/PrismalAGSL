package com.styropyr0.prismaltest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.styropyr0.prismal.interactive.LocalPrismalHapticFeedback

class CatalogPlaygroundActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrismalTestTheme {
                CompositionLocalProvider(
                    LocalPrismalHapticFeedback provides PlaygroundHapticsSettings.enabled
                ) {
                    CatalogPlaygroundScreen()
                }
            }
        }
    }
}

package com.styropyr0.prismaltest

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

class BackgroundImageState internal constructor(
    initialUri: Uri?,
) {
    var uri by mutableStateOf(initialUri)
        private set

    var generation by mutableIntStateOf(0)
        private set

    fun update(context: Context, sourceUri: Uri) {
        uri = persistBackgroundImage(context, sourceUri) ?: sourceUri
        generation++
    }
}

@Composable
fun rememberBackgroundImageState(): BackgroundImageState {
    val context = LocalContext.current
    return remember {
        BackgroundImageState(GlassPlaygroundStorage.loadBackgroundImageUri(context))
    }
}

fun persistBackgroundImage(context: Context, sourceUri: Uri): Uri? {
    return GlassPlaygroundStorage.saveBackgroundImage(context, sourceUri)
}

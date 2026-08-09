package com.styropyr0.prismaltest

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle

@Composable
fun TunableGlassPanel(
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    PlaygroundGlassSurface(
        backdrop = backdrop,
        params = params,
        luminance = luminance,
        shape = { PrismalRoundedRectangle(params.cornerRadiusDp.dp) },
        modifier = modifier,
        content = content
    )
}

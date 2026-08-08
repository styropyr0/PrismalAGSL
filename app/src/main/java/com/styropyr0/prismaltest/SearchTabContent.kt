package com.styropyr0.prismaltest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalGlassSurface
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.sources.PrismalGlassLayer

private val searchSuggestions = listOf(
    "Glass buttons",
    "Gradient panels",
    "Chromatic aberration",
    "Adaptive luminance",
    "Specular highlights",
    "Bottom tabs"
)

@Composable
fun SearchTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val density = LocalDensity.current
    var query by remember { mutableStateOf("") }

    val filteredResults = remember(query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            searchSuggestions.filter { it.contains(query, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Search",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        IosGlassSearchBar(
            backdrop = backdrop,
            glassParams = glassParams,
            luminance = luminance,
            query = query,
            onQueryChange = { query = it },
            onClear = { query = "" }
        )

        if (query.isNotBlank()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (filteredResults.isEmpty()) {
                    item {
                        Text(
                            text = "No results for \"$query\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                } else {
                    items(filteredResults) { result ->
                        PrismalGlassSurface(
                            backdrop = backdrop,
                            shape = { PrismalCapsule() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            adaptiveLuminance = glassParams.adaptiveLuminance,
                            luminance = luminance,
                            brightness = glassParams.brightness,
                            saturation = glassParams.saturation,
                            refractionHeightPx = with(density) { glassParams.refractionHeightDp.dp.toPx() },
                            refractionAmountPx = with(density) { glassParams.refractionAmountDp.dp.toPx() },
                            depthEffect = glassParams.depthEffect,
                            chromaticAberration = glassParams.chromaticAberration,
                            content = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                    )
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IosGlassSearchBar(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val density = LocalDensity.current
    val placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val textColor = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PrismalGlassSurface(
            backdrop = backdrop,
            shape = { PrismalCapsule() },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            adaptiveLuminance = glassParams.adaptiveLuminance,
            luminance = luminance,
            brightness = glassParams.brightness,
            saturation = glassParams.saturation,
            refractionHeightPx = with(density) { glassParams.refractionHeightDp.dp.toPx() },
            refractionAmountPx = with(density) { glassParams.refractionAmountDp.dp.toPx() },
            depthEffect = glassParams.depthEffect,
            chromaticAberration = glassParams.chromaticAberration,
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = placeholderColor
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = placeholderColor
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                modifier = Modifier.size(18.dp),
                                tint = placeholderColor
                            )
                        }
                    }
                }
            }
        )

        if (query.isNotEmpty()) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable(onClick = onClear)
            )
        }
    }
}

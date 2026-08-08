package com.styropyr0.prismaltest

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.sources.PrismalGlassLayer

private val searchSuggestions = listOf(
    "Glass Buttons",
    "Gradient Panels",
    "Chromatic Aberration",
    "Adaptive Luminance",
    "Specular Highlights",
    "Bottom Tabs"
)

@Composable
fun SearchTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var query by remember { mutableStateOf("") }

    val filteredResults = remember(query) {
        if (query.isBlank()) {
            emptyList()
        } else {
            searchSuggestions.filter { it.contains(query, ignoreCase = true) }
        }
    }

    IosGroupedScreen(modifier = modifier, contentPadding = contentPadding) {
        item {
            IosLargeTitle(title = "Search")
        }

        item {
            IosGlassSearchField(
                backdrop = backdrop,
                glassParams = glassParams,
                luminance = luminance,
                query = query,
                onQueryChange = { query = it },
                onClear = { query = "" }
            )
        }

        if (query.isBlank()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 32.dp, end = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = IosTheme.colors.tertiaryLabel
                    )
                    Text(
                        text = "Search Prismal",
                        style = IosTheme.headline,
                        color = IosTheme.colors.secondaryLabel
                    )
                    Text(
                        text = "Find components and effects",
                        style = IosTheme.subheadline,
                        color = IosTheme.colors.tertiaryLabel
                    )
                }
            }
        } else if (filteredResults.isEmpty()) {
            item {
                Text(
                    text = "No Results for \"$query\"",
                    style = IosTheme.headline,
                    color = IosTheme.colors.secondaryLabel,
                    modifier = Modifier.padding(
                        start = IosLayout.screenHorizontal,
                        top = 24.dp
                    )
                )
            }
        } else {
            item {
                IosSectionHeader("Results")
                IosGlassGroup(backdrop = backdrop, params = glassParams, luminance = luminance) {
                    filteredResults.forEachIndexed { index, result ->
                        if (index > 0) IosGroupDivider()
                        IosListRow(
                            title = result,
                            showChevron = true,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IosGlassSearchField(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IosLayout.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaygroundGlassSurface(
            backdrop = backdrop,
            params = glassParams,
            luminance = luminance,
            shape = { PrismalCapsule() },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = IosTheme.colors.secondaryLabel
                    )
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = IosTheme.body.copy(color = IosTheme.colors.label),
                        cursorBrush = SolidColor(IosTheme.colors.systemBlue),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search",
                                    style = IosTheme.body,
                                    color = IosTheme.colors.secondaryLabel
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(onClick = onClear),
                            tint = IosTheme.colors.secondaryLabel
                        )
                    }
                }
            }
        )

        if (query.isNotEmpty()) {
            Text(
                text = "Cancel",
                style = IosTheme.body,
                color = IosTheme.colors.systemBlue,
                modifier = Modifier.clickable(onClick = onClear)
            )
        }
    }

    Spacer(Modifier.height(4.dp))
}

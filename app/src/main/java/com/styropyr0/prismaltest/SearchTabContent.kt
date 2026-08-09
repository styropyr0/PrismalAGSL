package com.styropyr0.prismaltest

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
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
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isSearchActive = isFocused || query.isNotEmpty()
    val searchAnimationSpec = spring<Float>(dampingRatio = 0.84f, stiffness = 420f)
    val cancelWidth = 56.dp
    val cancelGap = 10.dp
    val cancelSpace by animateDpAsState(
        targetValue = if (isSearchActive) cancelWidth + cancelGap else 0.dp,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 420f),
        label = "searchCancelSpace"
    )
    val cancelAlpha by animateFloatAsState(
        targetValue = if (isSearchActive) 1f else 0f,
        animationSpec = searchAnimationSpec,
        label = "searchCancelAlpha"
    )
    val fieldScale by animateFloatAsState(
        targetValue = if (isSearchActive) 0.985f else 1f,
        animationSpec = searchAnimationSpec,
        label = "searchFieldScale"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = IosLayout.screenHorizontal)
    ) {
        val searchWidth = maxWidth - cancelSpace

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaygroundGlassSurface(
                backdrop = backdrop,
                params = glassParams,
                luminance = luminance,
                shape = { PrismalCapsule() },
                modifier = Modifier
                    .width(searchWidth)
                    .height(48.dp)
                    .graphicsLayer {
                        scaleX = fieldScale
                        scaleY = fieldScale
                    },
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
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { isFocused = it.isFocused },
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

            Box(
                modifier = Modifier
                    .width(cancelSpace)
                    .graphicsLayer { alpha = cancelAlpha },
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Cancel",
                    style = IosTheme.body,
                    color = IosTheme.colors.systemBlue,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier
                        .width(cancelWidth)
                        .clickable(enabled = isSearchActive) {
                            onClear()
                            focusManager.clearFocus()
                        }
                )
            }
        }
    }

    Spacer(Modifier.height(4.dp))
}

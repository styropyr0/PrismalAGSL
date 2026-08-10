package com.styropyr0.prismaltest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.components.LocalPrismalParentGlassLayer
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer

object IosLayout {
    val screenHorizontal = 16.dp
    val sectionSpacing = 22.dp
    val groupCorner = 22.dp
    val rowMinHeight = 50.dp
    val rowHorizontalPadding = 20.dp
    val rowVerticalPadding = 14.dp
    val groupInnerPadding = 20.dp
    val separatorLeadingInset = 20.dp
    val largeTitleBottom = 8.dp
}

@Composable
fun IosLargeTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = IosLayout.screenHorizontal,
                end = IosLayout.screenHorizontal,
                bottom = IosLayout.largeTitleBottom
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = IosTheme.largeTitle,
            color = IosTheme.colors.label
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = IosTheme.subheadline,
                color = IosTheme.colors.secondaryLabel
            )
        }
    }
}

@Composable
fun IosSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = IosTheme.sectionHeader,
        color = IosTheme.colors.secondaryLabel,
        modifier = modifier.padding(
            start = IosLayout.screenHorizontal,
            end = IosLayout.screenHorizontal,
            bottom = 6.dp
        )
    )
}

@Composable
fun IosSectionFooter(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = IosTheme.footnote,
        color = IosTheme.colors.secondaryLabel,
        modifier = modifier.padding(
            start = IosLayout.screenHorizontal,
            end = IosLayout.screenHorizontal,
            top = 8.dp
        )
    )
}

@Composable
fun IosGroupedScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 24.dp),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(IosLayout.sectionSpacing),
        content = content
    )
}

@Composable
fun IosGlassGroup(
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val groupGlassLayer = rememberPrismalGlassLayer()
    CompositionLocalProvider(LocalPrismalParentGlassLayer provides groupGlassLayer) {
        PlaygroundGlassSurface(
            backdrop = backdrop,
            params = params,
            luminance = luminance,
            shape = { PrismalRoundedRectangle(params.cornerRadiusDp.dp) },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = IosLayout.screenHorizontal),
            nestedGlassSource = groupGlassLayer,
            content = { Column(content = content) }
        )
    }
}

@Composable
fun IosPlainGroup(
    modifier: Modifier = Modifier,
    background: Color = IosTheme.colors.secondaryGroupedBackground.copy(alpha = 0.72f),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = IosLayout.screenHorizontal)
            .clip(RoundedCornerShape(IosLayout.groupCorner))
            .background(background),
        content = content
    )
}

@Composable
fun IosGroupDivider(showLeadingInset: Boolean = true) {
    HorizontalDivider(
        modifier = Modifier.padding(
            start = if (showLeadingInset) IosLayout.separatorLeadingInset else 0.dp
        ),
        thickness = 0.5.dp,
        color = IosTheme.colors.separator
    )
}

@Composable
fun IosListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    showChevron: Boolean = false,
    titleColor: Color = IosTheme.colors.label,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .heightIn(min = IosLayout.rowMinHeight)
            .padding(
                horizontal = IosLayout.rowHorizontalPadding,
                vertical = IosLayout.rowVerticalPadding
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, style = IosTheme.body, color = titleColor)
            if (subtitle != null) {
                Text(text = subtitle, style = IosTheme.footnote, color = IosTheme.colors.secondaryLabel)
            }
        }
        if (value != null) {
            Text(
                text = value,
                style = IosTheme.body,
                color = IosTheme.colors.secondaryLabel
            )
            Spacer(Modifier.width(6.dp))
        }
        trailing?.invoke(this)
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = IosTheme.colors.tertiaryLabel,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun IosToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    IosListRow(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        trailing = {
            com.styropyr0.prismal.components.PrismalGlassToggle(
                selected = { checked },
                onSelect = onCheckedChange,
                backdrop = backdrop
            )
        }
    )
}

@Composable
fun IosSliderRow(
    title: String,
    value: Float,
    valueLabel: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val labelColor = if (enabled) IosTheme.colors.label else IosTheme.colors.tertiaryLabel
    val valueColor = if (enabled) IosTheme.colors.secondaryLabel else IosTheme.colors.tertiaryLabel

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.45f)
            .padding(
                horizontal = IosLayout.rowHorizontalPadding,
                vertical = IosLayout.rowVerticalPadding
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = IosTheme.body, color = labelColor)
            Text(valueLabel, style = IosTheme.body, color = valueColor)
        }
        Box(Modifier.fillMaxWidth()) {
            PrismalGlassSlider(
                value = { value },
                onValueChange = if (enabled) onValueChange else { _ -> },
                valueRange = valueRange,
                visibilityThreshold = (valueRange.endInclusive - valueRange.start) * 0.005f,
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun IosDestructiveButton(
    text: String,
    onClick: () -> Unit,
    backdrop: PrismalBackdrop,
    params: GlassPlaygroundParams,
    luminance: () -> Float,
    modifier: Modifier = Modifier
) {
    PlaygroundGlassSurface(
        backdrop = backdrop,
        params = params,
        luminance = luminance,
        shape = { PrismalRoundedRectangle(params.cornerRadiusDp.dp) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = IosLayout.screenHorizontal),
        onClick = onClick,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IosLayout.rowMinHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = IosTheme.body,
                    color = IosTheme.colors.systemRed,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

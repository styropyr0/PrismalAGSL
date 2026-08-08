package com.styropyr0.prismaltest

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.styropyr0.prismal.PrismalGlassSurface
import com.styropyr0.prismal.components.PrismalGlassBottomTab
import com.styropyr0.prismal.components.PrismalGlassBottomTabs
import com.styropyr0.prismal.components.PrismalGlassButton
import com.styropyr0.prismal.components.PrismalGlassProgressBar
import com.styropyr0.prismal.components.PrismalGlassSlider
import com.styropyr0.prismal.components.PrismalGlassToggle
import com.styropyr0.prismal.components.PrismalGradientGlassPanel
import com.styropyr0.prismal.effects.rememberPrismalAdaptiveLuminance
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.shapes.PrismalRoundedRectangle
import com.styropyr0.prismal.sources.PrismalGlassLayer
import com.styropyr0.prismal.sources.prismalGlassLayer
import com.styropyr0.prismal.sources.rememberPrismalGlassLayer

private enum class PlaygroundTab {
    Home,
    Search,
    Profile,
    Settings
}

@Composable
fun CatalogPlaygroundScreen() {
    val backdropLayer = rememberPrismalGlassLayer()
    val glassParams = remember { GlassPlaygroundParams() }
    var selectedTab by remember { mutableIntStateOf(PlaygroundTab.Home.ordinal) }
    var toggleOn by remember { mutableStateOf(true) }
    var sliderValue by remember { mutableFloatStateOf(0.45f) }
    var progress by remember { mutableFloatStateOf(0.65f) }
    var indeterminate by remember { mutableStateOf(false) }
    var backgroundImageUri by remember { mutableStateOf<Uri?>(null) }

    val isLightTheme = !isSystemInDarkTheme()
    val adaptiveLuminance = rememberPrismalAdaptiveLuminance(
        enabled = glassParams.adaptiveLuminance,
        source = backdropLayer,
        isLightTheme = isLightTheme
    )
    val luminance = { adaptiveLuminance.luminance }

    val pickBackgroundImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            backgroundImageUri = uri
        }
    }

    Box(Modifier.fillMaxSize()) {
        PlaygroundBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .prismalGlassLayer(backdropLayer),
            backgroundImageUri = backgroundImageUri
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                PrismalGlassBottomTabs(
                    selectedTabIndex = { selectedTab },
                    onTabSelected = { selectedTab = it },
                    backdrop = backdropLayer,
                    tabsCount = 4,
                    adaptiveLuminance = glassParams.adaptiveLuminance,
                    luminance = luminance,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    PrismalGlassBottomTab(onClick = { selectedTab = PlaygroundTab.Home.ordinal }) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(22.dp))
                        Text("Home", style = MaterialTheme.typography.labelSmall)
                    }
                    PrismalGlassBottomTab(onClick = { selectedTab = PlaygroundTab.Search.ordinal }) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(22.dp))
                        Text("Search", style = MaterialTheme.typography.labelSmall)
                    }
                    PrismalGlassBottomTab(onClick = { selectedTab = PlaygroundTab.Profile.ordinal }) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(22.dp))
                        Text("Profile", style = MaterialTheme.typography.labelSmall)
                    }
                    PrismalGlassBottomTab(onClick = { selectedTab = PlaygroundTab.Settings.ordinal }) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(22.dp))
                        Text("Settings", style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            modifier = Modifier.padding(bottom = 20.dp)
        ) { innerPadding ->
            val contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 24.dp,
                bottom = 24.dp
            )

            when (PlaygroundTab.entries[selectedTab]) {
                PlaygroundTab.Home -> CatalogTabContent(
                    backdrop = backdropLayer,
                    glassParams = glassParams,
                    luminance = luminance,
                    toggleOn = toggleOn,
                    onToggleChange = { toggleOn = it },
                    sliderValue = sliderValue,
                    onSliderValueChange = { sliderValue = it },
                    progress = progress,
                    indeterminate = indeterminate,
                    onIndeterminateChange = { indeterminate = it },
                    onAdvanceProgress = {
                        progress = ((progress + 0.15f) % 1.05f).coerceAtMost(1f)
                    },
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )

                PlaygroundTab.Search -> SearchTabContent(
                    backdrop = backdropLayer,
                    glassParams = glassParams,
                    luminance = luminance,
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )

                PlaygroundTab.Profile -> ProfileTabContent(
                    backdrop = backdropLayer,
                    glassParams = glassParams,
                    luminance = luminance,
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )

                PlaygroundTab.Settings -> GlassSettingsPlayground(
                    backdrop = backdropLayer,
                    backdropLayer = backdropLayer,
                    params = glassParams,
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = contentPadding
                )
            }
        }

        BackgroundImagePickerButton(
            backdrop = backdropLayer,
            onClick = {
                pickBackgroundImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 12.dp, end = 20.dp)
        )
    }
}

@Composable
private fun CatalogTabContent(
    backdrop: PrismalGlassLayer,
    glassParams: GlassPlaygroundParams,
    luminance: () -> Float,
    toggleOn: Boolean,
    onToggleChange: (Boolean) -> Unit,
    sliderValue: Float,
    onSliderValueChange: (Float) -> Unit,
    progress: Float,
    indeterminate: Boolean,
    onIndeterminateChange: (Boolean) -> Unit,
    onAdvanceProgress: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val density = LocalDensity.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Prismal Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Interactive glass components sampled from the backdrop below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        item {
            CatalogSection(title = "Buttons") {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrismalGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        luminance = luminance
                    ) {
                        Text("Default")
                    }
                    PrismalGlassButton(
                        onClick = {},
                        backdrop = backdrop,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        luminance = luminance,
                        tint = Color(0xFF5856D6)
                    ) {
                        Text("Tinted")
                    }
                }
            }
        }

        item {
            CatalogSection(title = "Toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications", style = MaterialTheme.typography.bodyLarge)
                    PrismalGlassToggle(
                        selected = { toggleOn },
                        onSelect = onToggleChange,
                        backdrop = backdrop,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        luminance = luminance
                    )
                }
            }
        }

        item {
            CatalogSection(title = "Slider") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Value: ${"%.0f".format(sliderValue * 100)}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    PrismalGlassSlider(
                        value = { sliderValue },
                        onValueChange = onSliderValueChange,
                        valueRange = 0f..1f,
                        visibilityThreshold = 0.01f,
                        backdrop = backdrop,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        luminance = luminance
                    )
                }
            }
        }

        item {
            CatalogSection(title = "Progress") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PrismalGlassButton(
                            onClick = { onIndeterminateChange(!indeterminate) },
                            backdrop = backdrop,
                            adaptiveLuminance = glassParams.adaptiveLuminance,
                            luminance = luminance,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (indeterminate) "Determinate" else "Indeterminate")
                        }
                        if (!indeterminate) {
                            PrismalGlassButton(
                                onClick = onAdvanceProgress,
                                backdrop = backdrop,
                                adaptiveLuminance = glassParams.adaptiveLuminance,
                                luminance = luminance,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Advance")
                            }
                        }
                    }
                    PrismalGlassProgressBar(
                        progress = { progress },
                        backdrop = backdrop,
                        indeterminate = indeterminate,
                        adaptiveLuminance = glassParams.adaptiveLuminance,
                        luminance = luminance
                    )
                }
            }
        }

        item {
            CatalogSection(title = "Gradient Glass Panel") {
                PrismalGradientGlassPanel(
                    backdrop = backdrop,
                    height = 160.dp,
                    adaptiveLuminance = glassParams.adaptiveLuminance,
                    luminance = luminance,
                    blurRadiusDp = glassParams.blurRadiusDp.dp,
                    refractionHeightDp = glassParams.refractionHeightDp.dp,
                    refractionAmountDp = glassParams.refractionAmountDp.dp,
                    refractionBottomWeight = glassParams.gradientBottomWeight,
                    blurFadeEnd = glassParams.gradientBlurFadeEnd,
                    chromaticAberration = glassParams.chromaticAberration
                )
            }
        }

        item {
            CatalogSection(title = "Glass Surface") {
                PrismalGlassSurface(
                    backdrop = backdrop,
                    shape = { PrismalRoundedRectangle(24.dp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    onClick = {},
                    adaptiveLuminance = glassParams.adaptiveLuminance,
                    luminance = luminance,
                    brightness = glassParams.brightness,
                    saturation = glassParams.saturation,
                    refractionHeightPx = with(density) { glassParams.refractionHeightDp.dp.toPx() },
                    refractionAmountPx = with(density) { glassParams.refractionAmountDp.dp.toPx() },
                    depthEffect = glassParams.depthEffect,
                    chromaticAberration = glassParams.chromaticAberration,
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Tap me",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "General-purpose glass container with press ripple.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun BackgroundImagePickerButton(
    backdrop: PrismalGlassLayer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PrismalGlassSurface(
        backdrop = backdrop,
        shape = { PrismalCapsule() },
        modifier = modifier.size(60.dp),
        onClick = onClick,
        content = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_img_picker),
                    contentDescription = stringResource(R.string.pick_background_image),
                    modifier = Modifier.size(30.dp),
                    tint = Color.Black
                )
            }
        }
    )
}

@Composable
private fun PlaygroundBackdrop(
    modifier: Modifier = Modifier,
    backgroundImageUri: Uri? = null
) {
    if (backgroundImageUri != null) {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(backgroundImageUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
        return
    }

    DefaultPlaygroundBackdrop(modifier)
}

@Composable
private fun DefaultPlaygroundBackdrop(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    val baseColors = if (isDark) {
        listOf(Color(0xFF0D1117), Color(0xFF161B22), Color(0xFF21262D))
    } else {
        listOf(Color(0xFFE8F4FD), Color(0xFFFCE4EC), Color(0xFFE8EAF6))
    }
    val accentColors = listOf(
        Color(0xFFFF6B6B),
        Color(0xFF4ECDC4),
        Color(0xFFFFE66D),
        Color(0xFF5856D6),
        Color(0xFF34C759)
    )

    Box(
        modifier.background(
            Brush.linearGradient(
                colors = baseColors,
                start = Offset.Zero,
                end = Offset(800f, 1400f)
            )
        )
    ) {
        accentColors.forEachIndexed { index, color ->
            val xOffset = (index * 73) % 280
            val yOffset = 120 + index * 180
            Box(
                Modifier
                    .padding(start = xOffset.dp, top = yOffset.dp)
                    .size((120 + index * 20).dp)
                    .clip(if (index % 2 == 0) CircleShape else PrismalCapsule())
                    .background(color.copy(alpha = 0.55f))
            )
        }

        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
                .size(200.dp)
                .clip(PrismalRoundedRectangle(48.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0088FF).copy(alpha = 0.6f),
                            Color(0xFF5856D6).copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun CatalogSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
        content()
    }
}

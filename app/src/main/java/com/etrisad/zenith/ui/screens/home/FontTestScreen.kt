package com.etrisad.zenith.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.GSFlexPreset
import com.etrisad.zenith.ui.components.ZenithToggleButtonGroup
import com.etrisad.zenith.ui.components.ZenithToggleOption
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.theme.GSFlexSettings
import com.etrisad.zenith.ui.theme.VariableFontFactory
import com.etrisad.zenith.ui.screens.settings.VariableSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontTestScreen(
    onBack: () -> Unit,
    innerPadding: PaddingValues
) {
    var settings by remember { mutableStateOf(GSFlexSettings(preset = GSFlexPreset.ZENITH)) }
    var customText by remember { mutableStateOf("Zenith") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val dynamicTypography = VariableFontFactory.createTypography(settings)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = innerPadding.calculateTopPadding())
    ) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.overlay_font_tab_custom)) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.overlay_font_tab_scale)) })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text(stringResource(R.string.overlay_font_tab_presets)) })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                0 -> {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text(stringResource(R.string.overlay_font_interactive_text)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        shape = MaterialTheme.shapes.large
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = customText,
                                style = dynamicTypography.displayLarge.copy(
                                    fontSize = 64.sp,
                                    lineHeight = 64.sp,
                                    letterSpacing = (-2).sp
                                )
                            )
                            Text(
                                text = stringResource(R.string.overlay_font_custom_editorial),
                                style = dynamicTypography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                1 -> {
                    Text(stringResource(R.string.overlay_font_dynamic_typescale), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    TypescaleItem(stringResource(R.string.overlay_typescale_display_large), dynamicTypography.displayLarge)
                    TypescaleItem(stringResource(R.string.overlay_typescale_headline_medium), dynamicTypography.headlineMedium)
                    TypescaleItem(stringResource(R.string.overlay_typescale_title_large), dynamicTypography.titleLarge)
                    TypescaleItem(stringResource(R.string.overlay_typescale_body_large), dynamicTypography.bodyLarge)
                    TypescaleItem(stringResource(R.string.overlay_typescale_label_small), dynamicTypography.labelSmall)
                }
                2 -> {
                    ZenithProposeContent(
                        currentPreset = settings.preset,
                        onPresetChange = { settings = settings.copy(preset = it) },
                        typography = dynamicTypography
                    )
                }
            }

            if (selectedTab != 2) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(stringResource(R.string.overlay_font_global_axis_override), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                
                val currentAxes = settings.display
                fun updateGlobal(newVal: (com.etrisad.zenith.ui.theme.FontAxes) -> com.etrisad.zenith.ui.theme.FontAxes) {
                    settings = settings.copy(
                        preset = GSFlexPreset.CUSTOM,
                        display = newVal(settings.display),
                        headline = newVal(settings.headline),
                        body = newVal(settings.body)
                    )
                }

                VariableSlider(label = stringResource(R.string.overlay_font_axis_weight), value = currentAxes.weight, range = 100f..1000f, onValueChange = { w -> updateGlobal { it.copy(weight = w) } })
                VariableSlider(label = stringResource(R.string.overlay_font_axis_width), value = currentAxes.width, range = 25f..150f, onValueChange = { w -> updateGlobal { it.copy(width = w) } })
                VariableSlider(label = stringResource(R.string.overlay_font_axis_optical_size), value = currentAxes.opsz, range = 6f..72f, onValueChange = { v -> updateGlobal { it.copy(opsz = v) } })
                VariableSlider(label = stringResource(R.string.overlay_font_axis_grade), value = currentAxes.grade, range = -200f..200f, onValueChange = { v -> updateGlobal { it.copy(grade = v) } })
                VariableSlider(label = stringResource(R.string.overlay_font_axis_slant), value = currentAxes.slant, range = -10f..0f, onValueChange = { v -> updateGlobal { it.copy(slant = v) } })
                VariableSlider(label = stringResource(R.string.overlay_font_axis_roundness), value = currentAxes.roundness, range = 0f..100f, onValueChange = { v -> updateGlobal { it.copy(roundness = v) } })
            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 32.dp))
        }
    }
}

@Composable
fun ZenithProposeContent(
    currentPreset: GSFlexPreset,
    onPresetChange: (GSFlexPreset) -> Unit,
    typography: Typography
) {
    val presetOptions = listOf(
        GSFlexPreset.ZENITH to stringResource(R.string.overlay_font_preset_zenith),
        GSFlexPreset.NEO to stringResource(R.string.overlay_font_preset_neo),
        GSFlexPreset.COMPACT to stringResource(R.string.overlay_font_preset_impact),
        GSFlexPreset.AIRY to stringResource(R.string.overlay_font_preset_airy)
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(stringResource(R.string.overlay_font_pro_presets), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            ZenithToggleButtonGroup(
                options = presetOptions.map { ZenithToggleOption(text = it.second) },
                selectedIndices = setOf(presetOptions.indexOfFirst { it.first == currentPreset }.coerceAtLeast(0)),
                onToggle = { onPresetChange(presetOptions[it].first) },
                size = ZenithButtonSize.Medium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(currentPreset.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.overlay_font_material_preset_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ZenithProposeItem(
            level = stringResource(R.string.overlay_font_level_display),
            style = typography.displayLarge,
            description = stringResource(R.string.overlay_font_display_description)
        )

        ZenithProposeItem(
            level = stringResource(R.string.overlay_font_level_headline),
            style = typography.headlineMedium,
            description = stringResource(R.string.overlay_font_headline_description)
        )

        ZenithProposeItem(
            level = stringResource(R.string.overlay_font_level_body),
            style = typography.bodyMedium,
            description = stringResource(R.string.overlay_font_body_description)
        )
    }
}

@Composable
fun ZenithProposeItem(
    level: String,
    style: androidx.compose.ui.text.TextStyle,
    description: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = level, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
        Text(
            text = stringResource(R.string.overlay_font_zenith_design),
            style = style
        )
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    }
}

@Composable
fun TypescaleItem(label: String, style: androidx.compose.ui.text.TextStyle) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(
            text = stringResource(R.string.overlay_font_family_google_sans_flex),
            style = style
        )
    }
}

package com.etrisad.zenith.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etrisad.zenith.ui.components.focus.TimePickerDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.PerformanceConfig
import com.etrisad.zenith.data.preferences.PerformanceLevel
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.data.preferences.detectPreset
import com.etrisad.zenith.data.preferences.isPreset
import com.etrisad.zenith.data.preferences.toConfig
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.ZenithButtonWeighted
import com.etrisad.zenith.ui.components.ZenithGroupedButton
import com.etrisad.zenith.ui.components.focus.PreferenceCategory
import com.etrisad.zenith.ui.screens.settings.SettingsToggle
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.service.SharedMonitoringState
import com.etrisad.zenith.ui.components.BankingWarningBottomSheet
import com.etrisad.zenith.util.isAccessibilityServiceEnabled
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceSettings(
    preferences: UserPreferences,
    selectedLevel: PerformanceLevel,
    onSelectLevel: (PerformanceLevel) -> Unit,
    preferencesRepository: UserPreferencesRepository? = null,
    coroutineScope: kotlinx.coroutines.CoroutineScope? = null,
    onDisableTrackingAtUnusedHoursChange: (Boolean) -> Unit = {},
    onDisableTrackingStartHourChange: (Int) -> Unit = {},
    onDisableTrackingEndHourChange: (Int) -> Unit = {},
) {
    val allLevels = PerformanceLevel.values()
    var pendingBatteryLevel by remember { mutableStateOf<PerformanceLevel?>(null) }

    PreferenceCategory(title = stringResource(R.string.performance_profile))

    allLevels.forEachIndexed { index, level ->
        val isSelected = preferences.performanceLevel == level
        val isChecked = selectedLevel == level
        val isCustom = level == PerformanceLevel.CUSTOM
        val isBatteryPreset = level == PerformanceLevel.BATTERY_SAVER || level == PerformanceLevel.MAX_BATTERY

        val shape = when {
            index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            isCustom && index == allLevels.lastIndex -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            else -> RoundedCornerShape(8.dp)
        }

        val bgColor by animateColorAsState(
            targetValue = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else if (isCustom) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surfaceContainerLow,
            label = "preset_bg_${level.name}"
        )

        Surface(
            onClick = {
                if (isBatteryPreset) {
                    pendingBatteryLevel = level
                } else {
                    onSelectLevel(level)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = bgColor,
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val circleBg by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                    label = "circleBg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
                    label = "borderColor"
                )
                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "iconTint"
                )
                val iconState = when {
                    isSelected -> 0
                    isChecked -> 1
                    isCustom -> 2
                    else -> 3
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.5.dp, borderColor, CircleShape)
                        .clip(CircleShape)
                        .background(circleBg),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = iconState, animationSpec = spring(0.8f, 400f), label = "profileIcon") { state ->
                        when (state) {
                            0 -> Icon(Icons.Filled.Check, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                            1 -> Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                            2 -> Icon(Icons.Outlined.Tune, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(level.labelRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    if (isCustom && isSelected) {
                        val configSummary = activeConfigSummary(preferences.buildPerformanceConfig())
                        Text(text = configSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(text = stringResource(level.descriptionRes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (index < allLevels.lastIndex) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    pendingBatteryLevel?.let { level ->
        BatteryWarningBottomSheet(
            level = level,
            onDismiss = { pendingBatteryLevel = null },
            onConfirm = {
                pendingBatteryLevel = null
                onSelectLevel(level)
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(text = stringResource(R.string.performance_custom_profile_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp))

    Spacer(modifier = Modifier.height(16.dp))
    PreferenceCategory(title = stringResource(R.string.performance_unused_hours))
    SettingsToggle(
        title = stringResource(R.string.performance_disable_tracking_unused_hours),
        description = stringResource(R.string.performance_disable_tracking_unused_hours_desc),
        checked = preferences.disableTrackingAtUnusedHours,
        onCheckedChange = onDisableTrackingAtUnusedHoursChange,
        icon = Icons.Outlined.Nightlight,
        shape = if (preferences.disableTrackingAtUnusedHours) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                else RoundedCornerShape(24.dp)
    )
    AnimatedVisibility(
        visible = preferences.disableTrackingAtUnusedHours,
        enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)),
        exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
    ) {
        Column {
            Spacer(modifier = Modifier.height(4.dp))
            var showStartPicker by remember { mutableStateOf(false) }
            var showEndPicker by remember { mutableStateOf(false) }
            val startTimeState = rememberTimePickerState(
                initialHour = preferences.disableTrackingStartHour,
                initialMinute = 0,
                is24Hour = true
            )
            val endTimeState = rememberTimePickerState(
                initialHour = preferences.disableTrackingEndHour,
                initialMinute = 0,
                is24Hour = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 28.dp, bottomEnd = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.performance_start_time), style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "%02d:00".format(preferences.disableTrackingStartHour),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Surface(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.performance_end_time), style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "%02d:00".format(preferences.disableTrackingEndHour),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            if (showStartPicker) {
                TimePickerDialog(
                    onDismiss = { showStartPicker = false },
                    onConfirm = {
                        onDisableTrackingStartHourChange(startTimeState.hour)
                        showStartPicker = false
                    }
                ) {
                    MaterialTheme(
                        typography = MaterialTheme.typography.copy(
                            displayLarge = MaterialTheme.typography.headlineLarge
                        )
                    ) {
                        TimePicker(state = startTimeState)
                    }
                }
            }
            if (showEndPicker) {
                TimePickerDialog(
                    onDismiss = { showEndPicker = false },
                    onConfirm = {
                        onDisableTrackingEndHourChange(endTimeState.hour)
                        showEndPicker = false
                    }
                ) {
                    MaterialTheme(
                        typography = MaterialTheme.typography.copy(
                            displayLarge = MaterialTheme.typography.headlineLarge
                        )
                    ) {
                        TimePicker(state = endTimeState)
                    }
                }
            }
        }
    }
}

@Composable
fun PerformanceTuningPanel(
    preferences: UserPreferences,
    selectedLevel: PerformanceLevel,
    onSelectedLevelChange: (PerformanceLevel) -> Unit,
    onApplyCustomSettings: (PerformanceConfig) -> Unit,
    onSetPerformanceLevel: ((PerformanceLevel) -> Unit)? = null,
    onRegisterApplyAction: ((() -> Unit) -> Unit)? = null,
    onResetPerfMonDelays: (() -> Unit)? = null,
    onAccessibilityRequiredChange: (Boolean) -> Unit = {},
    preferencesRepository: UserPreferencesRepository? = null,
) {
    val scope = rememberCoroutineScope()
    var tuningExpanded by remember { mutableStateOf(selectedLevel == PerformanceLevel.CUSTOM) }
    var delayExpanded by remember { mutableStateOf(selectedLevel == PerformanceLevel.CUSTOM) }

    var a11yActive by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().a11yActiveDelay
            else preferences.performanceConfig.a11yActiveDelay
        )
    }
    var a11yInactive by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().a11yInactiveDelay
            else preferences.performanceConfig.a11yInactiveDelay
        )
    }
    var screenOff by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().screenOffDelay
            else preferences.performanceConfig.screenOffDelay
        )
    }
    var powerSave by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().powerSaveDelay
            else preferences.performanceConfig.powerSaveDelay
        )
    }
    var usageCache by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().usageStatsCacheMs
            else preferences.performanceConfig.usageStatsCacheMs
        )
    }
    var dbWrite by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().shieldDbWriteMs
            else preferences.performanceConfig.shieldDbWriteMs
        )
    }
    var dbWriteNear by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().shieldDbWriteNearMs
            else preferences.performanceConfig.shieldDbWriteNearMs
        )
    }
    var launcherCache by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().launcherCacheMs
            else preferences.performanceConfig.launcherCacheMs
        )
    }

    var delayPowerSave by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monPowerSave
            else preferences.performanceConfig.monPowerSave
        )
    }
    var delayOverlayShowing by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monOverlayShowing
            else preferences.performanceConfig.monOverlayShowing
        )
    }
    var delayGoalNear by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monGoalNear
            else preferences.performanceConfig.monGoalNear
        )
    }
    var delayGoalMid by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monGoalMid
            else preferences.performanceConfig.monGoalMid
        )
    }
    var delayGoalFar by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monGoalFar
            else preferences.performanceConfig.monGoalFar
        )
    }
    var delayShieldNear by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monShieldNear
            else preferences.performanceConfig.monShieldNear
        )
    }
    var delayShieldMid by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monShieldMid
            else preferences.performanceConfig.monShieldMid
        )
    }
    var delayShieldFar by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monShieldFar
            else preferences.performanceConfig.monShieldFar
        )
    }
    var delayShieldVeryFar by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monShieldVeryFar
            else preferences.performanceConfig.monShieldVeryFar
        )
    }
    var delayDefault by remember {
        mutableStateOf(
            if (selectedLevel.isPreset()) selectedLevel.toConfig().monDefault
            else preferences.performanceConfig.monDefault
        )
    }

    LaunchedEffect(selectedLevel) {
        if (selectedLevel == PerformanceLevel.CUSTOM) {
            tuningExpanded = true
            delayExpanded = true
        }
        if (selectedLevel.isPreset()) {
            val cfg = selectedLevel.toConfig()
            a11yActive = cfg.a11yActiveDelay
            a11yInactive = cfg.a11yInactiveDelay
            screenOff = cfg.screenOffDelay
            powerSave = cfg.powerSaveDelay
            usageCache = cfg.usageStatsCacheMs
            dbWrite = cfg.shieldDbWriteMs
            dbWriteNear = cfg.shieldDbWriteNearMs
            launcherCache = cfg.launcherCacheMs
            delayPowerSave = cfg.monPowerSave
            delayOverlayShowing = cfg.monOverlayShowing
            delayGoalNear = cfg.monGoalNear
            delayGoalMid = cfg.monGoalMid
            delayGoalFar = cfg.monGoalFar
            delayShieldNear = cfg.monShieldNear
            delayShieldMid = cfg.monShieldMid
            delayShieldFar = cfg.monShieldFar
            delayShieldVeryFar = cfg.monShieldVeryFar
            delayDefault = cfg.monDefault
        }
    }

    val activeCfg = preferences.buildPerformanceConfig()

    val autoSwitch = {
        if (selectedLevel.isPreset()) {
            onSelectedLevelChange(PerformanceLevel.CUSTOM)
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasAccessibility by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var showBankingWarning by remember { mutableStateOf(false) }

    val installedBankingApps = remember {
        val allInstalled = context.packageManager.getInstalledApplications(0)
            .map { it.packageName }
            .toSet()
        SharedMonitoringState.FINANCIAL_APPS.filter { it in allInstalled }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccessibility = isAccessibilityServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        onRegisterApplyAction?.invoke {
            scope.launch {
                val config = PerformanceConfig(
                    a11yActiveDelay = a11yActive,
                    a11yInactiveDelay = a11yInactive,
                    screenOffDelay = screenOff,
                    powerSaveDelay = powerSave,
                    usageStatsCacheMs = usageCache,
                    shieldDbWriteMs = dbWrite,
                    shieldDbWriteNearMs = dbWriteNear,
                    launcherCacheMs = launcherCache,
                    monPowerSave = delayPowerSave,
                    monOverlayShowing = delayOverlayShowing,
                    monGoalNear = delayGoalNear,
                    monGoalMid = delayGoalMid,
                    monGoalFar = delayGoalFar,
                    monShieldNear = delayShieldNear,
                    monShieldMid = delayShieldMid,
                    monShieldFar = delayShieldFar,
                    monShieldVeryFar = delayShieldVeryFar,
                    monDefault = delayDefault,
                )
                val detectedLevel = config.detectPreset()
                onApplyCustomSettings(config)
                onSelectedLevelChange(detectedLevel)
                onSetPerformanceLevel?.invoke(detectedLevel)
            }
        }
    }

    PreferenceCategory(title = stringResource(R.string.performance_instant_detection))
    TuningPermissionItemRow(
        title = stringResource(R.string.performance_accessibility_service),
        description = if (hasAccessibility) stringResource(R.string.performance_accessibility_service_active_desc)
                      else stringResource(R.string.performance_accessibility_service_inactive_desc),
        isGranted = hasAccessibility,
        onClick = {
            showBankingWarning = true
        },
        onInfoClick = { showBankingWarning = true },
        icon = Icons.Outlined.AccessibilityNew,
        position = TuningGroupPosition.Top
    )
    Spacer(modifier = Modifier.height(4.dp))
    SettingsToggle(
        title = stringResource(R.string.performance_make_as_requirement),
        description = stringResource(R.string.performance_make_as_requirement_desc),
        checked = preferences.accessibilityRequired,
        onCheckedChange = onAccessibilityRequiredChange,
        icon = Icons.Outlined.Lock,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    SettingsToggle(
        title = stringResource(R.string.performance_accessibility_active_notification),
        description = stringResource(R.string.performance_accessibility_active_notification_desc),
        checked = preferences.accessibilityActiveNotificationEnabled,
        onCheckedChange = { checked ->
            scope.launch {
                preferencesRepository?.setAccessibilityActiveNotificationEnabled(checked)
            }
        },
        icon = Icons.Outlined.WarningAmber,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))

    if (showBankingWarning) {
        BankingWarningBottomSheet(
            onDismiss = { showBankingWarning = false },
            onProceed = {
                showBankingWarning = false
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onDontShowAgain = {
                scope.launch {
                    preferencesRepository?.setBankingWarningDismissed(true)
                }
            }
        )
    }

    PreferenceCategory(title = stringResource(R.string.performance_custom_tuning))

    val tuningChevronRotation by animateFloatAsState(
        targetValue = if (tuningExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f), label = "tuningChevronRotation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { tuningExpanded = !tuningExpanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Tune, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.performance_parameter_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (tuningExpanded) stringResource(R.string.performance_parameter_details_expanded_hint)
                    else stringResource(R.string.performance_parameter_details_collapsed_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = if (tuningExpanded) stringResource(R.string.performance_collapse) else stringResource(R.string.performance_expand),
                modifier = Modifier.rotate(tuningChevronRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    AnimatedVisibility(
        visible = tuningExpanded,
        enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)),
        exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
    ) {
        Column {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_check_interval), Icons.Outlined.Timer)
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderItem(stringResource(R.string.performance_overlay_active), stringResource(R.string.performance_overlay_active_desc),
                        a11yActive / 1000f, 30f..600f, "s", Icons.Outlined.Visibility,
                        onChange = { a11yActive = (it * 1000).toLong(); autoSwitch() })
                    SliderItem(stringResource(R.string.performance_overlay_hidden), stringResource(R.string.performance_overlay_hidden_desc),
                        a11yInactive / 1000f, 1f..10f, "s", Icons.Outlined.VisibilityOff, 9,
                        onChange = { a11yInactive = (it * 1000).toLong(); autoSwitch() })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_power_saving), Icons.Outlined.BatteryChargingFull)
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderItem(stringResource(R.string.performance_screen_off), stringResource(R.string.performance_screen_off_desc),
                        screenOff / 60000f, 1f..30f, "min", Icons.Outlined.ScreenLockPortrait, 29,
                        onChange = { screenOff = (it * 60000).toLong(); autoSwitch() })
                    SliderItem(stringResource(R.string.performance_battery_saver), stringResource(R.string.performance_battery_saver_desc),
                        powerSave / 60000f, 1f..30f, "min", Icons.Outlined.BatteryStd, 29,
                        onChange = { powerSave = (it * 60000).toLong(); autoSwitch() })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_data_refresh), Icons.Outlined.Storage)
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderItem(stringResource(R.string.performance_usage_stats), stringResource(R.string.performance_usage_stats_desc),
                        usageCache / 60000f, 0.17f..60f, "min", Icons.Outlined.Refresh,
                        onChange = { usageCache = (it * 60000).toLong(); autoSwitch() })
                    SliderItem(stringResource(R.string.performance_app_list), stringResource(R.string.performance_app_list_desc),
                        launcherCache / 60000f, 5f..240f, "min", Icons.Outlined.Apps,
                        onChange = { launcherCache = (it * 60000).toLong(); autoSwitch() })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_storage), Icons.Outlined.Save)
                    Spacer(modifier = Modifier.height(8.dp))
                    SliderItem(stringResource(R.string.performance_save_data), stringResource(R.string.performance_save_data_desc),
                        dbWrite / 60000f, 1f..30f, "min", Icons.Outlined.Storage, 29,
                        onChange = { dbWrite = (it * 60000).toLong(); autoSwitch() })
                    SliderItem(stringResource(R.string.performance_save_data_near), stringResource(R.string.performance_save_data_near_desc),
                        dbWriteNear / 60000f, 0.5f..10f, "min", Icons.Outlined.TimerOff,
                        onChange = { dbWriteNear = (it * 60000).toLong(); autoSwitch() })
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    val delayBottomCorner by animateDpAsState(
        targetValue = if (delayExpanded) 8.dp else 28.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f), label = "delayBottomCorner"
    )
    val delayChevronRotation by animateFloatAsState(
        targetValue = if (delayExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f), label = "delayChevronRotation"
    )
    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = delayBottomCorner, bottomEnd = delayBottomCorner)).clickable { delayExpanded = !delayExpanded },
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = delayBottomCorner, bottomEnd = delayBottomCorner),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Timer, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.performance_scan_timing), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.performance_scan_timing_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2)
            }
            Icon(
                Icons.Outlined.ExpandMore,
                contentDescription = if (delayExpanded) stringResource(R.string.performance_collapse) else stringResource(R.string.performance_expand),
                modifier = Modifier.rotate(delayChevronRotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    AnimatedVisibility(
        visible = delayExpanded,
        enter = expandVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeIn(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)),
        exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f)) + fadeOut(animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f))
    ) {
        Column {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_power_overlay), Icons.Outlined.BatteryChargingFull)
                    Spacer(modifier = Modifier.height(8.dp))
                    DelaySliderItem(label = stringResource(R.string.performance_power_save_mode), description = stringResource(R.string.performance_power_save_mode_desc), icon = Icons.Outlined.BatteryChargingFull, value = delayPowerSave, onValueChange = { v -> delayPowerSave = v; autoSwitch() }, onReset = { delayPowerSave = 5000L; autoSwitch() }, range = 500f..15000f)
                    DelaySliderItem(label = stringResource(R.string.performance_overlay_showing), description = stringResource(R.string.performance_overlay_showing_desc), icon = Icons.Outlined.Visibility, value = delayOverlayShowing, onValueChange = { v -> delayOverlayShowing = v; autoSwitch() }, onReset = { delayOverlayShowing = 8000L; autoSwitch() }, range = 500f..15000f)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_goal_shield), Icons.Outlined.Flag)
                    Spacer(modifier = Modifier.height(8.dp))
                    DelaySliderItem(label = stringResource(R.string.performance_delay_near_label), description = stringResource(R.string.performance_delay_goal_near_desc), icon = Icons.Outlined.Flag, value = delayGoalNear, onValueChange = { v -> delayGoalNear = v; autoSwitch() }, onReset = { delayGoalNear = 600L; autoSwitch() }, range = 100f..10000f)
                    DelaySliderItem(label = stringResource(R.string.performance_delay_goal_mid_label), description = stringResource(R.string.performance_delay_goal_mid_desc), icon = Icons.Outlined.Flag, value = delayGoalMid, onValueChange = { v -> delayGoalMid = v; autoSwitch() }, onReset = { delayGoalMid = 1200L; autoSwitch() }, range = 100f..10000f)
                    DelaySliderItem(label = stringResource(R.string.performance_delay_goal_far_label), description = stringResource(R.string.performance_delay_goal_far_desc), icon = Icons.Outlined.Flag, value = delayGoalFar, onValueChange = { v -> delayGoalFar = v; autoSwitch() }, onReset = { delayGoalFar = 1800L; autoSwitch() }, range = 100f..10000f)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_regular_shield), Icons.Outlined.Security)
                    Spacer(modifier = Modifier.height(8.dp))
                    DelaySliderItem(label = stringResource(R.string.performance_delay_near_label), description = stringResource(R.string.performance_delay_shield_near_desc), icon = Icons.Outlined.Security, value = delayShieldNear, onValueChange = { v -> delayShieldNear = v; autoSwitch() }, onReset = { delayShieldNear = 600L; autoSwitch() }, range = 100f..10000f)
                    DelaySliderItem(label = stringResource(R.string.performance_delay_shield_mid_label), description = stringResource(R.string.performance_delay_shield_mid_desc), icon = Icons.Outlined.Security, value = delayShieldMid, onValueChange = { v -> delayShieldMid = v; autoSwitch() }, onReset = { delayShieldMid = 1500L; autoSwitch() }, range = 100f..10000f)
                    DelaySliderItem(label = stringResource(R.string.performance_delay_shield_far_label), description = stringResource(R.string.performance_delay_shield_far_desc), icon = Icons.Outlined.Security, value = delayShieldFar, onValueChange = { v -> delayShieldFar = v; autoSwitch() }, onReset = { delayShieldFar = 3000L; autoSwitch() }, range = 100f..15000f)
                    DelaySliderItem(label = stringResource(R.string.performance_delay_shield_very_far_label), description = stringResource(R.string.performance_delay_shield_very_far_desc), icon = Icons.Outlined.Security, value = delayShieldVeryFar, onValueChange = { v -> delayShieldVeryFar = v; autoSwitch() }, onReset = { delayShieldVeryFar = 5000L; autoSwitch() }, range = 100f..15000f)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GroupHeader(stringResource(R.string.performance_default), Icons.Outlined.TouchApp)
                    Spacer(modifier = Modifier.height(8.dp))
                    DelaySliderItem(label = stringResource(R.string.performance_default_interval), description = stringResource(R.string.performance_default_interval_desc), icon = Icons.Outlined.TouchApp, value = delayDefault, onValueChange = { v -> delayDefault = v; autoSwitch() }, onReset = { delayDefault = 1200L; autoSwitch() }, range = 100f..10000f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                delayPowerSave = 5000L; delayOverlayShowing = 8000L
                                delayGoalNear = 600L; delayGoalMid = 1200L; delayGoalFar = 1800L
                                delayShieldNear = 600L; delayShieldMid = 1500L; delayShieldFar = 3000L; delayShieldVeryFar = 5000L
                                delayDefault = 1200L
                                onResetPerfMonDelays?.invoke()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.performance_reset_all), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DelaySliderItem(
    label: String,
    description: String,
    icon: ImageVector,
    value: Long,
    onValueChange: (Long) -> Unit,
    onReset: () -> Unit,
    range: ClosedFloatingPointRange<Float> = 100f..5000f
) {
    val scope = rememberCoroutineScope()
    var localValue by remember { mutableFloatStateOf(value.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }
    val animatable = remember { Animatable(value.toFloat()) }

    LaunchedEffect(value) {
        if (!isDragging) {
            try {
                animatable.animateTo(value.toFloat(), spring(0.8f, 400f))
            } catch (_: Exception) { }
        }
    }

    val displayValue = if (isDragging) localValue else animatable.value

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = label, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = stringResource(R.string.performance_reset),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(text = description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = fmtDur(displayValue.toLong()), style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(32.dp))
            Slider(
                value = displayValue.coerceIn(range),
                onValueChange = {
                    isDragging = true
                    localValue = (Math.round(it / 10.0) * 10).toFloat()
                },
                onValueChangeFinished = {
                    scope.launch {
                        animatable.snapTo(localValue)
                        isDragging = false
                        onValueChange(localValue.toLong())
                    }
                },
                valueRange = range,
                modifier = Modifier.weight(1f).height(24.dp)
            )
        }
    }
}

@Composable
private fun GroupHeader(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatteryWarningBottomSheet(
    level: PerformanceLevel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val iconScale = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
            )
        }
        launch {
            contentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 200f)
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .graphicsLayer(
                        scaleX = iconScale.value,
                        scaleY = iconScale.value,
                        alpha = iconScale.value
                    ),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(level.labelRes),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer(alpha = contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.performance_battery_preset_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer(alpha = contentAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            ZenithGroupedButton(size = ZenithButtonSize.Large) {
                ZenithButtonWeighted(
                    onClick = onDismiss,
                    text = stringResource(R.string.performance_cancel),
                    type = ZenithButtonType.Outlined,
                    isLast = false,
                    size = ZenithButtonSize.ExtraLarge
                )
                ZenithButtonWeighted(
                    onClick = onConfirm,
                    text = stringResource(R.string.performance_use_anyway),
                    type = ZenithButtonType.Filled,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    isFirst = false,
                    size = ZenithButtonSize.ExtraLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun fmtDur(ms: Long): String {
    return when {
        ms >= 3600000 -> {
            val h = ms / 3600000
            val m = (ms % 3600000) / 60000
            if (m > 0) stringResource(R.string.general_duration_hours_minutes_short, h, m)
            else stringResource(R.string.general_duration_hours_short, h)
        }
        ms >= 60000 -> {
            val m = ms / 60000
            val s = (ms % 60000) / 1000
            if (s > 0) stringResource(R.string.general_duration_minutes_seconds_short, m, s)
            else stringResource(R.string.general_duration_minutes_short, m)
        }
        ms >= 1000 -> stringResource(R.string.general_duration_seconds_short, ms / 1000)
        else -> stringResource(R.string.main2_perf_duration_ms, ms)
    }
}

@Composable
private fun SliderItem(
    label: String,
    desc: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    icon: ImageVector,
    steps: Int = 0,
    onChange: (Float) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(value) }
    val animatable = remember { Animatable(value) }

    LaunchedEffect(value) {
        if (!isDragging) {
            try {
                animatable.animateTo(value, spring(0.8f, 400f))
            } catch (_: Exception) { }
        }
    }

    val displayValue = if (isDragging) dragValue else animatable.value

    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = desc, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = fmtVal(displayValue, unit), style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(32.dp))
            Slider(
                value = displayValue.coerceIn(range),
                onValueChange = {
                    isDragging = true
                    dragValue = it
                },
                onValueChangeFinished = {
                    scope.launch {
                        animatable.snapTo(dragValue)
                        isDragging = false
                        onChange(dragValue)
                    }
                },
                valueRange = range,
                steps = steps,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun fmtVal(value: Float, unit: String): String {
    return when {
        unit == "min" && value >= 60 -> stringResource(R.string.general_duration_hours_short, (value / 60).toInt())
        unit == "min" && value >= 1 -> stringResource(R.string.general_duration_minutes_short, value.toInt())
        unit == "min" && value < 1 -> stringResource(R.string.general_duration_seconds_short, (value * 60).toInt())
        unit == "s" && value >= 60 -> {
            val m = (value / 60).toInt()
            val s = (value % 60).toInt()
            if (s > 0) stringResource(R.string.general_duration_minutes_seconds_short, m, s)
            else stringResource(R.string.general_duration_minutes_short, m)
        }
        else -> stringResource(R.string.general_duration_seconds_short, value.toInt())
    }
}

@Composable
private fun activeConfigSummary(cfg: PerformanceConfig): String {
    val activeDuration = fmtDur(cfg.a11yActiveDelay)
    val hiddenDuration = fmtDur(cfg.a11yInactiveDelay)
    val saveDuration = fmtDur(cfg.shieldDbWriteMs)
    val active = stringResource(R.string.main2_perf_summary_active, activeDuration)
    val hidden = stringResource(R.string.main2_perf_summary_hidden, hiddenDuration)
    val save = stringResource(R.string.main2_perf_summary_save, saveDuration)
    return listOf(active, hidden, save).joinToString(" · ")
}

private enum class TuningGroupPosition {
    Top, Middle, Bottom, Single
}

@Composable
private fun TuningPermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    position: TuningGroupPosition = TuningGroupPosition.Middle,
    onInfoClick: (() -> Unit)? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bgColor"
    )

    val shape = when (position) {
        TuningGroupPosition.Top -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        TuningGroupPosition.Middle -> RoundedCornerShape(8.dp)
        TuningGroupPosition.Bottom -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        TuningGroupPosition.Single -> RoundedCornerShape(24.dp)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGranted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            if (isGranted) {
                Icon(
                    Icons.Outlined.CheckCircle,
                    stringResource(R.string.performance_granted),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onInfoClick != null) {
                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.performance_info),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Text(
                            text = stringResource(R.string.performance_grant),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

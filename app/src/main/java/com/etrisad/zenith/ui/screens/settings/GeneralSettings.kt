package com.etrisad.zenith.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.ZenithButtonWeighted
import com.etrisad.zenith.ui.components.ZenithGroupedButton
import com.etrisad.zenith.ui.components.focus.TimePickerDialog
import kotlinx.coroutines.launch

@Composable
fun GeneralSettings(
    preferences: UserPreferences,
    onSetTarget: (Int) -> Unit,
    onSetEmergencyRecharge: (Int) -> Unit,
    onSetDelayAppDuration: (Int) -> Unit,
    onSetDayStartTime: (Int, Int) -> Unit,
    onShowWhitelistSheetChange: (Boolean) -> Unit,
    onShowAppExclusionsSheetChange: (Boolean) -> Unit,
    onOpenPermissions: () -> Unit,
    permissionsMissing: Boolean = false,
    onOpenGuidedPath: () -> Unit = {}
) {
    var showTargetSheet by remember { mutableStateOf(false) }
    var showEmergencyRechargeSheet by remember { mutableStateOf(false) }
    var showDelayAppSheet by remember { mutableStateOf(false) }
    var showDayStartSheet by remember { mutableStateOf(false) }
    val exclusionSummary = appExclusionSummary(
        preferences.privatePackages,
        preferences.excludedFromTrackingPackages
    )

    val permissionsSummary = if (permissionsMissing) {
        stringResource(R.string.general_permissions_summary_missing)
    } else {
        stringResource(R.string.general_permissions_summary)
    }

    val rechargeMinutes = preferences.emergencyRechargeDurationMinutes
    val rechargeLabel = when {
        rechargeMinutes >= 1440 -> {
            val days = rechargeMinutes / 1440
            if (days > 1) stringResource(R.string.general_unit_days, days)
            else stringResource(R.string.general_unit_day, days)
        }
        rechargeMinutes >= 60 -> {
            val hours = rechargeMinutes / 60
            if (hours > 1) stringResource(R.string.general_unit_hours, hours)
            else stringResource(R.string.general_unit_hour, hours)
        }
        else -> stringResource(R.string.general_unit_minutes, rechargeMinutes)
    }
    val emergencyRechargeSummary = stringResource(R.string.general_recharge_period, rechargeLabel)

    val targetMinutes = preferences.screenTimeTargetMinutes
    val targetSummary = if (targetMinutes > 0) {
        val h = targetMinutes / 60
        val m = targetMinutes % 60
        if (h > 0) stringResource(R.string.general_target_set_hours, h, m)
        else stringResource(R.string.general_target_set_minutes, m)
    } else {
        stringResource(R.string.general_no_target_set)
    }

    val delaySeconds = preferences.delayAppDurationSeconds
    val delayLabel = when {
        delaySeconds >= 3600 -> stringResource(R.string.general_duration_hours_short, delaySeconds / 3600)
        delaySeconds >= 60 -> stringResource(R.string.general_duration_minutes_short, delaySeconds / 60)
        else -> stringResource(R.string.general_duration_seconds_short, delaySeconds)
    }
    val delaySummary = stringResource(R.string.general_wait_before_reopening, delayLabel)

    val dayStartSummary = if (preferences.dayStartHour == 0 && preferences.dayStartMinute == 0) {
        stringResource(R.string.general_day_resets_midnight)
    } else {
        stringResource(
            R.string.general_day_resets_at,
            String.format("%02d:%02d", preferences.dayStartHour, preferences.dayStartMinute)
        )
    }

    val bypassSummary = if (preferences.whitelistedPackages.isEmpty()) {
        stringResource(R.string.general_bypass_summary_none)
    } else {
        stringResource(R.string.general_bypass_summary_count, preferences.whitelistedPackages.size)
    }

    Column {
        PreferenceCategory(title = stringResource(R.string.general_general))

        SettingsActionItem(
            title = stringResource(R.string.general_permissions),
            summary = permissionsSummary,
            onClick = onOpenPermissions,
            icon = Icons.Outlined.Security,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            trailing = if (permissionsMissing) {
                {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.onboarding_open_path),
            summary = stringResource(R.string.onboarding_open_path_summary),
            onClick = onOpenGuidedPath,
            icon = Icons.Outlined.Explore,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.general_emergency_recharge_time),
            summary = emergencyRechargeSummary,
            onClick = { showEmergencyRechargeSheet = true },
            icon = Icons.Outlined.Shield,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.general_daily_screen_time_target),
            summary = targetSummary,
            onClick = { showTargetSheet = true },
            icon = Icons.Outlined.Edit,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.general_app_opening_delay),
            summary = delaySummary,
            onClick = { showDelayAppSheet = true },
            icon = Icons.Outlined.History,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.general_day_start_time),
            summary = dayStartSummary,
            onClick = { showDayStartSheet = true },
            icon = Icons.Outlined.Schedule,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.general_bypass_restrictions),
            summary = bypassSummary,
            onClick = { onShowWhitelistSheetChange(true) },
            icon = Icons.Outlined.VerifiedUser,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = stringResource(R.string.general_private_hidden_apps),
            summary = exclusionSummary,
            onClick = { onShowAppExclusionsSheetChange(true) },
            icon = Icons.Outlined.Security,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.settings_language_category))
        LanguageSettingsItem()
    }

    if (showTargetSheet) {
        com.etrisad.zenith.ui.screens.home.ScreenTimeTargetBottomSheet(
            initialMinutes = preferences.screenTimeTargetMinutes,
            onDismiss = { showTargetSheet = false },
            onSave = { minutes: Int ->
                onSetTarget(minutes)
                showTargetSheet = false
            }
        )
    }

    if (showEmergencyRechargeSheet) {
        EmergencyRechargeBottomSheet(
            initialMinutes = preferences.emergencyRechargeDurationMinutes,
            onDismiss = { showEmergencyRechargeSheet = false },
            onSave = { minutes: Int ->
                onSetEmergencyRecharge(minutes)
                showEmergencyRechargeSheet = false
            }
        )
    }

    if (showDelayAppSheet) {
        DelayAppBottomSheet(
            initialSeconds = preferences.delayAppDurationSeconds,
            onDismiss = { showDelayAppSheet = false },
            onSave = { seconds: Int ->
                onSetDelayAppDuration(seconds)
                showDelayAppSheet = false
            }
        )
    }

    if (showDayStartSheet) {
        DayStartTimeBottomSheet(
            initialHour = preferences.dayStartHour,
            initialMinute = preferences.dayStartMinute,
            onDismiss = { showDayStartSheet = false },
            onSave = { hour: Int, minute: Int ->
                onSetDayStartTime(hour, minute)
                showDayStartSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DelayAppBottomSheet(
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var seconds by remember { mutableIntStateOf(initialSeconds) }

    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "containerColor"
    )

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.general_app_opening_delay),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.general_delay_sheet_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = containerColor
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.general_delay_duration),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { if (seconds >= 5) seconds -= 5 },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineMedium)
                            }
                            Text(
                                text = if (seconds >= 60) {
                                    stringResource(
                                        R.string.general_duration_minutes_seconds_short,
                                        seconds / 60,
                                        seconds % 60
                                    )
                                } else {
                                    stringResource(R.string.general_duration_seconds_short, seconds)
                                },
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            IconButton(
                                onClick = { if (seconds < 3600) seconds += 5 },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                ZenithGroupedButton(size = ZenithButtonSize.Small) {
                    val presets = listOf(15, 30, 60)
                    presets.forEachIndexed { index, preset ->
                        val isSelected = seconds == preset
                        val label = stringResource(R.string.general_duration_seconds_short, preset)
                        val pShape = when (index) {
                            0 -> RoundedCornerShape(bottomStart = 28.dp, topStart = 8.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                            presets.lastIndex -> RoundedCornerShape(bottomEnd = 28.dp, topEnd = 8.dp, topStart = 8.dp, bottomStart = 8.dp)
                            else -> RoundedCornerShape(8.dp)
                        }
                        ZenithButtonWeighted(
                            onClick = { seconds = preset },
                            text = label,
                            type = if (isSelected) ZenithButtonType.Filled else ZenithButtonType.Tonal,
                            size = ZenithButtonSize.Small,
                            selected = isSelected,
                            shape = pShape,
                            isFirst = index == 0,
                            isLast = index == presets.lastIndex,
                            contentScaleEnabled = false
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            startY = 0f,
                            endY = 40f
                        )
                    )
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                ZenithButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onSave(seconds)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.general_save_delay)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyRechargeBottomSheet(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var minutes by remember { mutableIntStateOf(initialMinutes) }

    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "containerColor"
    )

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.general_emergency_recharge),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.general_emergency_recharge_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = containerColor
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.general_recharge_duration),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { if (minutes >= 5) minutes -= 5 },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineMedium)
                            }
                            Text(
                                text = when {
                                    minutes >= 1440 -> {
                                        val days = minutes / 1440
                                        val restHours = (minutes % 1440) / 60
                                        if (minutes % 1440 > 0) {
                                            stringResource(R.string.general_duration_days_hours_short, days, restHours)
                                        } else {
                                            stringResource(R.string.general_duration_days_short, days)
                                        }
                                    }
                                    minutes >= 60 -> {
                                        val hours = minutes / 60
                                        val restMinutes = minutes % 60
                                        if (minutes % 60 > 0) {
                                            stringResource(R.string.general_duration_hours_minutes_short, hours, restMinutes)
                                        } else {
                                            stringResource(R.string.general_duration_hours_short, hours)
                                        }
                                    }
                                    else -> stringResource(R.string.general_duration_minutes_short, minutes)
                                },
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            IconButton(
                                onClick = { if (minutes < 1440) minutes += 5 },
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                ZenithGroupedButton(size = ZenithButtonSize.Small) {
                    val presets = listOf(60, 180, 1440)
                    presets.forEachIndexed { index, preset ->
                        val isSelected = minutes == preset
                        val label = when {
                            preset >= 1440 -> stringResource(R.string.general_duration_days_short, preset / 1440)
                            preset >= 60 -> stringResource(R.string.general_duration_hours_short, preset / 60)
                            else -> stringResource(R.string.general_duration_minutes_short, preset)
                        }
                        val pShape = when (index) {
                            0 -> RoundedCornerShape(bottomStart = 28.dp, topStart = 8.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                            presets.lastIndex -> RoundedCornerShape(bottomEnd = 28.dp, topEnd = 8.dp, topStart = 8.dp, bottomStart = 8.dp)
                            else -> RoundedCornerShape(8.dp)
                        }
                        ZenithButtonWeighted(
                            onClick = { minutes = preset },
                            text = label,
                            type = if (isSelected) ZenithButtonType.Filled else ZenithButtonType.Tonal,
                            size = ZenithButtonSize.Small,
                            selected = isSelected,
                            shape = pShape,
                            isFirst = index == 0,
                            isLast = index == presets.lastIndex,
                            contentScaleEnabled = false
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            startY = 0f,
                            endY = 40f
                        )
                    )
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                ZenithButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onSave(minutes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.general_save_duration)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayStartTimeBottomSheet(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentLocale = androidx.compose.ui.text.intl.Locale.current.platformLocale

    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "containerColor"
    )

    val scrollState = rememberScrollState()

    var showTimePicker by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute,
            is24Hour = true
        )
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedHour = timePickerState.hour
                selectedMinute = timePickerState.minute
                showTimePicker = false
            }
        ) {
            MaterialTheme(
                typography = MaterialTheme.typography.copy(
                    displayLarge = MaterialTheme.typography.headlineLarge
                )
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.general_day_start_time),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.general_day_start_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    color = containerColor
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.general_day_resets_at_label),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(currentLocale, "%02d:%02d", selectedHour, selectedMinute),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                ZenithGroupedButton(size = ZenithButtonSize.Small) {
                    val presets = listOf(0 to 0, 6 to 0, 9 to 0, 12 to 0)
                    presets.forEachIndexed { index, (ph, pm) ->
                        val isSelected = selectedHour == ph && selectedMinute == pm
                        val label = if (ph == 0 && pm == 0) {
                            stringResource(R.string.general_midnight)
                        } else {
                            String.format("%02d:%02d", ph, pm)
                        }
                        val pShape = when (index) {
                            0 -> RoundedCornerShape(bottomStart = 28.dp, topStart = 8.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                            presets.lastIndex -> RoundedCornerShape(bottomEnd = 28.dp, topEnd = 8.dp, topStart = 8.dp, bottomStart = 8.dp)
                            else -> RoundedCornerShape(8.dp)
                        }
                        ZenithButtonWeighted(
                            onClick = {
                                selectedHour = ph
                                selectedMinute = pm
                            },
                            text = label,
                            type = if (isSelected) ZenithButtonType.Filled else ZenithButtonType.Tonal,
                            size = ZenithButtonSize.Small,
                            selected = isSelected,
                            shape = pShape,
                            isFirst = index == 0,
                            isLast = index == presets.lastIndex,
                            contentScaleEnabled = false
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            startY = 0f,
                            endY = 40f
                        )
                    )
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                ZenithButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onSave(selectedHour, selectedMinute)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.general_save_day_start_time)
                )
            }
        }
    }
}

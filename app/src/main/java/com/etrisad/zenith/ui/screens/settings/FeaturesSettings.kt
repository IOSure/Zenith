package com.etrisad.zenith.ui.screens.settings

import android.widget.Toast
import android.app.WallpaperManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.ui.onboarding.onboardingAnchor
import com.etrisad.zenith.data.preferences.ForegroundNotificationStatusMode
import com.etrisad.zenith.data.preferences.MindfulGatewayDefaults
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.util.hasCalendarPermission
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonWeighted
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.ZenithGroupedButton
import com.etrisad.zenith.ui.components.ZenithToggleButtonGroup
import com.etrisad.zenith.ui.components.ZenithToggleOption
import com.etrisad.zenith.ui.components.focus.CardGroup
import com.etrisad.zenith.ui.components.focus.ZenithDropdown
import com.etrisad.zenith.ui.navigation.Screen
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun FeaturesSettings(
    preferences: UserPreferences,
    onTotalUsagePillEnabledChange: (Boolean) -> Unit,
    onForegroundNotificationStatusModeChange: (ForegroundNotificationStatusMode) -> Unit,
    onSessionUsageOverlayEnabledChange: (Boolean) -> Unit,
    onSessionUsageOverlaySizeChange: (Int) -> Unit,
    onSessionUsageOverlayOpacityChange: (Int) -> Unit,
    onUsageGlimpseEnabledChange: (Boolean) -> Unit,
    onAwakeOverlayEnabledChange: (Boolean) -> Unit,
    onAwakeOverlayTriggerMinutesChange: (Int) -> Unit,
    onAwakeOverlayHideMinutesChange: (Int) -> Unit,
    onNavigateToEyeCare: () -> Unit,
    onMindfulGatewayEnabledChange: (Boolean) -> Unit,
    onEarlyKickEnabledChange: (Boolean) -> Unit,
    onInterceptAudioFocusEnabledChange: (Boolean) -> Unit,
    onBatteryStatsResetEnabledChange: (Boolean) -> Unit,
    onShowCurrentEventEnabledChange: (Boolean) -> Unit,
    onDailyRecapEnabledChange: (Boolean) -> Unit,
    onWeeklyInsightEnabledChange: (Boolean) -> Unit,
    onTrendMilestoneEnabledChange: (Boolean) -> Unit,
    onIncentiveLockEnabledChange: (Boolean) -> Unit,
    onIncentiveLockDisableRequest: () -> Unit,
    onIncentiveLockCancelDisableRequest: () -> Unit,
    onNavigateToGracePeriod: () -> Unit,
    onWebsiteAutoTrackingEnabledChange: (Boolean) -> Unit,
    onWebsiteBlockerBlockAdultChange: (Boolean) -> Unit,
    onWebsiteBlockerKeywordsChange: (List<String>) -> Unit,
    onNavigateToLockdown: () -> Unit,
    onNavigateToTamperProtection: () -> Unit,
    onNavigateToPomodoro: () -> Unit,
    onNavigateToPausePoint: () -> Unit = {},
    onMindfulGatewayDefaultsSave: (
        maxUses: Int,
        refreshMinutes: Int,
        maxEmergencyUses: Int,
        remindersEnabled: Boolean,
        strictModeEnabled: Boolean,
        pausePointEnabled: Boolean,
        endOverlayEnabled: Boolean,
        autoQuitEnabled: Boolean,
    ) -> Unit,
    onNavigateToReelsShield: () -> Unit = {},
    onNavigateToRedirectShield: () -> Unit = {},
    goalCount: Int
) {
    val context = LocalContext.current
    val calendarPermissionGranted = remember { hasCalendarPermission(context) }
    var showConfirmSheet by remember { mutableStateOf(false) }
    var showGatewayDefaultsSheet by remember { mutableStateOf(false) }
    var showKeywordsSheet by remember { mutableStateOf(false) }

    if (showKeywordsSheet) {
        WebsiteBlockerKeywordsBottomSheet(
            initialKeywords = preferences.websiteBlockerKeywords,
            onDismiss = { showKeywordsSheet = false },
            onSave = onWebsiteBlockerKeywordsChange
        )
    }

    if (showGatewayDefaultsSheet) {
        MindfulGatewayDefaultsBottomSheet(
            initialMaxUses = preferences.mindfulGatewayMaxUses,
            initialRefreshMinutes = preferences.mindfulGatewayRefreshMinutes,
            initialMaxEmergencyUses = preferences.mindfulGatewayMaxEmergencyUses,
            initialRemindersEnabled = preferences.mindfulGatewayRemindersEnabled,
            initialStrictModeEnabled = preferences.mindfulGatewayStrictModeEnabled,
            initialPausePointEnabled = preferences.mindfulGatewayPausePointEnabled,
            initialEndOverlayEnabled = preferences.mindfulGatewayEndOverlayEnabled,
            initialAutoQuitEnabled = preferences.mindfulGatewayAutoQuitEnabled,
            onDismiss = { showGatewayDefaultsSheet = false },
            onSave = onMindfulGatewayDefaultsSave
        )
    }

    if (showConfirmSheet) {
        com.etrisad.zenith.ui.components.ConfirmBottomSheet(
            onDismiss = { showConfirmSheet = false },
            onConfirm = {
                onIncentiveLockDisableRequest()
                showConfirmSheet = false
            },
            leverCount = 10,
            showTimeSelection = false
        )
    }

    Column {
        PreferenceCategory(title = stringResource(R.string.features_category_interface_overlays))

        SettingsToggle(
            title = stringResource(R.string.features_total_usage_pill_title),
            description = stringResource(R.string.features_total_usage_pill_description),
            checked = preferences.totalUsagePillEnabled,
            onCheckedChange = onTotalUsagePillEnabledChange,
            icon = Icons.Outlined.Public,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_session_usage_overlay_title),
            description = stringResource(R.string.features_session_usage_overlay_description),
            checked = preferences.sessionUsageOverlayEnabled,
            onCheckedChange = onSessionUsageOverlayEnabledChange,
            icon = Icons.Outlined.Timer,
            shape = RoundedCornerShape(8.dp)
        )

        AnimatedVisibility(
            visible = preferences.sessionUsageOverlayEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                HUDAppearanceSettings(
                    size = preferences.sessionUsageOverlaySize,
                    opacity = preferences.sessionUsageOverlayOpacity,
                    onSizeChange = onSessionUsageOverlaySizeChange,
                    onOpacityChange = onSessionUsageOverlayOpacityChange,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_usage_glimpse_title),
            description = stringResource(R.string.features_usage_glimpse_description),
            checked = preferences.usageGlimpseEnabled,
            onCheckedChange = onUsageGlimpseEnabledChange,
            icon = Icons.Outlined.Visibility,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_awake_overlay_title),
            description = stringResource(R.string.features_awake_overlay_description),
            checked = preferences.awakeOverlayEnabled,
            onCheckedChange = onAwakeOverlayEnabledChange,
            icon = Icons.Outlined.Alarm,
            shape = RoundedCornerShape(8.dp)
        )

        AnimatedVisibility(
            visible = preferences.awakeOverlayEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                AwakeOverlayTuning(
                    triggerMinutes = preferences.awakeOverlayTriggerMinutes,
                    hideMinutes = preferences.awakeOverlayHideMinutes,
                    onTriggerChange = onAwakeOverlayTriggerMinutesChange,
                    onHideChange = onAwakeOverlayHideMinutesChange
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_show_current_event_title),
            description = if (calendarPermissionGranted) stringResource(R.string.features_show_current_event_description) else stringResource(R.string.features_show_current_event_description_no_permission),
            checked = preferences.showCurrentEvent,
            onCheckedChange = { enabled ->
                if (calendarPermissionGranted) {
                    onShowCurrentEventEnabledChange(enabled)
                } else {
                    Toast.makeText(context, context.getString(R.string.features_enable_calendar_access_permission), Toast.LENGTH_SHORT).show()
                }
            },
            icon = Icons.Outlined.CalendarMonth,
            enabled = calendarPermissionGranted,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_notifications))

        ForegroundNotificationStatusSelector(
            selectedMode = preferences.foregroundNotificationStatusMode,
            onModeChange = onForegroundNotificationStatusModeChange,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_daily_focus_recap_title),
            description = stringResource(R.string.features_daily_focus_recap_description),
            checked = preferences.dailyRecapEnabled,
            onCheckedChange = onDailyRecapEnabledChange,
            icon = Icons.Outlined.Today,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_weekly_insight_title),
            description = stringResource(R.string.features_weekly_insight_description),
            checked = preferences.weeklyInsightEnabled,
            onCheckedChange = onWeeklyInsightEnabledChange,
            icon = Icons.Outlined.Assessment,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_trend_milestones_title),
            description = stringResource(R.string.features_trend_milestones_description),
            checked = preferences.trendMilestoneEnabled,
            onCheckedChange = onTrendMilestoneEnabledChange,
            icon = Icons.Outlined.EmojiEvents,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_entry_control))

        Box(modifier = Modifier.onboardingAnchor("features.mindful_gateway")) {
            SettingsToggle(
                title = stringResource(R.string.features_mindful_gateway_title),
                description = stringResource(R.string.features_mindful_gateway_description),
                checked = preferences.mindfulGatewayEnabled,
                onCheckedChange = onMindfulGatewayEnabledChange,
                icon = Icons.Outlined.AutoFixHigh,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )
        }

        AnimatedVisibility(
            visible = preferences.mindfulGatewayEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = stringResource(R.string.features_mindful_gateway_defaults_title),
                    summary = stringResource(R.string.features_mindful_gateway_defaults_summary),
                    onClick = { showGatewayDefaultsSheet = true },
                    icon = Icons.Outlined.Tune,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        val isDisablingIncentiveLock = preferences.incentiveLockDisableRequestTimestamp > 0

        SettingsToggle(
            title = stringResource(R.string.features_incentive_lock_title),
            description = stringResource(R.string.features_incentive_lock_description),
            checked = preferences.incentiveLockEnabled,
            onCheckedChange = { enabled ->
                if (enabled) {
                    if (goalCount > 0) {
                        onIncentiveLockEnabledChange(true)
                    } else {
                        Toast.makeText(context, context.getString(R.string.features_add_app_goal_first), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    showConfirmSheet = true
                }
            },
            icon = Icons.Outlined.Lock,
            enabled = (preferences.incentiveLockEnabled || goalCount > 0) && !isDisablingIncentiveLock,
            shape = RoundedCornerShape(8.dp)
        )

        AnimatedVisibility(
            visible = isDisablingIncentiveLock,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val deactivationTime = preferences.incentiveLockDisableRequestTimestamp + 3600000L
            var timeLeftMillis by remember(preferences.incentiveLockDisableRequestTimestamp) { 
                mutableLongStateOf(deactivationTime - System.currentTimeMillis()) 
            }
            
            LaunchedEffect(preferences.incentiveLockDisableRequestTimestamp) {
                if (preferences.incentiveLockDisableRequestTimestamp > 0) {
                    while (timeLeftMillis > 0) {
                        timeLeftMillis = deactivationTime - System.currentTimeMillis()
                        if (timeLeftMillis <= 0) {
                            onIncentiveLockEnabledChange(false)
                            onIncentiveLockCancelDisableRequest()
                        }
                        delay(1000)
                    }
                }
            }

            Column {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.features_disabling_incentive_lock),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            val minutesLeft = (timeLeftMillis / 60000).coerceAtLeast(0)
                            val secondsLeft = ((timeLeftMillis % 60000) / 1000).coerceAtLeast(0)
                            val timeStr = String.format("%02d:%02d", minutesLeft, secondsLeft)
                            val targetTimeStr = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(deactivationTime))
                            Text(
                                stringResource(R.string.features_incentive_lock_disable_countdown, timeStr, targetTimeStr),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = onIncentiveLockCancelDisableRequest) {
                            Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.features_cancel), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.onboardingAnchor("features.grace_period")) {
            SettingsActionItem(
                title = stringResource(R.string.features_grace_period_title),
                summary = stringResource(R.string.features_grace_period_summary),
                onClick = onNavigateToGracePeriod,
                icon = Icons.Outlined.FreeBreakfast,
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.onboardingAnchor("features.eye_care")) {
            SettingsActionItem(
                title = stringResource(R.string.features_eye_care_title),
                summary = stringResource(R.string.features_eye_care_summary, preferences.eyeCareWorkMinutes),
                onClick = onNavigateToEyeCare,
                icon = Icons.Outlined.RemoveRedEye,
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.onboardingAnchor("features.pomodoro")) {
            SettingsActionItem(
                title = stringResource(R.string.features_pomodoro_title),
                summary = stringResource(R.string.features_pomodoro_summary),
                onClick = onNavigateToPomodoro,
                icon = Icons.Outlined.Visibility,
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.onboardingAnchor("features.lockdown")) {
            SettingsActionItem(
                title = stringResource(R.string.features_lockdown_title),
                summary = if (preferences.lockdownEnabled && preferences.isInLockdown()) stringResource(R.string.features_lockdown_summary_active) else stringResource(R.string.features_lockdown_summary),
                onClick = onNavigateToLockdown,
                icon = Icons.Outlined.Lock,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_short_video))

        Box(modifier = Modifier.onboardingAnchor("features.reels_shield")) {
            SettingsActionItem(
                title = stringResource(R.string.features_reels_shield_title),
                summary = if (preferences.reelsShieldEnabled) {
                    stringResource(R.string.features_reels_shield_summary_on, preferences.reelsShieldDailyLimit, preferences.reelsShieldPackages.size)
                } else {
                    stringResource(R.string.features_reels_shield_summary)
                },
                onClick = onNavigateToReelsShield,
                icon = Icons.Outlined.SwapVert,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_protection))

        SettingsActionItem(
            title = stringResource(R.string.features_redirect_shield_title),
            summary = if (preferences.redirectShieldEnabled) {
                stringResource(R.string.features_redirect_shield_summary_on, preferences.redirectShieldApps.size, preferences.redirectShieldTriggerApps.size)
            } else {
                stringResource(R.string.features_redirect_shield_summary)
            },
            onClick = onNavigateToRedirectShield,
            icon = Icons.Outlined.Security,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsActionItem(
            title = stringResource(R.string.features_tamper_protection_title),
            summary = if (preferences.tamperProtectionEnabled) stringResource(R.string.features_tamper_protection_summary_on) else stringResource(R.string.features_tamper_protection_summary),
            onClick = onNavigateToTamperProtection,
            icon = Icons.Outlined.AdminPanelSettings,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_pause_point))

        Box(modifier = Modifier.onboardingAnchor("features.pause_point")) {
            SettingsActionItem(
                title = stringResource(R.string.features_pause_point_title),
                summary = if (preferences.pausePointEnabled) {
                    val count = preferences.pausePointTaskTypes.size
                    if (count == 0) stringResource(R.string.features_pause_point_summary_enabled_no_types)
                    else stringResource(R.string.features_pause_point_summary_enabled, count)
                } else stringResource(R.string.features_pause_point_summary_disabled),
                onClick = onNavigateToPausePoint,
                icon = Icons.Outlined.PauseCircle,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_triggers))

        SettingsToggle(
            title = stringResource(R.string.features_pause_media_title),
            description = stringResource(R.string.features_pause_media_description),
            checked = preferences.interceptAudioFocusEnabled,
            onCheckedChange = onInterceptAudioFocusEnabledChange,
            icon = Icons.Outlined.MusicNote,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.onboardingAnchor("features.early_kick")) {
            SettingsToggle(
                title = stringResource(R.string.features_early_kick_title),
                description = stringResource(R.string.features_early_kick_description),
                checked = preferences.earlyKickEnabled,
                onCheckedChange = onEarlyKickEnabledChange,
                icon = Icons.Outlined.ExitToApp,
                shape = RoundedCornerShape(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = stringResource(R.string.features_battery_usage_reset_title),
            description = stringResource(R.string.features_battery_usage_reset_description),
            checked = preferences.batteryStatsResetEnabled,
            onCheckedChange = onBatteryStatsResetEnabledChange,
            icon = Icons.Outlined.BatteryChargingFull,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.features_category_website))

        Box(modifier = Modifier.onboardingAnchor("features.website_tracking")) {
            SettingsToggle(
                title = stringResource(R.string.features_auto_track_websites_title),
                description = stringResource(R.string.features_auto_track_websites_description),
                checked = preferences.websiteAutoTrackingEnabled,
                onCheckedChange = onWebsiteAutoTrackingEnabledChange,
                icon = Icons.Outlined.Language,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        SettingsToggle(
            title = stringResource(R.string.features_block_adult_websites_title),
            description = stringResource(R.string.features_block_adult_websites_description),
            checked = preferences.websiteBlockerBlockAdult,
            onCheckedChange = onWebsiteBlockerBlockAdultChange,
            icon = Icons.Outlined.Security,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsActionItem(
            title = stringResource(R.string.features_blocked_keywords_title),
            summary = stringResource(R.string.features_blocked_keywords_summary, preferences.websiteBlockerKeywords.size),
            onClick = { showKeywordsSheet = true },
            icon = Icons.Outlined.Description,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )
    }
}

@Composable
fun ForegroundNotificationStatusSelector(
    selectedMode: ForegroundNotificationStatusMode,
    onModeChange: (ForegroundNotificationStatusMode) -> Unit,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.features_status_notification_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.features_status_notification_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val modeOptions = listOf(
                ForegroundNotificationStatusMode.DAILY_USAGE to stringResource(R.string.features_notification_mode_usage),
                ForegroundNotificationStatusMode.ACTIVE_FOCUS to stringResource(R.string.features_notification_mode_focus),
                ForegroundNotificationStatusMode.DEFAULT to stringResource(R.string.features_notification_mode_default)
            )

            ZenithToggleButtonGroup(
                options = modeOptions.map { ZenithToggleOption(text = it.second) },
                selectedIndices = setOf(modeOptions.indexOfFirst { it.first == selectedMode }.coerceAtLeast(0)),
                onToggle = { index -> onModeChange(modeOptions[index].first) },
                size = ZenithButtonSize.Medium,
                isInsideContainer = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HUDAppearanceSettings(
    size: Int,
    opacity: Int,
    onSizeChange: (Int) -> Unit,
    onOpacityChange: (Int) -> Unit,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    val context = LocalContext.current
    val wallpaperDrawable = remember {
        try {
            WallpaperManager.getInstance(context).drawable
        } catch (_: Exception) {
            null
        }
    }

    var localSize by remember(size) { mutableFloatStateOf(size.toFloat()) }
    var localOpacity by remember(opacity) { mutableFloatStateOf(opacity.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.features_hud_appearance_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PhotoSizeSelectSmall, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = localSize,
                    onValueChange = { localSize = it },
                    onValueChangeFinished = { onSizeChange(localSize.toInt()) },
                    valueRange = 50f..200f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                Icon(Icons.Outlined.PhotoSizeSelectLarge, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(R.string.features_hud_size, localSize.toInt()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Opacity, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = localOpacity,
                    onValueChange = { localOpacity = it },
                    onValueChangeFinished = { onOpacityChange(localOpacity.toInt()) },
                    valueRange = 20f..100f,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                Icon(Icons.Outlined.Contrast, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(R.string.features_hud_opacity, localOpacity.toInt()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.features_hud_preview_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (wallpaperDrawable != null) {
                    androidx.compose.foundation.Image(
                        painter = rememberDrawablePainter(wallpaperDrawable),
                        contentDescription = null,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
                }

                val animatedScale by animateFloatAsState(
                    targetValue = localSize / 100f,
                    label = "HUDScale"
                )
                val animatedOpacity by animateFloatAsState(
                    targetValue = localOpacity / 100f,
                    label = "HUDOpacity"
                )

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            alpha = animatedOpacity
                        }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularWavyProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        stroke = Stroke(width = 6.dp.value),
                        trackStroke = Stroke(width = 6.dp.value),
                        wavelength = 20.dp,
                        waveSpeed = 0.dp
                    )
                    Text(
                        text = stringResource(R.string.features_hud_preview_time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindfulGatewayDefaultsBottomSheet(
    initialMaxUses: Int,
    initialRefreshMinutes: Int,
    initialMaxEmergencyUses: Int,
    initialRemindersEnabled: Boolean,
    initialStrictModeEnabled: Boolean,
    initialPausePointEnabled: Boolean,
    initialEndOverlayEnabled: Boolean,
    initialAutoQuitEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "containerColor"
    )

    var maxUsesText by remember { mutableStateOf(initialMaxUses.toString()) }
    var refreshMinutes by remember { mutableIntStateOf(initialRefreshMinutes) }
    var maxEmergencyText by remember { mutableStateOf(initialMaxEmergencyUses.toString()) }
    var remindersEnabled by remember { mutableStateOf(initialRemindersEnabled) }
    var strictModeEnabled by remember { mutableStateOf(initialStrictModeEnabled) }
    var pausePointEnabled by remember { mutableStateOf(initialPausePointEnabled) }
    var endOverlayEnabled by remember { mutableStateOf(initialEndOverlayEnabled) }
    var autoQuitEnabled by remember { mutableStateOf(initialAutoQuitEnabled) }

    val refreshOptions = listOf(
        stringResource(R.string.features_refresh_every_30_minutes) to 30,
        stringResource(R.string.features_refresh_every_1_hour) to 60,
        stringResource(R.string.features_refresh_every_2_hours) to 120,
        stringResource(R.string.features_refresh_every_6_hours) to 360,
        stringResource(R.string.features_refresh_every_12_hours) to 720,
        stringResource(R.string.features_refresh_every_24_hours) to 1440
    )

    val currentMaxUses = maxUsesText.toIntOrNull() ?: initialMaxUses
    val currentMaxEmergency = maxEmergencyText.toIntOrNull() ?: initialMaxEmergencyUses

    val hasChanges = currentMaxUses != initialMaxUses ||
        refreshMinutes != initialRefreshMinutes ||
        currentMaxEmergency != initialMaxEmergencyUses ||
        remindersEnabled != initialRemindersEnabled ||
        strictModeEnabled != initialStrictModeEnabled ||
        pausePointEnabled != initialPausePointEnabled ||
        endOverlayEnabled != initialEndOverlayEnabled ||
        autoQuitEnabled != initialAutoQuitEnabled

    val topShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    val middleShape = RoundedCornerShape(8.dp)
    val bottomShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 28.dp, bottomEnd = 28.dp)

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
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.features_mindful_gateway_defaults_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.features_mindful_gateway_defaults_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.features_mindful_gateway_defaults_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        PreferenceCategory(title = stringResource(R.string.features_category_limits))

                        CardGroup(shape = topShape, containerColor = containerColor) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.features_times_of_uses_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.features_times_of_uses_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        BasicTextField(
                                            value = maxUsesText,
                                            onValueChange = { if (it.all { char -> char.isDigit() }) maxUsesText = it },
                                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CardGroup(shape = middleShape, containerColor = containerColor) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.features_refresh_period_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.features_refresh_period_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ZenithDropdown(
                                    options = refreshOptions,
                                    selectedOption = refreshMinutes,
                                    onOptionSelected = { refreshMinutes = it },
                                    width = 160.dp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CardGroup(shape = bottomShape, containerColor = containerColor) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Bolt,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.features_emergency_uses_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = stringResource(R.string.features_emergency_uses_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    modifier = Modifier
                                        .width(72.dp)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        BasicTextField(
                                            value = maxEmergencyText,
                                            onValueChange = { if (it.all { char -> char.isDigit() }) maxEmergencyText = it },
                                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                                textAlign = TextAlign.Center,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        PreferenceCategory(title = stringResource(R.string.features_category_settings))

                        CardGroup(shape = topShape, containerColor = containerColor) {
                            SettingsToggle(
                                title = stringResource(R.string.features_show_reminders_title),
                                description = stringResource(R.string.features_show_reminders_description),
                                checked = remindersEnabled,
                                onCheckedChange = { remindersEnabled = it },
                                icon = Icons.Outlined.NotificationsActive,
                                shape = topShape
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CardGroup(shape = middleShape, containerColor = containerColor) {
                            SettingsToggle(
                                title = stringResource(R.string.features_strict_mode_title),
                                description = stringResource(R.string.features_strict_mode_description),
                                checked = strictModeEnabled,
                                onCheckedChange = { strictModeEnabled = it },
                                icon = Icons.Outlined.GppGood,
                                shape = middleShape
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CardGroup(shape = middleShape, containerColor = containerColor) {
                            SettingsToggle(
                                title = stringResource(R.string.features_pause_point_title),
                                description = stringResource(R.string.features_pause_point_description),
                                checked = pausePointEnabled,
                                onCheckedChange = { pausePointEnabled = it },
                                icon = Icons.Outlined.PauseCircle,
                                shape = middleShape
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CardGroup(shape = middleShape, containerColor = containerColor) {
                            SettingsToggle(
                                title = stringResource(R.string.features_end_overlay_title),
                                description = stringResource(R.string.features_end_overlay_description),
                                checked = endOverlayEnabled,
                                onCheckedChange = { value ->
                                    endOverlayEnabled = value
                                    if (!value) autoQuitEnabled = true
                                },
                                icon = Icons.Outlined.Layers,
                                shape = middleShape
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CardGroup(shape = bottomShape, containerColor = containerColor) {
                            SettingsToggle(
                                title = stringResource(R.string.features_auto_quit_title),
                                description = stringResource(R.string.features_auto_quit_description),
                                checked = autoQuitEnabled,
                                onCheckedChange = { value ->
                                    autoQuitEnabled = value
                                    if (!value) endOverlayEnabled = true
                                },
                                icon = Icons.AutoMirrored.Outlined.ExitToApp,
                                shape = bottomShape
                            )
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    listOf(MaterialTheme.colorScheme.surface, Color.Transparent)
                                )
                            )
                    )
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
                ZenithGroupedButton {
                    ZenithButtonWeighted(
                        onClick = {
                            maxUsesText = MindfulGatewayDefaults.MAX_USES_PER_PERIOD.toString()
                            refreshMinutes = MindfulGatewayDefaults.REFRESH_PERIOD_MINUTES
                            maxEmergencyText = MindfulGatewayDefaults.MAX_EMERGENCY_USES.toString()
                            remindersEnabled = MindfulGatewayDefaults.REMINDERS_ENABLED
                            strictModeEnabled = MindfulGatewayDefaults.STRICT_MODE_ENABLED
                            pausePointEnabled = MindfulGatewayDefaults.PAUSE_POINT_ENABLED
                            endOverlayEnabled = MindfulGatewayDefaults.END_OVERLAY_ENABLED
                            autoQuitEnabled = MindfulGatewayDefaults.AUTO_QUIT_ENABLED
                            onSave(
                                MindfulGatewayDefaults.MAX_USES_PER_PERIOD,
                                MindfulGatewayDefaults.REFRESH_PERIOD_MINUTES,
                                MindfulGatewayDefaults.MAX_EMERGENCY_USES,
                                MindfulGatewayDefaults.REMINDERS_ENABLED,
                                MindfulGatewayDefaults.STRICT_MODE_ENABLED,
                                MindfulGatewayDefaults.PAUSE_POINT_ENABLED,
                                MindfulGatewayDefaults.END_OVERLAY_ENABLED,
                                MindfulGatewayDefaults.AUTO_QUIT_ENABLED
                            )
                        },
                        text = stringResource(R.string.features_reset),
                        type = ZenithButtonType.Tonal,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        weight = 1f,
                        isFirst = true,
                        isLast = false
                    )
                    ZenithButtonWeighted(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onSave(
                                    currentMaxUses,
                                    refreshMinutes,
                                    currentMaxEmergency,
                                    remindersEnabled,
                                    strictModeEnabled,
                                    pausePointEnabled,
                                    endOverlayEnabled,
                                    autoQuitEnabled
                                )
                                onDismiss()
                            }
                        },
                        text = stringResource(R.string.features_save_defaults),
                        weight = 1.5f,
                        isFirst = false,
                        isLast = true,
                        enabled = hasChanges
                    )
                }
            }
        }
    }
}

@Composable
private fun AwakeOverlayTuning(
    triggerMinutes: Int,
    hideMinutes: Int,
    onTriggerChange: (Int) -> Unit,
    onHideChange: (Int) -> Unit
) {
    var showTriggerDialog by remember { mutableStateOf(false) }
    var showHideDialog by remember { mutableStateOf(false) }

    SettingsActionItem(
        title = stringResource(R.string.features_awake_overlay_trigger_title),
        summary = stringResource(R.string.features_awake_overlay_trigger_value, triggerMinutes),
        onClick = { showTriggerDialog = true },
        icon = Icons.Outlined.Schedule,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))

    SettingsActionItem(
        title = stringResource(R.string.features_awake_overlay_hide_title),
        summary = stringResource(R.string.features_awake_overlay_hide_value, hideMinutes),
        onClick = { showHideDialog = true },
        icon = Icons.Outlined.Timer,
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    )

    if (showTriggerDialog) {
        MinuteChoiceDialog(
            title = stringResource(R.string.features_awake_overlay_trigger_dialog),
            current = triggerMinutes,
            options = listOf(1, 5, 10, 15, 30, 60),
            onSelect = {
                onTriggerChange(it)
                showTriggerDialog = false
            },
            onDismiss = { showTriggerDialog = false }
        )
    }

    if (showHideDialog) {
        MinuteChoiceDialog(
            title = stringResource(R.string.features_awake_overlay_hide_dialog),
            current = hideMinutes,
            options = listOf(1, 5, 10, 30),
            onSelect = {
                onHideChange(it)
                showHideDialog = false
            },
            onDismiss = { showHideDialog = false }
        )
    }
}

@Composable
private fun MinuteChoiceDialog(
    title: String,
    current: Int,
    options: List<Int>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.height(240.dp)
            ) {
                items(options) { minutes ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(minutes) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = minutes == current, onClick = { onSelect(minutes) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.duration_minutes_short, minutes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_language_cancel))
            }
        }
    )
}

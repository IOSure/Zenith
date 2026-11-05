package com.etrisad.zenith.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.etrisad.zenith.R
import com.etrisad.zenith.data.local.entity.FocusType
import com.etrisad.zenith.data.local.entity.ShieldEntity
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.screens.profile.levelForXp
import com.etrisad.zenith.ui.components.ZenithButtonSize
import com.etrisad.zenith.ui.components.ZenithButtonType
import com.etrisad.zenith.ui.components.focus.AppPickerBottomSheet
import com.etrisad.zenith.ui.components.focus.ZenithDropdown
import com.etrisad.zenith.ui.components.overlay.InterceptBottomSheet
import com.etrisad.zenith.ui.viewmodel.AppInfo
import com.etrisad.zenith.ui.viewmodel.FocusUiState
import com.etrisad.zenith.util.ScreenUsageHelper

@Composable
fun DeveloperSettings(
    preferences: UserPreferences,
    focusUiState: FocusUiState,
    onSearchQueryChange: (String) -> Unit,
    onShowDatabaseIndicatorChange: (Boolean) -> Unit,
    onSmartRepairOnRefreshChange: (Boolean) -> Unit,
    onNavigateToDatabaseDebug: () -> Unit,
    onNavigateToDataRepairment: () -> Unit,
    onTestGoalOverlay: () -> Unit,
    onTestUpdateSheet: () -> Unit,
    onNavigateToFontTest: () -> Unit,
    onNavigateToSystemUsageDebug: () -> Unit,
    onTriggerOnboardingPermissions: () -> Unit,
    onTriggerOnboardingStats: () -> Unit,
    onTriggerOnboardingUpdate: () -> Unit,
    onResetOnboardingGuide: () -> Unit = {},
    onResetBedtimeStreak: () -> Unit,
    onResetStreakRecovery: () -> Unit,
    onUpdateAppStreak: (String, Int) -> Unit,
    onUpdateGlobalScreenTime: (Long) -> Unit,
    onUpdateAppScreenTime: (String, Long) -> Unit,
    onUpdateProfileLevel: (Int) -> Unit = {},
    onResetXpDebug: () -> Unit = {},
    onTestUsageGlimpse: () -> Unit,
    onTestGoalCallerDelayed: () -> Unit,
    onTestAlarmOverlay: () -> Unit,
    onTestAchievementBanner: () -> Unit = {},
    onShowDbLogViewer: () -> Unit = {},
    onShowOverlayLogViewer: () -> Unit = {}
) {
    var showAppPickerForStreak by remember { mutableStateOf(false) }
    var showAppPickerForUsage by remember { mutableStateOf(false) }
    var showGlobalUsageDialog by remember { mutableStateOf(false) }
    var showProfileLevelDialog by remember { mutableStateOf(false) }

    var selectedAppInfo by remember { mutableStateOf<AppInfo?>(null) }
    var showStreakEditSheet by remember { mutableStateOf(false) }
    var showAppUsageEditSheet by remember { mutableStateOf(false) }
    var showBottomSheetTest by remember { mutableStateOf(false) }
    var bottomSheetExpandValue by remember { mutableFloatStateOf(0f) }
    var showTtsTestSheet by remember { mutableStateOf(false) }
    var triggerComposeCrash by remember { mutableStateOf(false) }
    if (triggerComposeCrash) {
        throw IllegalStateException(stringResource(R.string.tail_test_crash_composition))
    }

    if (preferences.developerModeEnabled) {
        Column {
            PreferenceCategory(title = stringResource(R.string.developer_category_database_editor))

            SettingsActionItem(
                title = stringResource(R.string.developer_edit_app_streak_title),
                summary = stringResource(R.string.developer_edit_app_streak_summary),
                onClick = { showAppPickerForStreak = true },
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_edit_global_screen_time_title),
                summary = stringResource(R.string.developer_edit_global_screen_time_summary),
                onClick = { showGlobalUsageDialog = true },
                icon = Icons.Outlined.Public,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_edit_app_screen_time_title),
                summary = stringResource(R.string.developer_edit_app_screen_time_summary),
                onClick = { showAppPickerForUsage = true },
                icon = Icons.Outlined.Smartphone,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_edit_profile_level_title),
                summary = stringResource(
                    R.string.developer_edit_profile_level_summary,
                    levelForXp(preferences.userXpTotal),
                    preferences.userXpTotal
                ),
                onClick = { showProfileLevelDialog = true },
                icon = Icons.Outlined.EmojiEvents,
                shape = if (preferences.userXpDebugBase >= 0L) RoundedCornerShape(8.dp)
                else RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            if (preferences.userXpDebugBase >= 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = stringResource(R.string.developer_reset_xp_debug_title),
                    summary = stringResource(R.string.developer_reset_xp_debug_summary, preferences.userXpDebugBase),
                    onClick = onResetXpDebug,
                    icon = Icons.Outlined.RestartAlt,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = stringResource(R.string.developer_category_database_data))

            SettingsToggle(
                title = stringResource(R.string.developer_database_source_indicator_title),
                description = stringResource(R.string.developer_database_source_indicator_summary),
                checked = preferences.showDatabaseIndicator,
                onCheckedChange = onShowDatabaseIndicatorChange,
                icon = Icons.Outlined.Storage,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsToggle(
                title = stringResource(R.string.developer_smart_repair_on_refresh_title),
                description = stringResource(R.string.developer_smart_repair_on_refresh_summary),
                checked = preferences.smartRepairOnRefresh,
                onCheckedChange = onSmartRepairOnRefreshChange,
                icon = Icons.Outlined.AutoFixHigh,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_database_records_title),
                summary = stringResource(R.string.developer_database_records_summary),
                onClick = onNavigateToDatabaseDebug,
                icon = Icons.Outlined.SdStorage,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_view_system_usage_fetch_title),
                summary = stringResource(R.string.developer_view_system_usage_fetch_summary),
                onClick = onNavigateToSystemUsageDebug,
                icon = Icons.Outlined.Analytics,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_data_repairment_title),
                summary = stringResource(R.string.developer_data_repairment_summary),
                onClick = onNavigateToDataRepairment,
                icon = Icons.Outlined.Build,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = stringResource(R.string.developer_category_logs))

            SettingsActionItem(
                title = stringResource(R.string.developer_db_log_viewer_title),
                summary = stringResource(R.string.developer_db_log_viewer_summary),
                onClick = onShowDbLogViewer,
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_overlay_log_viewer_title),
                summary = stringResource(R.string.developer_overlay_log_viewer_summary),
                onClick = onShowOverlayLogViewer,
                icon = Icons.Outlined.Visibility,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = stringResource(R.string.developer_category_onboarding_triggers))

            SettingsActionItem(
                title = stringResource(R.string.developer_trigger_permission_sheet_title),
                summary = stringResource(R.string.developer_trigger_permission_sheet_summary),
                onClick = onTriggerOnboardingPermissions,
                icon = Icons.Outlined.AdminPanelSettings,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_trigger_stats_onboarding_title),
                summary = stringResource(R.string.developer_trigger_stats_onboarding_summary),
                onClick = onTriggerOnboardingStats,
                icon = Icons.Outlined.BarChart,
                shape = RoundedCornerShape(8.dp)
            )

            if (com.etrisad.zenith.BuildConfig.SHOW_UPDATES) {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = stringResource(R.string.developer_trigger_update_onboarding_title),
                    summary = stringResource(R.string.developer_trigger_update_onboarding_summary),
                    onClick = onTriggerOnboardingUpdate,
                    icon = Icons.Outlined.Update,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_reset_guided_path_title),
                summary = stringResource(R.string.developer_reset_guided_path_summary),
                onClick = onResetOnboardingGuide,
                icon = Icons.Outlined.Explore,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_reset_bedtime_streak_title),
                summary = stringResource(R.string.developer_reset_bedtime_streak_summary),
                onClick = onResetBedtimeStreak,
                icon = Icons.Outlined.Bedtime,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_run_streak_recovery_title),
                summary = stringResource(R.string.developer_run_streak_recovery_summary),
                onClick = onResetStreakRecovery,
                icon = Icons.Outlined.RestartAlt,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = stringResource(R.string.developer_category_ui_testing))

            SettingsActionItem(
                title = stringResource(R.string.developer_test_goal_overlay_title),
                summary = stringResource(R.string.developer_test_goal_overlay_summary),
                onClick = onTestGoalOverlay,
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_goal_caller_title),
                summary = stringResource(R.string.developer_test_goal_caller_summary),
                onClick = onTestGoalCallerDelayed,
                icon = Icons.Outlined.Alarm,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_usage_glimpse_title),
                summary = stringResource(R.string.developer_test_usage_glimpse_summary),
                onClick = onTestUsageGlimpse,
                icon = Icons.Outlined.Visibility,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_achievement_banner_title),
                summary = stringResource(R.string.developer_test_achievement_banner_summary),
                onClick = onTestAchievementBanner,
                icon = Icons.Outlined.EmojiEvents,
                shape = RoundedCornerShape(8.dp)
            )

            if (com.etrisad.zenith.BuildConfig.SHOW_UPDATES) {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = stringResource(R.string.developer_test_update_sheet_title),
                    summary = stringResource(R.string.developer_test_update_sheet_summary),
                    onClick = onTestUpdateSheet,
                    icon = Icons.Outlined.NewReleases,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_variable_font_title),
                summary = stringResource(R.string.developer_test_variable_font_summary),
                onClick = onNavigateToFontTest,
                icon = Icons.Outlined.FontDownload,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_bottom_sheet_expand_title),
                summary = stringResource(R.string.developer_test_bottom_sheet_expand_summary),
                onClick = { showBottomSheetTest = true },
                icon = Icons.Outlined.OpenInFull,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_tts_title),
                summary = stringResource(R.string.developer_test_tts_summary),
                onClick = { showTtsTestSheet = true },
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            var showTestAlarmDialog by remember { mutableStateOf(false) }

            SettingsActionItem(
                title = stringResource(R.string.developer_test_alarm_overlay_title),
                summary = stringResource(R.string.developer_test_alarm_overlay_summary),
                onClick = { showTestAlarmDialog = true },
                icon = Icons.Outlined.Alarm,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            if (showTestAlarmDialog) {
                TestAlarmBottomSheet(
                    onDismiss = { showTestAlarmDialog = false }
                )
            }

            if (showTtsTestSheet) {
                TtsTestBottomSheet(
                    onDismiss = { showTtsTestSheet = false }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceCategory(title = stringResource(R.string.developer_category_crash_safety))

            val crashContext = LocalContext.current
            SettingsActionItem(
                title = stringResource(R.string.developer_test_crash_main_thread_title),
                summary = stringResource(R.string.developer_test_crash_main_thread_summary),
                onClick = { throw RuntimeException(crashContext.getString(R.string.tail_test_crash_main_thread)) },
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_crash_background_thread_title),
                summary = stringResource(R.string.developer_test_crash_background_thread_summary),
                onClick = {
                    Thread { throw RuntimeException(crashContext.getString(R.string.tail_test_crash_background_thread)) }.start()
                },
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_crash_delayed_title),
                summary = stringResource(R.string.developer_test_crash_delayed_summary),
                onClick = {
                    Thread {
                        try {
                            Thread.sleep(5000)
                        } catch (_: InterruptedException) {
                        }
                        throw RuntimeException(crashContext.getString(R.string.tail_test_crash_delayed_background))
                    }.start()
                },
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_test_crash_composition_title),
                summary = stringResource(R.string.developer_test_crash_composition_summary),
                onClick = { triggerComposeCrash = true },
                icon = Icons.Outlined.Warning,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_open_last_crash_report_title),
                summary = if (com.etrisad.zenith.crash.CrashStore.hasAnyCrash(crashContext)) {
                    stringResource(R.string.developer_open_last_crash_report_summary_found)
                } else {
                    stringResource(R.string.developer_open_last_crash_report_summary_empty)
                },
                onClick = {
                    crashContext.startActivity(
                        android.content.Intent(
                            crashContext,
                            com.etrisad.zenith.crash.CrashActivity::class.java
                        )
                    )
                },
                icon = Icons.Outlined.ReportProblem,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = stringResource(R.string.developer_restart_safe_mode_title),
                summary = stringResource(R.string.developer_restart_safe_mode_summary),
                onClick = {
                    com.etrisad.zenith.crash.SafeMode.setEnabled(crashContext, true)
                    com.etrisad.zenith.crash.SafeMode.restartApp(crashContext)
                },
                icon = Icons.Outlined.HealthAndSafety,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showBottomSheetTest) {
        var showContentB by remember { mutableStateOf(false) }
        val density = androidx.compose.ui.platform.LocalDensity.current
        val screenHeightDp = with(density) {
            androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
        }
        val contentHeight = screenHeightDp * (0.3f + bottomSheetExpandValue * 0.6f)

        Dialog(
            onDismissRequest = { showBottomSheetTest = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(Modifier.fillMaxSize()) {
                InterceptBottomSheet(
                    visible = true,
                    backgroundAlpha = 0.6f,
                    isLandscape = false,
                    showBedtimePill = false,
                    contentKey = showContentB
                ) { key ->
                    Column(
                        modifier = Modifier
                            .height(contentHeight)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (key == true) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = stringResource(R.string.developer_test_content_b_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = stringResource(R.string.developer_test_content_b_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.PauseCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )

                            Text(
                                text = stringResource(R.string.developer_test_content_a_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = stringResource(R.string.developer_test_content_a_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = stringResource(R.string.developer_test_height_label, (30 + bottomSheetExpandValue * 60).toInt()),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Slider(
                            value = bottomSheetExpandValue,
                            onValueChange = { bottomSheetExpandValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showContentB = !showContentB },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (key == true) stringResource(R.string.developer_show_content_a)
                                else stringResource(R.string.developer_show_content_b),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showBottomSheetTest = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.developer_close_button), fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showAppPickerForStreak) {
        AppPickerBottomSheet(
            uiState = focusUiState.copy(selectedFocusType = FocusType.SHIELD),
            title = stringResource(R.string.developer_select_app_streak_title),
            onDismiss = { showAppPickerForStreak = false },
            onAppSelected = {
                selectedAppInfo = it
                showAppPickerForStreak = false
                showStreakEditSheet = true
            },
            onSearchQueryChange = onSearchQueryChange
        )
    }

    if (showAppPickerForUsage) {
        AppPickerBottomSheet(
            uiState = focusUiState.copy(selectedFocusType = FocusType.SHIELD),
            title = stringResource(R.string.developer_select_app_usage_title),
            onDismiss = { showAppPickerForUsage = false },
            onAppSelected = {
                selectedAppInfo = it
                showAppPickerForUsage = false
                showAppUsageEditSheet = true
            },
            onSearchQueryChange = onSearchQueryChange
        )
    }

    if (showStreakEditSheet && selectedAppInfo != null) {
        val shield = focusUiState.activeShields.find { it.packageName == selectedAppInfo!!.packageName }
            ?: focusUiState.activeGoals.find { it.packageName == selectedAppInfo!!.packageName }

        EditValueBottomSheet(
            title = stringResource(R.string.developer_edit_streak_title, selectedAppInfo!!.appName),
            currentValueLabel = stringResource(R.string.developer_current_streak_label),
            currentValue = stringResource(R.string.developer_days_value, shield?.currentStreak ?: 0),
            inputValueLabel = stringResource(R.string.developer_new_streak_label),
            initialValue = shield?.currentStreak?.toString() ?: "0",
            onDismiss = { showStreakEditSheet = false },
            onConfirm = { newValue ->
                onUpdateAppStreak(selectedAppInfo!!.packageName, newValue.toIntOrNull() ?: 0)
                showStreakEditSheet = false
            }
        )
    }

    if (showGlobalUsageDialog) {
        val currentGlobalUsage = preferences.lastKnownDailyUsage

        EditValueBottomSheet(
            title = stringResource(R.string.developer_edit_global_screen_time_title),
            currentValueLabel = stringResource(R.string.developer_recorded_today_label),
            currentValue = formatMillis(currentGlobalUsage),
            inputValueLabel = stringResource(R.string.developer_new_usage_minutes_label),
            initialValue = (currentGlobalUsage / 60000).toString(),
            onDismiss = { showGlobalUsageDialog = false },
            onConfirm = { newValue ->
                val minutes = newValue.toLongOrNull() ?: 0L
                onUpdateGlobalScreenTime(minutes * 60 * 1000)
                showGlobalUsageDialog = false
            }
        )
    }

    if (showProfileLevelDialog) {
        val currentLevel = levelForXp(preferences.userXpTotal)
        EditValueBottomSheet(
            title = stringResource(R.string.developer_edit_profile_level_title),
            currentValueLabel = stringResource(R.string.developer_current_label),
            currentValue = stringResource(R.string.developer_current_level_value, currentLevel, preferences.userXpTotal),
            inputValueLabel = stringResource(R.string.developer_new_level_label),
            initialValue = currentLevel.toString(),
            onDismiss = { showProfileLevelDialog = false },
            onConfirm = { newValue ->
                onUpdateProfileLevel((newValue.toIntOrNull() ?: currentLevel).coerceAtLeast(1))
                showProfileLevelDialog = false
            }
        )
    }

    if (showAppUsageEditSheet && selectedAppInfo != null) {
        val context = LocalContext.current
        val usageStatsManager = context.getSystemService(android.content.Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val todayUsage = remember(selectedAppInfo) {
            ScreenUsageHelper.fetchDetailedUsageToday(usageStatsManager, dayStartHour = com.etrisad.zenith.service.SharedMonitoringState.cachedDayStartHour, dayStartMinute = com.etrisad.zenith.service.SharedMonitoringState.cachedDayStartMinute).appUsageMap[selectedAppInfo!!.packageName] ?: 0L
        }

        EditValueBottomSheet(
            title = stringResource(R.string.developer_edit_usage_title, selectedAppInfo!!.appName),
            currentValueLabel = stringResource(R.string.developer_system_recorded_label),
            currentValue = formatMillis(todayUsage),
            inputValueLabel = stringResource(R.string.developer_new_usage_minutes_label),
            initialValue = (todayUsage / 60000).toString(),
            onDismiss = { showAppUsageEditSheet = false },
            onConfirm = { newValue ->
                val minutes = newValue.toLongOrNull() ?: 0L
                onUpdateAppScreenTime(selectedAppInfo!!.packageName, minutes * 60 * 1000)
                showAppUsageEditSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditValueBottomSheet(
    title: String,
    currentValueLabel: String,
    currentValue: String,
    inputValueLabel: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var inputValue by remember { mutableStateOf(initialValue) }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentValueLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentValue,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it.filter { char -> char.isDigit() } },
                label = { Text(inputValueLabel) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onConfirm(inputValue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(stringResource(R.string.developer_update_data_button), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TestAlarmBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var mathChallengeEnabled by remember { mutableStateOf(false) }
    var gradualVolumeEnabled by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "containerColor"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.developer_test_alarm_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.developer_test_alarm_sheet_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                onClick = { mathChallengeEnabled = !mathChallengeEnabled },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
                color = containerColor
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.developer_math_challenge_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.developer_math_challenge_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = mathChallengeEnabled,
                        onCheckedChange = { mathChallengeEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Surface(
                onClick = { gradualVolumeEnabled = !gradualVolumeEnabled },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                color = containerColor
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.developer_gradual_volume_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.developer_gradual_volume_summary),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = gradualVolumeEnabled,
                        onCheckedChange = { gradualVolumeEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ZenithButton(
                onClick = {
                    val intent = android.content.Intent(
                        context,
                        com.etrisad.zenith.service.AlarmOverlayActivity::class.java
                    ).apply {
                        putExtra(
                            com.etrisad.zenith.service.AlarmOverlayActivity.EXTRA_TEST_MATH_CHALLENGE,
                            mathChallengeEnabled
                        )
                        putExtra(
                            com.etrisad.zenith.service.AlarmOverlayActivity.EXTRA_TEST_GRADUAL_VOLUME,
                            gradualVolumeEnabled
                        )
                    }
                    context.startActivity(intent)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.developer_test_alarm_button),
                type = ZenithButtonType.Filled,
                size = ZenithButtonSize.ExtraLarge,
                fillMaxWidth = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            ZenithButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.developer_cancel),
                type = ZenithButtonType.Outlined,
                size = ZenithButtonSize.ExtraLarge,
                fillMaxWidth = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TtsTestBottomSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val defaultTtsText = stringResource(R.string.developer_tts_default_text)
    var textInput by remember { mutableStateOf(defaultTtsText) }
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    var ttsStatus by remember {
        mutableStateOf(context.getString(R.string.developer_tts_status_initializing))
    }
    var ttsReady by remember { mutableStateOf(false) }
    var availableLocales by remember { mutableStateOf<List<java.util.Locale>>(emptyList()) }
    var selectedLocale by remember { mutableStateOf(java.util.Locale.getDefault()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        try {
            var engine: android.speech.tts.TextToSpeech? = null
            engine = android.speech.tts.TextToSpeech(context) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    engine?.let { e ->
                        availableLocales = e.availableLanguages?.toList() ?: emptyList()
                        selectedLocale = java.util.Locale.getDefault()
                        val result = e.setLanguage(selectedLocale)
                        ttsReady = result != android.speech.tts.TextToSpeech.LANG_MISSING_DATA &&
                            result != android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED
                        ttsStatus = when (result) {
                            android.speech.tts.TextToSpeech.LANG_COUNTRY_AVAILABLE -> context.getString(
                                R.string.developer_tts_status_ready_language,
                                selectedLocale.displayName
                            )
                            android.speech.tts.TextToSpeech.LANG_AVAILABLE -> context.getString(
                                R.string.developer_tts_status_ready_language,
                                selectedLocale.displayName
                            )
                            android.speech.tts.TextToSpeech.LANG_MISSING_DATA ->
                                context.getString(R.string.developer_tts_status_missing_data)
                            android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED ->
                                context.getString(R.string.developer_tts_status_not_supported)
                            else -> context.getString(R.string.developer_tts_status_ready)
                        }
                    }
                } else {
                    ttsStatus = context.getString(R.string.developer_tts_status_init_failed, status)
                    ttsReady = false
                }
            }
            tts = engine
        } catch (e: Exception) {
            ttsStatus = context.getString(R.string.developer_tts_status_error, e.message ?: "")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            tts?.stop()
            tts?.shutdown()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.developer_tts_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.developer_tts_sheet_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                imageVector = if (ttsReady) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.developer_tts_engine_label),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ttsStatus,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!ttsReady) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(R.string.developer_tts_engine_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            onClick = {
                                context.startActivity(
                                    android.content.Intent(android.speech.tts.TextToSpeech.Engine.ACTION_CHECK_TTS_DATA)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.developer_open_tts_settings),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text(stringResource(R.string.developer_tts_text_to_speak_label)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                maxLines = 6
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            tts?.setLanguage(selectedLocale)
                            tts?.speak(textInput, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, "tts_utterance")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    enabled = ttsReady && textInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.developer_tts_speak_button), fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        tts?.stop()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    enabled = ttsReady
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.developer_tts_stop_button), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (availableLocales.isNotEmpty()) {
                val localeOptions = remember(availableLocales) {
                    availableLocales
                        .distinctBy { it.language }
                        .sortedBy { it.displayName }
                        .map { it.displayName to it.toLanguageTag() }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
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
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.developer_voice_language_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.developer_voice_language_summary),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        ZenithDropdown(
                            options = localeOptions,
                            selectedOption = selectedLocale.toLanguageTag(),
                            onOptionSelected = { tag ->
                                val locale = java.util.Locale.forLanguageTag(tag)
                                selectedLocale = locale
                                tts?.setLanguage(locale)
                                ttsStatus = context.getString(
                                    R.string.developer_tts_status_ready_language,
                                    locale.displayName
                                )
                                ttsReady = true
                            },
                            width = 200.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ZenithButton(
                onClick = {
                    tts?.stop()
                    tts?.shutdown()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.developer_close_button),
                type = ZenithButtonType.Outlined,
                size = ZenithButtonSize.ExtraLarge,
                fillMaxWidth = true
            )
        }
    }
}

@Composable
private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) stringResource(
        R.string.developer_duration_hours_minutes, hours, minutes
    ) else stringResource(
        R.string.developer_duration_minutes, minutes
    )
}

package com.etrisad.zenith.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.etrisad.zenith.R
import com.etrisad.zenith.ui.navigation.Screen

object FeatureInfoRegistry {

    // Copy comes from string resources, so the lookup runs during composition;
    // every caller (the info sheet and the header button) is a composable.
    @Composable
    fun infoFor(route: String?, category: String? = null): FeatureInfo? = when {
        route == Screen.Home.route -> home()
        route == Screen.Focus.route -> focus()
        route == Screen.Settings.route -> settings()
        route == Screen.UsageStats.route -> usageStats()
        route == Screen.Bedtime.route -> bedtime()
        route == Screen.Alarm.route -> alarm()
        route == Screen.GracePeriod.route -> gracePeriod()
        route == Screen.EyeCare.route -> eyeCare()
        route == Screen.ReelsShield.route -> reelsShield()
        route == Screen.RedirectShield.route -> redirectShield()
        route?.startsWith("app_detail") == true -> appDetail()
        route == Screen.Lockdown.route -> lockdown()
        route == Screen.TamperProtection.route -> tamperProtection()
        route == Screen.Pomodoro.route -> pomodoro()
        route == Screen.PausePoint.route -> pausePoint()
        route == Screen.Profile.route -> profile()
        route == Screen.Achievements.route -> achievements()
        route == Screen.Level.route -> level()
        route?.startsWith("pause_point_type") == true -> pausePointType()
        route == Screen.OverlayAppearance.route -> overlayAppearance()
        route == Screen.GSFlexCustomizer.route -> gsFlexCustomizer()
        route == Screen.DatabaseDebug.route -> databaseDebug()
        route == Screen.DataRepairment.route -> dataRepairment()
        route == Screen.SystemUsageDebug.route -> systemUsageDebug()
        route == Screen.FontTest.route -> fontTest()
        route?.startsWith("settings_category") == true -> when (category?.lowercase()) {
            "features" -> featuresCategory()
            "appearance" -> appearanceCategory()
            "performance" -> performanceCategory()
            "privacy" -> privacyCategory()
            "data management" -> dataManagementCategory()
            "developer" -> developerCategory()
            else -> null
        }
        else -> null
    }

    @Composable
    private fun entry(
        titleRes: Int,
        summaryRes: Int,
        vararg sections: FeatureInfoSection
    ) = FeatureInfo(
        title = stringResource(titleRes),
        summary = stringResource(summaryRes),
        sections = sections.toList()
    )

    @Composable
    private fun does(vararg resIds: Int) =
        FeatureInfoSections.whatItDoes(*resIds.map { stringResource(it) }.toTypedArray())

    @Composable
    private fun sees(vararg resIds: Int) =
        FeatureInfoSections.whatYoullSee(*resIds.map { stringResource(it) }.toTypedArray())

    @Composable
    private fun tips(vararg resIds: Int) =
        FeatureInfoSections.tips(*resIds.map { stringResource(it) }.toTypedArray())

    // Copy with literal percent signs is stored as "%%" so aapt2 accepts it;
    // collapse the escapes again so a single "%" reaches the screen.
    @Composable
    private fun percentCopy(resId: Int) = stringResource(resId).replace("%%", "%")

    @Composable
    private fun home() = entry(
        R.string.feature_info_home_title,
        R.string.feature_info_home_summary,
        does(R.string.feature_info_home_what_it_does_1, R.string.feature_info_home_what_it_does_2, R.string.feature_info_home_what_it_does_3),
        sees(R.string.feature_info_home_what_youll_see_1, R.string.feature_info_home_what_youll_see_2, R.string.feature_info_home_what_youll_see_3),
        tips(R.string.feature_info_home_tips_1, R.string.feature_info_home_tips_2, R.string.feature_info_home_tips_3)
    )

    @Composable
    private fun focus() = entry(
        R.string.feature_info_focus_title,
        R.string.feature_info_focus_summary,
        does(R.string.feature_info_focus_what_it_does_1, R.string.feature_info_focus_what_it_does_2, R.string.feature_info_focus_what_it_does_3),
        sees(R.string.feature_info_focus_what_youll_see_1, R.string.feature_info_focus_what_youll_see_2, R.string.feature_info_focus_what_youll_see_3),
        tips(R.string.feature_info_focus_tips_1, R.string.feature_info_focus_tips_2, R.string.feature_info_focus_tips_3)
    )

    @Composable
    private fun settings() = entry(
        R.string.feature_info_settings_title,
        R.string.feature_info_settings_summary,
        does(R.string.feature_info_settings_what_it_does_1, R.string.feature_info_settings_what_it_does_2, R.string.feature_info_settings_what_it_does_3),
        sees(R.string.feature_info_settings_what_youll_see_1, R.string.feature_info_settings_what_youll_see_2, R.string.feature_info_settings_what_youll_see_3),
        tips(R.string.feature_info_settings_tips_1, R.string.feature_info_settings_tips_2, R.string.feature_info_settings_tips_3)
    )

    @Composable
    private fun redirectShield() = entry(
        R.string.feature_info_redirect_shield_title,
        R.string.feature_info_redirect_shield_summary,
        does(R.string.feature_info_redirect_shield_what_it_does_1, R.string.feature_info_redirect_shield_what_it_does_2),
        sees(R.string.feature_info_redirect_shield_what_youll_see_1, R.string.feature_info_redirect_shield_what_youll_see_2),
        tips(R.string.feature_info_redirect_shield_tips_1, R.string.feature_info_redirect_shield_tips_2, R.string.feature_info_redirect_shield_tips_3)
    )

    @Composable
    private fun usageStats() = entry(
        R.string.feature_info_usage_stats_title,
        R.string.feature_info_usage_stats_summary,
        does(R.string.feature_info_usage_stats_what_it_does_1, R.string.feature_info_usage_stats_what_it_does_2),
        sees(R.string.feature_info_usage_stats_what_youll_see_1, R.string.feature_info_usage_stats_what_youll_see_2, R.string.feature_info_usage_stats_what_youll_see_3),
        tips(R.string.feature_info_usage_stats_tips_1, R.string.feature_info_usage_stats_tips_2, R.string.feature_info_usage_stats_tips_3)
    )

    @Composable
    private fun bedtime() = entry(
        R.string.feature_info_bedtime_title,
        R.string.feature_info_bedtime_summary,
        does(R.string.feature_info_bedtime_what_it_does_1, R.string.feature_info_bedtime_what_it_does_2),
        sees(R.string.feature_info_bedtime_what_youll_see_1, R.string.feature_info_bedtime_what_youll_see_2, R.string.feature_info_bedtime_what_youll_see_3),
        tips(R.string.feature_info_bedtime_tips_1, R.string.feature_info_bedtime_tips_2, R.string.feature_info_bedtime_tips_3)
    )

    @Composable
    private fun alarm() = entry(
        R.string.feature_info_alarm_title,
        R.string.feature_info_alarm_summary,
        does(R.string.feature_info_alarm_what_it_does_1, R.string.feature_info_alarm_what_it_does_2),
        sees(R.string.feature_info_alarm_what_youll_see_1, R.string.feature_info_alarm_what_youll_see_2, R.string.feature_info_alarm_what_youll_see_3),
        tips(R.string.feature_info_alarm_tips_1, R.string.feature_info_alarm_tips_2, R.string.feature_info_alarm_tips_3)
    )

    @Composable
    private fun gracePeriod() = entry(
        R.string.feature_info_grace_period_title,
        R.string.feature_info_grace_period_summary,
        does(R.string.feature_info_grace_period_what_it_does_1, R.string.feature_info_grace_period_what_it_does_2, R.string.feature_info_grace_period_what_it_does_3),
        sees(R.string.feature_info_grace_period_what_youll_see_1, R.string.feature_info_grace_period_what_youll_see_2, R.string.feature_info_grace_period_what_youll_see_3),
        tips(R.string.feature_info_grace_period_tips_1, R.string.feature_info_grace_period_tips_2, R.string.feature_info_grace_period_tips_3, R.string.feature_info_grace_period_tips_4, R.string.feature_info_grace_period_tips_5)
    )

    @Composable
    private fun eyeCare() = entry(
        R.string.feature_info_eye_care_title,
        R.string.feature_info_eye_care_summary,
        does(R.string.feature_info_eye_care_what_it_does_1, R.string.feature_info_eye_care_what_it_does_2),
        sees(R.string.feature_info_eye_care_what_youll_see_1, R.string.feature_info_eye_care_what_youll_see_2),
        tips(R.string.feature_info_eye_care_tips_1, R.string.feature_info_eye_care_tips_2, R.string.feature_info_eye_care_tips_3)
    )

    @Composable
    private fun reelsShield() = entry(
        R.string.feature_info_reels_shield_title,
        R.string.feature_info_reels_shield_summary,
        does(R.string.feature_info_reels_shield_what_it_does_1, R.string.feature_info_reels_shield_what_it_does_2, R.string.feature_info_reels_shield_what_it_does_3, R.string.feature_info_reels_shield_what_it_does_4, R.string.feature_info_reels_shield_what_it_does_5, R.string.feature_info_reels_shield_what_it_does_6),
        sees(R.string.feature_info_reels_shield_what_youll_see_1, R.string.feature_info_reels_shield_what_youll_see_2, R.string.feature_info_reels_shield_what_youll_see_3),
        tips(R.string.feature_info_reels_shield_tips_1, R.string.feature_info_reels_shield_tips_2, R.string.feature_info_reels_shield_tips_3)
    )

    @Composable
    private fun appDetail() = entry(
        R.string.feature_info_app_detail_title,
        R.string.feature_info_app_detail_summary,
        does(R.string.feature_info_app_detail_what_it_does_1, R.string.feature_info_app_detail_what_it_does_2),
        sees(R.string.feature_info_app_detail_what_youll_see_1, R.string.feature_info_app_detail_what_youll_see_2, R.string.feature_info_app_detail_what_youll_see_3),
        tips(R.string.feature_info_app_detail_tips_1, R.string.feature_info_app_detail_tips_2, R.string.feature_info_app_detail_tips_3)
    )

    @Composable
    private fun lockdown() = entry(
        R.string.feature_info_lockdown_title,
        R.string.feature_info_lockdown_summary,
        does(R.string.feature_info_lockdown_what_it_does_1, R.string.feature_info_lockdown_what_it_does_2),
        sees(R.string.feature_info_lockdown_what_youll_see_1, R.string.feature_info_lockdown_what_youll_see_2),
        tips(R.string.feature_info_lockdown_tips_1, R.string.feature_info_lockdown_tips_2, R.string.feature_info_lockdown_tips_3)
    )

    @Composable
    private fun tamperProtection() = entry(
        R.string.feature_info_tamper_protection_title,
        R.string.feature_info_tamper_protection_summary,
        does(R.string.feature_info_tamper_protection_what_it_does_1, R.string.feature_info_tamper_protection_what_it_does_2, R.string.feature_info_tamper_protection_what_it_does_3),
        sees(R.string.feature_info_tamper_protection_what_youll_see_1, R.string.feature_info_tamper_protection_what_youll_see_2),
        tips(R.string.feature_info_tamper_protection_tips_1, R.string.feature_info_tamper_protection_tips_2, R.string.feature_info_tamper_protection_tips_3)
    )

    @Composable
    private fun pomodoro() = entry(
        R.string.feature_info_pomodoro_title,
        R.string.feature_info_pomodoro_summary,
        does(R.string.feature_info_pomodoro_what_it_does_1, R.string.feature_info_pomodoro_what_it_does_2),
        sees(R.string.feature_info_pomodoro_what_youll_see_1, R.string.feature_info_pomodoro_what_youll_see_2, R.string.feature_info_pomodoro_what_youll_see_3),
        tips(R.string.feature_info_pomodoro_tips_1, R.string.feature_info_pomodoro_tips_2, R.string.feature_info_pomodoro_tips_3)
    )

    @Composable
    private fun pausePoint() = entry(
        R.string.feature_info_pause_point_title,
        R.string.feature_info_pause_point_summary,
        does(R.string.feature_info_pause_point_what_it_does_1, R.string.feature_info_pause_point_what_it_does_2),
        sees(R.string.feature_info_pause_point_what_youll_see_1, R.string.feature_info_pause_point_what_youll_see_2, R.string.feature_info_pause_point_what_youll_see_3),
        tips(R.string.feature_info_pause_point_tips_1, R.string.feature_info_pause_point_tips_2, R.string.feature_info_pause_point_tips_3)
    )

    @Composable
    private fun profile() = entry(
        R.string.feature_info_profile_title,
        R.string.feature_info_profile_summary,
        does(R.string.feature_info_profile_what_it_does_1, R.string.feature_info_profile_what_it_does_2),
        sees(R.string.feature_info_profile_what_youll_see_1, R.string.feature_info_profile_what_youll_see_2, R.string.feature_info_profile_what_youll_see_3),
        tips(R.string.feature_info_profile_tips_1, R.string.feature_info_profile_tips_2)
    )

    @Composable
    private fun achievements() = entry(
        R.string.feature_info_achievements_title,
        R.string.feature_info_achievements_summary,
        does(R.string.feature_info_achievements_what_it_does_1, R.string.feature_info_achievements_what_it_does_2),
        sees(R.string.feature_info_achievements_what_youll_see_1, R.string.feature_info_achievements_what_youll_see_2),
        tips(R.string.feature_info_achievements_tips_1)
    )

    @Composable
    private fun level() = entry(
        R.string.feature_info_level_title,
        R.string.feature_info_level_summary,
        does(R.string.feature_info_level_what_it_does_1, R.string.feature_info_level_what_it_does_2),
        sees(R.string.feature_info_level_what_youll_see_1, R.string.feature_info_level_what_youll_see_2, R.string.feature_info_level_what_youll_see_3, R.string.feature_info_level_what_youll_see_4),
        tips(R.string.feature_info_level_tips_1, R.string.feature_info_level_tips_2)
    )

    @Composable
    private fun pausePointType() = entry(
        R.string.feature_info_pause_point_type_title,
        R.string.feature_info_pause_point_type_summary,
        does(R.string.feature_info_pause_point_type_what_it_does_1, R.string.feature_info_pause_point_type_what_it_does_2),
        sees(R.string.feature_info_pause_point_type_what_youll_see_1, R.string.feature_info_pause_point_type_what_youll_see_2, R.string.feature_info_pause_point_type_what_youll_see_3),
        tips(R.string.feature_info_pause_point_type_tips_1, R.string.feature_info_pause_point_type_tips_2, R.string.feature_info_pause_point_type_tips_3, R.string.feature_info_pause_point_type_tips_4)
    )

    @Composable
    private fun overlayAppearance() = entry(
        R.string.feature_info_overlay_appearance_title,
        R.string.feature_info_overlay_appearance_summary,
        does(R.string.feature_info_overlay_appearance_what_it_does_1),
        sees(R.string.feature_info_overlay_appearance_what_youll_see_1, R.string.feature_info_overlay_appearance_what_youll_see_2),
        tips(R.string.feature_info_overlay_appearance_tips_1, R.string.feature_info_overlay_appearance_tips_2, R.string.feature_info_overlay_appearance_tips_3)
    )

    @Composable
    private fun gsFlexCustomizer() = entry(
        R.string.feature_info_gs_flex_customizer_title,
        R.string.feature_info_gs_flex_customizer_summary,
        does(R.string.feature_info_gs_flex_customizer_what_it_does_1),
        sees(R.string.feature_info_gs_flex_customizer_what_youll_see_1, R.string.feature_info_gs_flex_customizer_what_youll_see_2),
        tips(R.string.feature_info_gs_flex_customizer_tips_1, R.string.feature_info_gs_flex_customizer_tips_2)
    )

    @Composable
    private fun featuresCategory() = entry(
        R.string.feature_info_features_category_title,
        R.string.feature_info_features_category_summary,
        does(R.string.feature_info_features_category_what_it_does_1, R.string.feature_info_features_category_what_it_does_2, R.string.feature_info_features_category_what_it_does_3),
        sees(R.string.feature_info_features_category_what_youll_see_1),
        FeatureInfoSections.tips(
            percentCopy(R.string.feature_info_features_category_tips_1),
            stringResource(R.string.feature_info_features_category_tips_2)
        )
    )

    @Composable
    private fun appearanceCategory() = entry(
        R.string.feature_info_appearance_category_title,
        R.string.feature_info_appearance_category_summary,
        does(R.string.feature_info_appearance_category_what_it_does_1, R.string.feature_info_appearance_category_what_it_does_2),
        sees(R.string.feature_info_appearance_category_what_youll_see_1),
        tips(R.string.feature_info_appearance_category_tips_1, R.string.feature_info_appearance_category_tips_2)
    )

    @Composable
    private fun performanceCategory() = entry(
        R.string.feature_info_performance_category_title,
        R.string.feature_info_performance_category_summary,
        does(R.string.feature_info_performance_category_what_it_does_1, R.string.feature_info_performance_category_what_it_does_2, R.string.feature_info_performance_category_what_it_does_3),
        sees(R.string.feature_info_performance_category_what_youll_see_1),
        tips(R.string.feature_info_performance_category_tips_1, R.string.feature_info_performance_category_tips_2, R.string.feature_info_performance_category_tips_3)
    )

    @Composable
    private fun privacyCategory() = entry(
        R.string.feature_info_privacy_category_title,
        R.string.feature_info_privacy_category_summary,
        does(R.string.feature_info_privacy_category_what_it_does_1, R.string.feature_info_privacy_category_what_it_does_2, R.string.feature_info_privacy_category_what_it_does_3),
        sees(R.string.feature_info_privacy_category_what_youll_see_1, R.string.feature_info_privacy_category_what_youll_see_2, R.string.feature_info_privacy_category_what_youll_see_3),
        tips(R.string.feature_info_privacy_category_tips_1, R.string.feature_info_privacy_category_tips_2, R.string.feature_info_privacy_category_tips_3, R.string.feature_info_privacy_category_tips_4)
    )

    @Composable
    private fun dataManagementCategory() = entry(
        R.string.feature_info_data_management_category_title,
        R.string.feature_info_data_management_category_summary,
        does(R.string.feature_info_data_management_category_what_it_does_1, R.string.feature_info_data_management_category_what_it_does_2),
        sees(R.string.feature_info_data_management_category_what_youll_see_1, R.string.feature_info_data_management_category_what_youll_see_2),
        tips(R.string.feature_info_data_management_category_tips_1, R.string.feature_info_data_management_category_tips_2)
    )

    @Composable
    private fun developerCategory() = entry(
        R.string.feature_info_developer_category_title,
        R.string.feature_info_developer_category_summary,
        does(R.string.feature_info_developer_category_what_it_does_1, R.string.feature_info_developer_category_what_it_does_2),
        sees(R.string.feature_info_developer_category_what_youll_see_1, R.string.feature_info_developer_category_what_youll_see_2),
        tips(R.string.feature_info_developer_category_tips_1, R.string.feature_info_developer_category_tips_2)
    )

    @Composable
    private fun databaseDebug() = entry(
        R.string.feature_info_database_debug_title,
        R.string.feature_info_database_debug_summary,
        does(R.string.feature_info_database_debug_what_it_does_1),
        sees(R.string.feature_info_database_debug_what_youll_see_1),
        tips(R.string.feature_info_database_debug_tips_1, R.string.feature_info_database_debug_tips_2)
    )

    @Composable
    private fun dataRepairment() = entry(
        R.string.feature_info_data_repairment_title,
        R.string.feature_info_data_repairment_summary,
        does(R.string.feature_info_data_repairment_what_it_does_1, R.string.feature_info_data_repairment_what_it_does_2),
        sees(R.string.feature_info_data_repairment_what_youll_see_1, R.string.feature_info_data_repairment_what_youll_see_2),
        tips(R.string.feature_info_data_repairment_tips_1, R.string.feature_info_data_repairment_tips_2)
    )

    @Composable
    private fun systemUsageDebug() = entry(
        R.string.feature_info_system_usage_debug_title,
        R.string.feature_info_system_usage_debug_summary,
        does(R.string.feature_info_system_usage_debug_what_it_does_1),
        sees(R.string.feature_info_system_usage_debug_what_youll_see_1),
        tips(R.string.feature_info_system_usage_debug_tips_1, R.string.feature_info_system_usage_debug_tips_2)
    )

    @Composable
    private fun fontTest() = entry(
        R.string.feature_info_font_test_title,
        R.string.feature_info_font_test_summary,
        does(R.string.feature_info_font_test_what_it_does_1),
        sees(R.string.feature_info_font_test_what_youll_see_1),
        tips(R.string.feature_info_font_test_tips_1, R.string.feature_info_font_test_tips_2)
    )
}

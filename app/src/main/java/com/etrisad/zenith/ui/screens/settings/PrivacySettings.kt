package com.etrisad.zenith.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.ui.components.focus.CardGroup
import com.etrisad.zenith.ui.components.focus.PreferenceCategory

@Composable
fun PrivacySettings(
    preferences: UserPreferences,
    onAppExclusionsChange: (Set<String>, Set<String>) -> Unit,
    onAccessibilityLimitedModeChange: (Boolean) -> Unit,
    onNotificationInterceptionEnabledChange: (Boolean) -> Unit,
    onNotificationExcludedPackagesChange: (Set<String>) -> Unit,
    onStoreInterceptedNotificationContentChange: (Boolean) -> Unit
) {
    var showAppExclusionsSheet by remember { mutableStateOf(false) }
    var showNotifExcludedSheet by remember { mutableStateOf(false) }
    var showPrivacyInfoSheet by remember { mutableStateOf(false) }
    val exclusionSummary = appExclusionSummary(
        preferences.privatePackages,
        preferences.excludedFromTrackingPackages
    )

    Column {
        PreferenceCategory(title = stringResource(R.string.grace_privacy_and_data))

        SettingsActionItem(
            title = stringResource(R.string.grace_private_hidden_apps),
            summary = exclusionSummary,
            onClick = { showAppExclusionsSheet = true },
            icon = Icons.Outlined.Security,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsActionItem(
            title = stringResource(R.string.grace_privacy_guarantees_title),
            summary = stringResource(R.string.grace_privacy_guarantees_summary),
            onClick = { showPrivacyInfoSheet = true },
            icon = Icons.Outlined.Info,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.grace_accessibility_mode))

        SettingsToggle(
            title = stringResource(R.string.grace_limited_accessibility_title),
            description = stringResource(R.string.grace_limited_accessibility_desc),
            checked = preferences.accessibilityLimitedMode,
            onCheckedChange = onAccessibilityLimitedModeChange,
            icon = Icons.Outlined.AccessibilityNew,
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.grace_notification_privacy))

        SettingsToggle(
            title = stringResource(R.string.grace_notification_interception_title),
            description = stringResource(R.string.grace_notification_interception_desc),
            checked = preferences.notificationInterceptionEnabled,
            onCheckedChange = onNotificationInterceptionEnabledChange,
            icon = Icons.Outlined.NotificationsActive,
            shape = if (preferences.notificationInterceptionEnabled)
                RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            else RoundedCornerShape(24.dp)
        )

        if (preferences.notificationInterceptionEnabled) {
            Spacer(modifier = Modifier.height(4.dp))

            SettingsActionItem(
                title = stringResource(R.string.grace_excluded_notification_apps),
                summary = if (preferences.notificationExcludedPackages.isEmpty()) stringResource(R.string.grace_no_apps_excluded)
                          else stringResource(
                              R.string.grace_apps_never_intercepted,
                              preferences.notificationExcludedPackages.size
                          ),
                onClick = { showNotifExcludedSheet = true },
                icon = Icons.Outlined.NotificationsOff,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            SettingsToggle(
                title = stringResource(R.string.grace_store_notification_contents_title),
                description = stringResource(R.string.grace_store_notification_contents_desc),
                checked = preferences.storeInterceptedNotificationContent,
                onCheckedChange = onStoreInterceptedNotificationContentChange,
                icon = Icons.AutoMirrored.Outlined.Notes,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = stringResource(R.string.grace_local_storage_cloud_backup))

        CardGroup(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        ) {
            BackupInfoRow(
                title = stringResource(R.string.grace_android_cloud_backup_title),
                description = stringResource(R.string.grace_android_cloud_backup_desc)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        CardGroup(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            BackupInfoRow(
                title = stringResource(R.string.grace_local_backups_title),
                description = stringResource(R.string.grace_local_backups_desc)
            )
        }
    }

    if (showAppExclusionsSheet) {
        AppExclusionsBottomSheet(
            initialPrivateApps = preferences.privatePackages,
            initialUsageOnlyApps = preferences.excludedFromTrackingPackages,
            onDismiss = { showAppExclusionsSheet = false },
            onSave = { privateApps, usageOnlyApps ->
                onAppExclusionsChange(privateApps, usageOnlyApps)
                showAppExclusionsSheet = false
            }
        )
    }

    if (showNotifExcludedSheet) {
        NotificationExcludedAppsBottomSheet(
            initialExcluded = preferences.notificationExcludedPackages,
            onDismiss = { showNotifExcludedSheet = false },
            onSave = { pkgs ->
                onNotificationExcludedPackagesChange(pkgs)
                showNotifExcludedSheet = false
            }
        )
    }

    if (showPrivacyInfoSheet) {
        PrivacyInfoBottomSheet(
            onDismiss = { showPrivacyInfoSheet = false }
        )
    }
}

@Composable
private fun BackupInfoRow(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

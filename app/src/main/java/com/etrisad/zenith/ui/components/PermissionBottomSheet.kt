package com.etrisad.zenith.ui.components

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.ui.screens.settings.PrivacyInfoBottomSheet
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.etrisad.zenith.R
import com.etrisad.zenith.data.preferences.UserPreferences
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.ui.components.nfc.isNfcEnabled
import com.etrisad.zenith.ui.components.nfc.isNfcSupported
import com.etrisad.zenith.ui.components.nfc.openNfcSettings
import com.etrisad.zenith.service.AppUsageMonitorService
import com.etrisad.zenith.service.SharedMonitoringState
import com.etrisad.zenith.util.canScheduleExactAlarms
import com.etrisad.zenith.util.hasCalendarPermission
import com.etrisad.zenith.util.hasNotificationPermission
import com.etrisad.zenith.util.hasUsageStatsPermission
import com.etrisad.zenith.util.isAccessibilityServiceEnabled
import com.etrisad.zenith.util.isAndroidGo
import com.etrisad.zenith.util.isIgnoringBatteryOptimizations
import com.etrisad.zenith.util.isNotificationListenerEnabled
import com.etrisad.zenith.util.openNotificationPolicySettings
import com.etrisad.zenith.util.openSystemSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class GroupPosition {
    Top, Middle, Bottom, Single
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionBottomSheet(
    preferencesRepository: UserPreferencesRepository,
    onDismissRequest: () -> Unit,
    onAllPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    val preferences by preferencesRepository.userPreferencesFlow.collectAsState(
        initial = UserPreferences()
    )
    
    var hasUsageStats by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var hasAccessibility by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var hasNotifications by remember { mutableStateOf(hasNotificationPermission(context)) }
    var hasNotificationPolicy by remember { mutableStateOf((context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).isNotificationPolicyAccessGranted) }
    var hasNotificationListener by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var hasCalendar by remember { mutableStateOf(hasCalendarPermission(context)) }
    var hasNfcSupport by remember { mutableStateOf(isNfcSupported(context)) }
    var hasNfcEnabled by remember { mutableStateOf(isNfcEnabled(context)) }
    var hasCamera by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraAsked by remember { mutableStateOf(false) }
    var isBatteryOptimized by remember { mutableStateOf(!isIgnoringBatteryOptimizations(context)) }
    var canExactAlarm by remember { mutableStateOf(canScheduleExactAlarms(context)) }
    
    var stabilityExpanded by remember { mutableStateOf(false) }
    var optionalExpanded by remember { mutableStateOf(false) }
    var showBankingWarning by remember { mutableStateOf(false) }
    var showInterceptWarning by remember { mutableStateOf(false) }
    var showPrivacyInfoSheet by remember { mutableStateOf(false) }

    val installedBankingApps = remember {
        val allInstalled = context.packageManager.getInstalledApplications(0)
            .map { it.packageName }
            .toSet()
        SharedMonitoringState.FINANCIAL_APPS.filter { it in allInstalled }
    }

    val optionalAllGranted = if (preferences.accessibilityRequired) hasNotificationListener else (hasAccessibility && hasNotificationListener)
    val stabilityAllGranted = !isBatteryOptimized && canExactAlarm

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotifications = isGranted
            if (isGranted) {
                context.startService(Intent(context, AppUsageMonitorService::class.java).apply {
                    action = "SEND_TEST_NOTIFICATION"
                })
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCamera = isGranted
            if (!isGranted) cameraAsked = true
            if (isGranted) {
                AppUsageMonitorService.requestCameraTypeUpgrade(context)
            }
        }
    )
    var calendarAsked by remember { mutableStateOf(false) }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCalendar = isGranted
            if (!isGranted) calendarAsked = true
        }
    )
    fun openCalendarPermission() {
        val permanentlyDenied = !hasCalendar && calendarAsked &&
            (context as? Activity)?.let {
                !ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.READ_CALENDAR)
            } == true
        if (permanentlyDenied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            try {
                calendarAsked = true
                calendarPermissionLauncher.launch(android.Manifest.permission.READ_CALENDAR)
            } catch (_: Exception) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
    val cameraPermanentlyDenied = !hasCamera && cameraAsked &&
        (context as? Activity)?.let {
            !ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.CAMERA)
        } == true

    fun openCameraPermission() {
        if (cameraPermanentlyDenied) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            cameraAsked = true
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    fun openAccessibilitySettings(ctx: android.content.Context) {
        val wasAlreadyEnabled = isAccessibilityServiceEnabled(ctx)
        if (!openSystemSettings(ctx, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) {
            Toast.makeText(
                ctx,
                ctx.getString(R.string.permission_settings_unavailable),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (!wasAlreadyEnabled) {
            scope.launch {
                val deadline = System.currentTimeMillis() + 120_000L
                while (System.currentTimeMillis() < deadline) {
                    delay(500)
                    if (isAccessibilityServiceEnabled(ctx)) {
                        delay(300)
                        val launchIntent = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(launchIntent)
                        }
                        break
                    }
                }
            }
        }
    }

    val allGranted = hasUsageStats && hasOverlay && hasNotifications && hasNotificationPolicy && (hasAccessibility || !preferences.accessibilityRequired)
    val missingRequiredCount = listOf(
        hasUsageStats,
        hasOverlay,
        hasNotifications,
        hasNotificationPolicy,
        hasAccessibility || !preferences.accessibilityRequired
    ).count { !it }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageStats = hasUsageStatsPermission(context)
                hasOverlay = Settings.canDrawOverlays(context)
                hasAccessibility = isAccessibilityServiceEnabled(context)
                hasNotifications = hasNotificationPermission(context)
                hasNotificationPolicy = (context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager).isNotificationPolicyAccessGranted
                hasNotificationListener = isNotificationListenerEnabled(context)
                hasCalendar = hasCalendarPermission(context)
                hasNfcSupport = isNfcSupported(context)
                hasNfcEnabled = isNfcEnabled(context)
                hasCamera = ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
                isBatteryOptimized = !isIgnoringBatteryOptimizations(context)
                canExactAlarm = canScheduleExactAlarms(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Icon(
                Icons.Outlined.Security,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.permission_permissions_required),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.permission_permissions_required_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            TextButton(
                onClick = { showPrivacyInfoSheet = true }
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.permission_privacy_data_guarantees),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PermissionItemRow(
                title = stringResource(R.string.permission_notifications_title),
                description = stringResource(R.string.permission_notifications_description),
                isGranted = hasNotifications,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                },
                icon = Icons.Outlined.Notifications,
                position = GroupPosition.Top
            )

            Spacer(modifier = Modifier.height(4.dp))

            PermissionItemRow(
                title = stringResource(R.string.permission_usage_stats_title),
                description = stringResource(R.string.permission_usage_stats_description),
                isGranted = hasUsageStats,
                onClick = {
                    if (!openSystemSettings(context, Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.permission_settings_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                icon = Icons.Outlined.BarChart,
                position = GroupPosition.Middle
            )

            Spacer(modifier = Modifier.height(4.dp))

            PermissionItemRow(
                title = stringResource(R.string.permission_notification_policy_title),
                description = stringResource(R.string.permission_notification_policy_description),
                isGranted = hasNotificationPolicy,
                onClick = {
                    if (!openNotificationPolicySettings(context)) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.permission_settings_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                icon = Icons.Outlined.DoNotDisturbOn,
                position = GroupPosition.Middle
            )

            Spacer(modifier = Modifier.height(4.dp))

            PermissionItemRow(
                title = stringResource(R.string.permission_system_overlay_title),
                description = if (isAndroidGo(context)) stringResource(R.string.permission_system_overlay_go_description) else stringResource(R.string.permission_system_overlay_description),
                isGranted = hasOverlay,
                onClick = {
                    val opened = openSystemSettings(
                        context,
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).setData(
                            Uri.fromParts("package", context.packageName, null)
                        )
                    )
                    if (!opened) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.permission_settings_unavailable),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                icon = Icons.Outlined.Layers,
                position = if (preferences.accessibilityRequired) GroupPosition.Middle else GroupPosition.Bottom
            )

            if (preferences.accessibilityRequired) {
                Spacer(modifier = Modifier.height(4.dp))
                PermissionItemRow(
                    title = stringResource(R.string.permission_accessibility_title),
                    description = stringResource(R.string.permission_accessibility_description),
                    isGranted = hasAccessibility,
                    onClick = {
                        if (!preferences.bankingWarningDismissed) {
                            showBankingWarning = true
                        } else {
                            openAccessibilitySettings(context)
                        }
                    },
                    onInfoClick = { showBankingWarning = true },
                    icon = Icons.Outlined.AccessibilityNew,
                    position = GroupPosition.Bottom
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CollapsibleSection(
                title = stringResource(R.string.permission_optional_service_title),
                icon = Icons.Outlined.AccessibilityNew,
                expanded = optionalExpanded,
                onToggle = { optionalExpanded = !optionalExpanded },
                isAllGranted = optionalAllGranted
            ) {
                Column {
                    if (!preferences.accessibilityRequired) {
                        PermissionItemRow(
                            title = stringResource(R.string.permission_accessibility_title),
                            description = stringResource(R.string.permission_accessibility_description),
                            isGranted = hasAccessibility,
                            onClick = {
                                if (!preferences.bankingWarningDismissed) {
                                    showBankingWarning = true
                                } else {
                                    openAccessibilitySettings(context)
                                }
                            },
                            onInfoClick = { showBankingWarning = true },
                            icon = Icons.Outlined.AccessibilityNew,
                            position = GroupPosition.Top,
                            isInsideCollapse = true
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    PermissionItemRow(
                        title = stringResource(R.string.permission_intercept_title),
                        description = stringResource(R.string.permission_intercept_description),
                        isGranted = hasNotificationListener,
                        onClick = {
                            if (!preferences.interceptWarningDismissed) {
                                showInterceptWarning = true
                            } else {
                                if (!openSystemSettings(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.permission_settings_unavailable),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onInfoClick = { showInterceptWarning = true },
                        icon = Icons.Outlined.PhonelinkErase,
                        position = if (preferences.accessibilityRequired) GroupPosition.Top else GroupPosition.Middle,
                        isInsideCollapse = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PermissionItemRow(
                        title = stringResource(R.string.permission_calendar_title),
                        description = stringResource(R.string.permission_calendar_description),
                        isGranted = hasCalendar,
                        onClick = { openCalendarPermission() },
                        icon = Icons.Outlined.CalendarMonth,
                        position = GroupPosition.Middle,
                        isInsideCollapse = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PermissionItemRow(
                        title = stringResource(R.string.permission_camera_title),
                        description = if (cameraPermanentlyDenied)
                            stringResource(R.string.permission_camera_denied_description)
                        else
                            stringResource(R.string.permission_camera_description),
                        isGranted = hasCamera,
                        onClick = { openCameraPermission() },
                        icon = Icons.Outlined.QrCodeScanner,
                        position = GroupPosition.Middle,
                        isInsideCollapse = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PermissionItemRow(
                        title = stringResource(R.string.permission_nfc_title),
                        description = when {
                            !hasNfcSupport -> stringResource(R.string.permission_nfc_not_supported)
                            hasNfcEnabled -> stringResource(R.string.permission_nfc_ready)
                            else -> stringResource(R.string.permission_nfc_disabled)
                        },
                        isGranted = !hasNfcSupport || hasNfcEnabled,
                        onClick = { if (hasNfcSupport) openNfcSettings(context) },
                        icon = Icons.Outlined.Nfc,
                        position = GroupPosition.Bottom,
                        isInsideCollapse = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            CollapsibleSection(
                title = stringResource(R.string.permission_app_stability_title),
                icon = Icons.Outlined.AutoGraph,
                expanded = stabilityExpanded,
                onToggle = { stabilityExpanded = !stabilityExpanded },
                isAllGranted = stabilityAllGranted
            ) {
                Column {
                    PermissionItemRow(
                        title = stringResource(R.string.permission_battery_optimization_title),
                        description = stringResource(R.string.permission_battery_optimization_description),
                        isGranted = !isBatteryOptimized,
                        icon = Icons.Outlined.BatteryStd,
                        onClick = {
                            openSystemSettings(
                                context,
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                    .setData(Uri.parse("package:${context.packageName}")),
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            )
                        },
                        position = GroupPosition.Top,
                        isInsideCollapse = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    PermissionItemRow(
                        title = stringResource(R.string.permission_exact_alarms_title),
                        description = stringResource(R.string.permission_exact_alarms_description),
                        isGranted = canExactAlarm,
                        icon = Icons.Outlined.Alarm,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        },
                        position = GroupPosition.Bottom,
                        isInsideCollapse = true
                    )
                }
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
                if (!allGranted) {
                    Text(
                        text = if (missingRequiredCount == 1) {
                            stringResource(R.string.permission_missing_required_singular, missingRequiredCount)
                        } else {
                            stringResource(R.string.permission_missing_required_plural, missingRequiredCount)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                ZenithButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            if (allGranted) {
                                onAllPermissionsGranted()
                            } else {
                                onDismissRequest()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    text = if (allGranted) stringResource(R.string.permission_all_ready) else stringResource(R.string.permission_close_for_now),
                    type = if (allGranted) ZenithButtonType.Filled else ZenithButtonType.Outlined,
                    fillMaxWidth = true
                )
            }
        }
    }

    if (showBankingWarning) {
        BankingWarningBottomSheet(
            onDismiss = { showBankingWarning = false },
            onProceed = {
                showBankingWarning = false
                openAccessibilitySettings(context)
            },
            onDontShowAgain = {
                scope.launch {
                    preferencesRepository.setBankingWarningDismissed(true)
                }
            }
        )
    }

    if (showInterceptWarning) {
        InterceptServiceWarningBottomSheet(
            onDismiss = { showInterceptWarning = false },
            onOpenSettings = {
                showInterceptWarning = false
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            },
            onDontShowAgain = {
                scope.launch {
                    preferencesRepository.setInterceptWarningDismissed(true)
                }
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
private fun CollapsibleSection(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    isAllGranted: Boolean = false,
    content: @Composable () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isAllGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                      else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "sectionColor"
    )

    val contentColor = MaterialTheme.colorScheme.onSurface

    val iconPillColor = if (isAllGranted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.secondaryContainer

    val iconTint = if (isAllGranted) MaterialTheme.colorScheme.primary 
                   else MaterialTheme.colorScheme.secondary

    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .width(48.dp)
                            .background(iconPillColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(48.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Box(modifier = Modifier.padding(6.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun PermissionItemRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    position: GroupPosition = GroupPosition.Middle,
    isInsideCollapse: Boolean = false,
    onInfoClick: (() -> Unit)? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isGranted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            isInsideCollapse -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "bgColor"
    )

    val shape = when (position) {
        GroupPosition.Top -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        GroupPosition.Middle -> RoundedCornerShape(8.dp)
        GroupPosition.Bottom -> RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        GroupPosition.Single -> RoundedCornerShape(24.dp)
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
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .align(Alignment.CenterVertically)
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
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
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

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGranted) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        stringResource(R.string.permission_granted),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    if (onInfoClick != null) {
                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.permission_info),
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
                            text = stringResource(R.string.permission_grant),
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


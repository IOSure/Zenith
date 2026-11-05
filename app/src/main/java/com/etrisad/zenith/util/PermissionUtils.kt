package com.etrisad.zenith.util

import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityManager
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.PowerManager
import androidx.core.content.ContextCompat

fun hasNotificationPolicyAccess(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    return nm.isNotificationPolicyAccessGranted
}

fun hasAllPermissions(context: Context): Boolean {
    val hasUsageStats = hasUsageStatsPermission(context)
    val hasOverlay = Settings.canDrawOverlays(context)
    val hasAccessibility = isAccessibilityServiceEnabled(context)
    
    return hasUsageStats && hasOverlay && hasAccessibility
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expected = android.content.ComponentName(context.packageName, "com.etrisad.zenith.service.ZenithService")
    val enabledServices = android.provider.Settings.Secure.getString(
        context.contentResolver,
        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: ""

    val inSettings = enabledServices.split(':').any { raw ->
        android.content.ComponentName.unflattenFromString(raw) == expected
    }
    if (inSettings) return true

    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { it.resolveInfo?.serviceInfo?.packageName == context.packageName }
}

fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

fun hasNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

fun isNotificationListenerEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (!flat.isNullOrEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val cn = android.content.ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == pkgName) {
                return true
            }
        }
    }
    return false
}

fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

fun canScheduleExactAlarms(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

fun isAndroidGo(context: Context): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    return activityManager.isLowRamDevice
}

fun hasCalendarPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
}

/**
 * Starts the first settings screen that this device can actually handle.
 *
 * Some OEM ROMs strip individual Settings activities (for example the
 * notification policy access screen), and calling startActivity() for them
 * throws ActivityNotFoundException. Returns false when none of the candidates
 * can be opened, so callers can show a hint instead of crashing.
 */
fun openSystemSettings(context: Context, primary: Intent, vararg fallbacks: Intent): Boolean {
    for (candidate in listOf(primary) + fallbacks) {
        val intent = Intent(candidate).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            return true
        } catch (_: ActivityNotFoundException) {
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
    }
    return false
}

fun openNotificationPolicySettings(context: Context): Boolean = openSystemSettings(
    context,
    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.fromParts("package", context.packageName, null)),
    Intent(Settings.ACTION_SETTINGS),
)

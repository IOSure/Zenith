package com.etrisad.zenith.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.etrisad.zenith.R
import com.etrisad.zenith.data.local.database.ZenithDatabase
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationInsightsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (com.etrisad.zenith.crash.SafeMode.isEnabled(applicationContext)) return Result.success()
        val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!pm.isInteractive) {
            return Result.success()
        }
        val type = inputData.getString(KEY_INSIGHT_TYPE) ?: return Result.failure()
        val prefsRepo = UserPreferencesRepository(applicationContext)
        val prefs = prefsRepo.userPreferencesFlow.first()

        val channelId = "zenith_insights"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    applicationContext.getString(R.string.usage_history_channel_insights),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
        }

        val database = ZenithDatabase.getDatabase(applicationContext)
        val dailyUsageDao = database.dailyUsageDao()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val cal = Calendar.getInstance()

        when (type) {
            TYPE_DAILY_RECAP -> {
                if (!prefs.dailyRecapEnabled) return Result.success()

                val todayStr = dateFormat.format(cal.time)
                val yesterdayCal = cal.clone() as Calendar; yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = dateFormat.format(yesterdayCal.time)

                val todayTotal = withContext(Dispatchers.IO) {
                    dailyUsageDao.getUsageByDateAndPackage(todayStr, "TOTAL")?.usageTimeMillis ?: 0L
                }
                val yesterdayTotal = withContext(Dispatchers.IO) {
                    dailyUsageDao.getUsageByDateAndPackage(yesterdayStr, "TOTAL")?.usageTimeMillis ?: 0L
                }

                val shieldToday = withContext(Dispatchers.IO) {
                    dailyUsageDao.getUsageByDateAndPackage(todayStr, "SHIELD_TOTAL")?.usageTimeMillis ?: 0L
                }
                val goalToday = withContext(Dispatchers.IO) {
                    dailyUsageDao.getUsageByDateAndPackage(todayStr, "GOAL_TOTAL")?.usageTimeMillis ?: 0L
                }

                if (todayTotal <= 0 && shieldToday <= 0) return Result.success()

                val todayMinutes = todayTotal / 60000
                val yesterdayMinutes = yesterdayTotal / 60000
                val shieldMinutes = shieldToday / 60000
                val goalMinutes = goalToday / 60000
                val otherMinutes = ((todayTotal - shieldToday - goalToday) / 60000).coerceAtLeast(0)

                val targetMinutes = prefs.screenTimeTargetMinutes
                val message = buildString {
                    append(applicationContext.getString(R.string.usage_history_total_screen_time, formatDuration(todayMinutes)))
                    if (targetMinutes > 0) {
                        val diff = todayMinutes - targetMinutes
                        if (diff > 0) append(
                            " " + applicationContext.getString(R.string.usage_history_over_target, formatDuration(diff))
                        )
                        else append(
                            " " + applicationContext.getString(R.string.usage_history_under_target, formatDuration(-diff))
                        )
                    }
                    if (yesterdayMinutes > 0) {
                        val change = ((todayMinutes - yesterdayMinutes).toFloat() / yesterdayMinutes * 100).toInt()
                        append(
                            " " + applicationContext.getString(
                                R.string.usage_history_vs_yesterday,
                                if (change >= 0) "+" else "",
                                change
                            )
                        )
                    }
                }

                val detailLines = mutableListOf<String>()
                if (shieldMinutes > 0) detailLines.add(
                    applicationContext.getString(R.string.usage_history_blocked_usage, formatDuration(shieldMinutes))
                )
                if (goalMinutes > 0) detailLines.add(
                    applicationContext.getString(R.string.usage_history_goal_usage, formatDuration(goalMinutes))
                )
                if (otherMinutes > 0) detailLines.add(
                    applicationContext.getString(R.string.usage_history_other_usage, formatDuration(otherMinutes))
                )

                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setContentTitle(applicationContext.getString(R.string.usage_history_daily_recap_title))
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(
                        if (detailLines.isNotEmpty()) "$message\n\n${detailLines.joinToString("\n")}" else message
                    ))
                    .setSmallIcon(R.drawable.ic_calendar)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setGroup("zenith_insights")
                    .build()

                manager.notify(NOTIFICATION_DAILY_RECAP, notification)
            }

            TYPE_WEEKLY_INSIGHT -> {
                if (!prefs.weeklyInsightEnabled) return Result.success()

                val todayStr = dateFormat.format(cal.time)
                val weekAgoCal = cal.clone() as Calendar; weekAgoCal.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgoStr = dateFormat.format(weekAgoCal.time)

                val weeklyData = withContext(Dispatchers.IO) {
                    dailyUsageDao.getRecentUsage(weekAgoStr).first()
                }

                var weeklyTotal = 0L; var blockedTotal = 0L; var goalTotal = 0L; var dailyValues = mutableListOf<Long>()
                for (usage in weeklyData) {
                    when (usage.packageName) {
                        "TOTAL" -> { weeklyTotal += usage.usageTimeMillis; dailyValues.add(usage.usageTimeMillis / 60000) }
                        "SHIELD_TOTAL" -> blockedTotal += usage.usageTimeMillis
                        "GOAL_TOTAL" -> goalTotal += usage.usageTimeMillis
                        "OTHER_TOTAL" -> {}
                    }
                }

                if (weeklyTotal <= 0) return Result.success()

                val weeklyMinutes = weeklyTotal / 60000
                val avgDaily = if (dailyValues.isNotEmpty()) dailyValues.average().toInt() else 0
                val bestDay = dailyValues.maxOrNull() ?: 0
                val worstDay = dailyValues.minOrNull() ?: 0

                val title = applicationContext.getString(R.string.usage_history_weekly_insight_title)
                val message = applicationContext.getString(R.string.usage_history_weekly_total, formatDuration(weeklyMinutes))
                val detailLines = mutableListOf<String>()
                detailLines.add(
                    applicationContext.getString(R.string.usage_history_daily_average, formatDuration(avgDaily.toLong()))
                )
                detailLines.add(
                    applicationContext.getString(
                        R.string.usage_history_most_least,
                        formatDuration(bestDay),
                        formatDuration(worstDay)
                    )
                )
                if (blockedTotal > 0) detailLines.add(
                    applicationContext.getString(R.string.usage_history_blocked_label, formatDuration(blockedTotal / 60000))
                )
                if (goalTotal > 0) detailLines.add(
                    applicationContext.getString(R.string.usage_history_goals_label, formatDuration(goalTotal / 60000))
                )

                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$message\n\n${detailLines.joinToString("\n")}"))
                    .setSmallIcon(R.drawable.ic_analytics)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setGroup("zenith_insights")
                    .build()

                manager.notify(NOTIFICATION_WEEKLY_INSIGHT, notification)
            }

            TYPE_MILESTONE -> {
                if (!prefs.trendMilestoneEnabled) return Result.success()

                val milestoneType = inputData.getString(KEY_MILESTONE_TYPE) ?: return Result.success()
                val value = inputData.getInt(KEY_MILESTONE_VALUE, 0)

                val title: String
                val message: String

                when (milestoneType) {
                    MILESTONE_SCREEN_TIME -> {
                        val days = value
                        title = applicationContext.getString(R.string.usage_history_milestone_screen_time_title)
                        message = applicationContext.getString(R.string.usage_history_milestone_screen_time_message, days)
                    }
                    MILESTONE_STREAK -> {
                        val streakDays = value
                        title = applicationContext.getString(R.string.usage_history_milestone_streak_title)
                        message = applicationContext.getString(R.string.usage_history_milestone_streak_message, streakDays)
                    }
                    MILESTONE_FOCUS_HOURS -> {
                        val hours = value
                        title = applicationContext.getString(R.string.usage_history_milestone_focus_title)
                        message = applicationContext.getString(R.string.usage_history_milestone_focus_message, hours)
                    }
                    else -> return Result.success()
                }

                val notification = NotificationCompat.Builder(applicationContext, channelId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_crown)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setGroup("zenith_insights")
                    .build()

                manager.notify(NOTIFICATION_MILESTONE, notification)
            }
        }

        return Result.success()
    }

    private fun formatDuration(totalMinutes: Long): String {
        val m = totalMinutes
        return when {
            m <= 0 -> applicationContext.getString(R.string.usage_history_duration_zero)
            m < 60 -> applicationContext.getString(R.string.usage_history_duration_minutes, m)
            m < 1440 -> {
                val h = m / 60
                val r = m % 60
                if (r > 0) applicationContext.getString(R.string.usage_history_duration_hours_minutes, h, r)
                else applicationContext.getString(R.string.usage_history_duration_hours, h)
            }
            else -> {
                val d = m / 1440
                val h = (m % 1440) / 60
                if (h > 0) applicationContext.getString(R.string.usage_history_duration_days_hours, d, h)
                else applicationContext.getString(R.string.usage_history_duration_days, d)
            }
        }
    }

    companion object {
        const val KEY_INSIGHT_TYPE = "insight_type"
        const val TYPE_DAILY_RECAP = "daily_recap"
        const val TYPE_WEEKLY_INSIGHT = "weekly_insight"
        const val TYPE_MILESTONE = "milestone"
        const val KEY_MILESTONE_TYPE = "milestone_type"
        const val KEY_MILESTONE_VALUE = "milestone_value"
        const val MILESTONE_SCREEN_TIME = "screen_time"
        const val MILESTONE_STREAK = "streak"
        const val MILESTONE_FOCUS_HOURS = "focus_hours"
        const val NOTIFICATION_DAILY_RECAP = 1001
        const val NOTIFICATION_WEEKLY_INSIGHT = 1002
        const val NOTIFICATION_MILESTONE = 1003

        fun scheduleDailyRecap(context: Context) {
            val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.set(Calendar.HOUR_OF_DAY, 19); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis <= now) calendar.add(Calendar.DAY_OF_YEAR, 1)

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyRecapWorker", ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<NotificationInsightsWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(calendar.timeInMillis - now, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(workDataOf(KEY_INSIGHT_TYPE to TYPE_DAILY_RECAP))
                    .build()
            )
        }

        fun scheduleWeeklyInsight(context: Context) {
            val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            calendar.set(Calendar.HOUR_OF_DAY, 19); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis <= now) calendar.add(Calendar.DAY_OF_YEAR, 7)

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "WeeklyInsightWorker", ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<NotificationInsightsWorker>(7, TimeUnit.DAYS)
                    .setInitialDelay(calendar.timeInMillis - now, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(workDataOf(KEY_INSIGHT_TYPE to TYPE_WEEKLY_INSIGHT))
                    .build()
            )
        }

        fun scheduleMilestone(context: Context, type: String, value: Int) {
            val constraints = Constraints.Builder().build()
            WorkManager.getInstance(context).enqueue(
                OneTimeWorkRequestBuilder<NotificationInsightsWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf(
                        KEY_INSIGHT_TYPE to TYPE_MILESTONE,
                        KEY_MILESTONE_TYPE to type,
                        KEY_MILESTONE_VALUE to value
                    ))
                    .build()
            )
        }

        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("DailyRecapWorker")
            WorkManager.getInstance(context).cancelUniqueWork("WeeklyInsightWorker")
        }
    }
}

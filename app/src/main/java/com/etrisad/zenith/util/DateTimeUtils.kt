package com.etrisad.zenith.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.etrisad.zenith.R

object DateTimeUtils {
    private val dateFormatTL = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue() = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    fun getDateFormat(): SimpleDateFormat = dateFormatTL.get()!!

    /**
     * Compact, localized weekday label for the given day, used under the
     * history bars ("一".."日" in Chinese, "M".."S" in English).
     *
     * The resource array is Sunday-first so its index matches
     * [Calendar.DAY_OF_WEEK] minus one.
     */
    fun weekdayShortLabel(context: android.content.Context, timeMillis: Long): String {
        val labels = context.resources.getStringArray(R.array.weekday_short_labels)
        val dayOfWeek = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
            .get(Calendar.DAY_OF_WEEK)
        return labels.getOrElse(dayOfWeek - 1) { "" }
    }

    fun getDayStartTime(
        now: Long = System.currentTimeMillis(),
        dayStartHour: Int = 0,
        dayStartMinute: Int = 0
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, dayStartHour)
            set(Calendar.MINUTE, dayStartMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDayStart = cal.timeInMillis
        return if (now >= todayDayStart) todayDayStart
        else {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            cal.timeInMillis
        }
    }

    fun getDayStartDateString(
        now: Long = System.currentTimeMillis(),
        dayStartHour: Int = 0,
        dayStartMinute: Int = 0
    ): String {
        val dayStart = getDayStartTime(now, dayStartHour, dayStartMinute)
        return getDateFormat().format(Date(dayStart))
    }

    fun isBeforeDayStart(
        now: Long = System.currentTimeMillis(),
        dayStartHour: Int = 0,
        dayStartMinute: Int = 0
    ): Boolean {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, dayStartHour)
            set(Calendar.MINUTE, dayStartMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return now < cal.timeInMillis
    }

    fun getDayStartForDate(
        dateMillis: Long,
        dayStartHour: Int = 0,
        dayStartMinute: Int = 0
    ): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, dayStartHour)
            set(Calendar.MINUTE, dayStartMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun getOffsetsFromDayStart(
        now: Long = System.currentTimeMillis(),
        dayStartHour: Int = 0,
        dayStartMinute: Int = 0
    ): Long {
        val dayStart = getDayStartTime(now, dayStartHour, dayStartMinute)
        return (now - dayStart).coerceAtLeast(0L)
    }
    fun formatDateRange(
        startMillis: Long,
        endMillis: Long,
        locale: Locale = Locale.ENGLISH
    ): String {
        val startCal = Calendar.getInstance().apply { timeInMillis = startMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }
        val dayFmt = SimpleDateFormat("d", locale)
        val dayMonthFmt = SimpleDateFormat("d MMM", locale)
        val fullFmt = SimpleDateFormat("d MMM yyyy", locale)
        val sameDay = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)
        if (sameDay) return fullFmt.format(Date(endMillis))
        val sameMonth = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)
        return if (sameMonth) {
            "${dayFmt.format(Date(startMillis))} - ${fullFmt.format(Date(endMillis))}"
        } else {
            "${dayMonthFmt.format(Date(startMillis))} - ${fullFmt.format(Date(endMillis))}"
        }
    }
    fun isPastScheduleEnd(startMinutes: Int, endMinutes: Int, currentTotalMinutes: Int): Boolean = when {
        startMinutes == endMinutes -> false
        startMinutes < endMinutes -> currentTotalMinutes >= endMinutes
        else -> currentTotalMinutes >= endMinutes && currentTotalMinutes < startMinutes
    }
}

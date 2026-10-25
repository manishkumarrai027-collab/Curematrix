package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDay(
    val dayOfMonth: Int,
    val month: Int, // 0-indexed (0 = Jan, 9 = Oct)
    val year: Int,
    val millis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val dayOfWeek: Int // Calendar.SUNDAY = 1, etc.
) {
    val dayOfWeekShort: String
        get() = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
}

object DateUtils {

    fun getStartOfDay(millis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(millis: Long): Boolean {
        return isSameDay(millis, System.currentTimeMillis())
    }

    fun formatDate(millis: Long, pattern: String): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return formatDate(millis, "MMM dd, yyyy")
    }

    fun formatDayMonth(millis: Long): String {
        return formatDate(millis, "EEE, MMM dd")
    }

    fun formatFullDate(millis: Long): String {
        return formatDate(millis, "EEEE, MMMM dd, yyyy")
    }

    fun formatMonthYear(year: Int, month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }

    fun getRelativeDateTitle(millis: Long): String {
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val targetStart = getStartOfDay(millis)
        val diffDays = (targetStart - todayStart) / (24 * 60 * 60 * 1000)

        return when (diffDays) {
            0L -> "Today"
            1L -> "Tomorrow"
            -1L -> "Yesterday"
            in 2L..6L -> formatDate(millis, "EEEE, MMM dd")
            else -> formatDate(millis, "MMM dd, yyyy")
        }
    }

    /**
     * Generates a 35 or 42 item grid for the specified month and year,
     * including padding days from the previous month and next month.
     */
    fun getMonthMatrix(year: Int, month: Int): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Day of week for 1st of month (1 = Sunday)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val paddingBefore = firstDayOfWeek - Calendar.SUNDAY

        // Previous month padding
        val prevCal = calendar.clone() as Calendar
        prevCal.add(Calendar.DAY_OF_MONTH, -paddingBefore)
        for (i in 0 until paddingBefore) {
            val millis = prevCal.timeInMillis
            days.add(
                CalendarDay(
                    dayOfMonth = prevCal.get(Calendar.DAY_OF_MONTH),
                    month = prevCal.get(Calendar.MONTH),
                    year = prevCal.get(Calendar.YEAR),
                    millis = millis,
                    isCurrentMonth = false,
                    isToday = isToday(millis),
                    dayOfWeek = prevCal.get(Calendar.DAY_OF_WEEK)
                )
            )
            prevCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Current month days
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..maxDays) {
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val millis = calendar.timeInMillis
            days.add(
                CalendarDay(
                    dayOfMonth = day,
                    month = month,
                    year = year,
                    millis = millis,
                    isCurrentMonth = true,
                    isToday = isToday(millis),
                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                )
            )
        }

        // Next month padding to fill out complete weeks (grid rows of 7)
        val totalCells = if (days.size <= 35) 35 else 42
        val nextCal = calendar.clone() as Calendar
        nextCal.add(Calendar.DAY_OF_MONTH, 1)
        while (days.size < totalCells) {
            val millis = nextCal.timeInMillis
            days.add(
                CalendarDay(
                    dayOfMonth = nextCal.get(Calendar.DAY_OF_MONTH),
                    month = nextCal.get(Calendar.MONTH),
                    year = nextCal.get(Calendar.YEAR),
                    millis = millis,
                    isCurrentMonth = false,
                    isToday = isToday(millis),
                    dayOfWeek = nextCal.get(Calendar.DAY_OF_WEEK)
                )
            )
            nextCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    /**
     * Generates a continuous horizontal list of days around the centerMillis
     * (e.g. 7 days before to 14 days after).
     */
    fun getWeekStripDays(centerMillis: Long, daysBefore: Int = 3, daysAfter: Int = 10): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = getStartOfDay(centerMillis)
        calendar.add(Calendar.DAY_OF_MONTH, -daysBefore)

        val totalDays = daysBefore + 1 + daysAfter
        for (i in 0 until totalDays) {
            val millis = calendar.timeInMillis
            days.add(
                CalendarDay(
                    dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                    month = calendar.get(Calendar.MONTH),
                    year = calendar.get(Calendar.YEAR),
                    millis = millis,
                    isCurrentMonth = true,
                    isToday = isToday(millis),
                    dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
}

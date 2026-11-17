package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MedicationSlot {
    MORNING,
    LUNCH,
    DINNER
}

data class DailyCareRecord(
    val dateKey: String,
    val morningTaken: Boolean = false,
    val lunchTaken: Boolean = false,
    val dinnerTaken: Boolean = false,
    val waterCups: Int = 0
) {
    val completedMedicationCount: Int
        get() = listOf(morningTaken, lunchTaken, dinnerTaken).count { it }

    val allMedicationsTaken: Boolean
        get() = morningTaken && lunchTaken && dinnerTaken
}

object KoreanDateFormatter {
    fun todayKey(date: Date = Date()): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(date)

    fun formatKoreanDate(date: Date = Date()): String =
        SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA).format(date)

    fun formatKoreanDayOfWeek(date: Date = Date()): String =
        SimpleDateFormat("EEEE", Locale.KOREA).format(date)

    fun millisUntilNextMidnight(now: Date = Date()): Long {
        val nextMidnight = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 50)
        }
        return (nextMidnight.timeInMillis - now.time).coerceAtLeast(100L)
    }
}

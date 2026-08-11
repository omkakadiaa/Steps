package com.steps.app.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    private val dayFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)
    private val fullFmt = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)
    private val shortDay = DateTimeFormatter.ofPattern("EEE", Locale.US)
    private val monthYear = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    fun today(): String = LocalDate.now().toString()
    fun formatDay(date: String): String = LocalDate.parse(date).format(dayFmt)
    fun formatFull(date: String): String = LocalDate.parse(date).format(fullFmt)
    fun formatShortWeekday(date: String): String = LocalDate.parse(date).format(shortDay)
    fun formatMonthYear(yearMonth: YearMonth): String = yearMonth.atDay(1).format(monthYear)
    fun monthName(month: Int): String =
        java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.US)
}

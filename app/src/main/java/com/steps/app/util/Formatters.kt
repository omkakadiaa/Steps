package com.steps.app.util

import java.text.NumberFormat
import java.util.Locale

object Formatters {
    private val intFmt = NumberFormat.getIntegerInstance(Locale.US)
    fun steps(value: Int): String = intFmt.format(value)
    fun steps(value: Long): String = intFmt.format(value)
    fun compact(value: Int): String = when {
        value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        value >= 1_000 -> String.format(Locale.US, "%.1fk", value / 1_000.0)
        else -> value.toString()
    }
    fun compact(value: Long): String = when {
        value >= 1_000_000 -> String.format(Locale.US, "%.2fM", value / 1_000_000.0)
        value >= 10_000 -> String.format(Locale.US, "%.1fk", value / 1_000.0)
        else -> intFmt.format(value)
    }
}

package ru.example.expensetracker.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Money {
    private const val MAX_DIGITS = 11

    private fun symbols() = DecimalFormatSymbols(Locale("ru")).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }

    /** Из любого текста берём только цифры - получаем сумму в копейках. */
    fun parse(text: String): Long =
        text.filter { it.isDigit() }.trimStart('0').take(MAX_DIGITS).toLongOrNull() ?: 0L

    /** 123456 -> "1 234,56" */
    fun format(cents: Long): String =
        DecimalFormat("#,##0", symbols()).format(cents / 100) +
            "," + (cents % 100).toString().padStart(2, '0')
}

object Dates {
    fun startOfDay(ms: Long): Long = Calendar.getInstance().apply {
        timeInMillis = ms
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun today(): Long = startOfDay(System.currentTimeMillis())

    /** MaterialDatePicker работает в UTC, а мы храним локальную полночь. */
    fun fromPicker(utcMs: Long): Long {
        val u = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMs }
        return Calendar.getInstance().apply {
            clear(); set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun toPicker(localMs: Long): Long {
        val l = Calendar.getInstance().apply { timeInMillis = localMs }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear(); set(l.get(Calendar.YEAR), l.get(Calendar.MONTH), l.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    fun format(ms: Long): String = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(ms))
    fun formatMonth(ms: Long): String = SimpleDateFormat("MM.yyyy", Locale("ru")).format(Date(ms))

    /** Границы текущего месяца: (начало, конец) в миллисекундах. */
    fun monthRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.MONTH, 1); add(Calendar.MILLISECOND, -1)
        }
        return start.timeInMillis to end.timeInMillis
    }
}

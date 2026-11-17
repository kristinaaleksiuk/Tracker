package ru.example.expensetracker.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * TextWatcher-форматирование суммы: вводятся только цифры, 
 * справа налево заполняются копейки: 1 -> "0,01", 12345 -> "123,45", 1234567 -> "12 345,67".
 * Текст меняем только в afterTextChanged, флаг busy защищает от рекурсии.
 */
class MoneyMaskWatcher(private val editText: EditText) : TextWatcher {
    private var busy = false

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (busy || s == null) return
        busy = true
        val cents = Money.parse(s.toString())
        val formatted = if (cents == 0L) "" else Money.format(cents)
        if (formatted != s.toString()) s.replace(0, s.length, formatted)
        editText.setSelection(editText.text?.length ?: 0)
        busy = false
    }
}

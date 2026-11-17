package ru.example.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.example.expensetracker.ExpenseApp
import ru.example.expensetracker.R
import ru.example.expensetracker.data.CategoryTotal
import ru.example.expensetracker.util.Dates
import ru.example.expensetracker.util.Money

class StatsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as ExpenseApp).repository
    private val range = Dates.monthRange()

    val byCategory: StateFlow<List<CategoryTotal>> = repo.byCategory(range.first, range.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Текст отчёта за текущий месяц для экспорта в файл. */
    suspend fun buildReport(): String {
        val res = getApplication<Application>().resources
        val names = res.getStringArray(R.array.categories)
        val items = repo.forPeriod(range.first, range.second)
        val sb = StringBuilder()
        sb.appendLine(res.getString(R.string.report_title, Dates.formatMonth(range.first)))
        sb.appendLine()
        items.forEach {
            sb.appendLine(
                "${Dates.format(it.date)} | ${names[it.categoryId.coerceIn(names.indices)]} | " +
                    "${Money.format(it.amount)} | ${it.comment}"
            )
        }
        sb.appendLine()
        sb.appendLine(res.getString(R.string.report_by_category))
        byCategory.value.forEach {
            sb.appendLine("${names[it.categoryId.coerceIn(names.indices)]}: ${Money.format(it.total)}")
        }
        sb.appendLine()
        sb.appendLine(res.getString(R.string.report_total, Money.format(items.sumOf { it.amount })))
        return sb.toString()
    }
}

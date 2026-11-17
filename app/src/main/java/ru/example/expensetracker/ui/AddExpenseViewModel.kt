package ru.example.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import ru.example.expensetracker.ExpenseApp
import ru.example.expensetracker.data.Expense
import ru.example.expensetracker.util.Dates

/** SavedStateHandle: данные переживают и поворот экрана, и убийство процесса. */
class AddExpenseViewModel(
    app: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    private val repo = (app as ExpenseApp).repository

    var categoryId: Int
        get() = state.get<Int>("category") ?: 0
        set(value) { state["category"] = value }

    var dateMillis: Long
        get() = state.get<Long>("date") ?: Dates.today().also { state["date"] = it }
        set(value) { state["date"] = value }

    suspend fun save(amountCents: Long, comment: String) {
        repo.add(Expense(amount = amountCents, categoryId = categoryId, date = dateMillis, comment = comment))
    }
}

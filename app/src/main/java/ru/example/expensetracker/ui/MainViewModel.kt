package ru.example.expensetracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.example.expensetracker.ExpenseApp
import ru.example.expensetracker.data.Expense
import ru.example.expensetracker.util.Dates

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as ExpenseApp).repository
    private val month = Dates.monthRange()

    val expenses: StateFlow<List<Expense>> = repo.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthTotal: StateFlow<Long> = repo.total(month.first, month.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun delete(expense: Expense) {
        viewModelScope.launch { repo.delete(expense) }
    }
}

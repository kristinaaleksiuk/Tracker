package ru.example.expensetracker.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {
    fun all(): Flow<List<Expense>> = dao.observeAll()
    fun total(from: Long, to: Long): Flow<Long> = dao.observeTotal(from, to)
    fun byCategory(from: Long, to: Long): Flow<List<CategoryTotal>> = dao.observeByCategory(from, to)
    suspend fun forPeriod(from: Long, to: Long): List<Expense> = dao.getForPeriod(from, to)
    suspend fun add(expense: Expense) = dao.insert(expense)
    suspend fun delete(expense: Expense) = dao.delete(expense)
}

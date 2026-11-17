package ru.example.expensetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun observeAll(): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE date BETWEEN :from AND :to")
    fun observeTotal(from: Long, to: Long): Flow<Long>

    @Query(
        "SELECT categoryId, SUM(amount) AS total FROM expenses " +
            "WHERE date BETWEEN :from AND :to GROUP BY categoryId ORDER BY total DESC"
    )
    fun observeByCategory(from: Long, to: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :from AND :to ORDER BY date")
    suspend fun getForPeriod(from: Long, to: Long): List<Expense>

    @Insert
    suspend fun insert(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}

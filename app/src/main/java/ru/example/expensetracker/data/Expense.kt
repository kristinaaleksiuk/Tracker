package ru.example.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** amount хранится в копейках (Long), чтобы избежать ошибок округления Double. */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Long,
    val categoryId: Int,
    val date: Long,
    val comment: String
)

data class CategoryTotal(val categoryId: Int, val total: Long)

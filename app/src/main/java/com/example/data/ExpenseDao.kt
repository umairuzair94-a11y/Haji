package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ExpenseWithCategory(
    val expenseId: Int,
    val amount: Double,
    val date: Long,
    val notes: String,
    val receiptPhotoPath: String?,
    val categoryId: Int,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String
)

@Dao
interface ExpenseDao {
    @Query("""
        SELECT 
            e.id AS expenseId,
            e.amount AS amount,
            e.date AS date,
            e.notes AS notes,
            e.receiptPhotoPath AS receiptPhotoPath,
            e.categoryId AS categoryId,
            c.name AS categoryName,
            c.iconName AS categoryIcon,
            c.colorHex AS categoryColor
        FROM expenses e
        LEFT JOIN categories c ON e.categoryId = c.id
        ORDER BY e.date DESC
    """)
    fun getAllExpenses(): Flow<List<ExpenseWithCategory>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): ExpenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}

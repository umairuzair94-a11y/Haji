package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExpenseRepository(application)
    private val context = application.applicationContext

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseWithCategory>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings & Preferences
    private val prefs = context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)
    
    private val _currency = MutableStateFlow(prefs.getString("currency", "PKR") ?: "PKR")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", true)) // Default to Dark Mode for premium feel
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(prefs.getFloat("monthly_budget", 50000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultCategories()
            triggerWidgetUpdate()
        }
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
        prefs.edit().putString("currency", newCurrency).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        prefs.edit().putFloat("monthly_budget", budget.toFloat()).apply()
    }

    // Expense Operations
    fun saveExpense(amount: Double, categoryId: Int, date: Long, notes: String, receiptPhotoPath: String? = null) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                amount = amount,
                categoryId = categoryId,
                date = date,
                notes = notes,
                receiptPhotoPath = receiptPhotoPath
            )
            repository.insertExpense(expense)
            triggerWidgetUpdate()
        }
    }

    fun updateExpense(expenseId: Int, amount: Double, categoryId: Int, date: Long, notes: String, receiptPhotoPath: String? = null) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                id = expenseId,
                amount = amount,
                categoryId = categoryId,
                date = date,
                notes = notes,
                receiptPhotoPath = receiptPhotoPath
            )
            repository.updateExpense(expense)
            triggerWidgetUpdate()
        }
    }

    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            repository.deleteExpenseById(expenseId)
            triggerWidgetUpdate()
        }
    }

    // Category Operations
    fun saveCategory(name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val category = CategoryEntity(name = name, iconName = iconName, colorHex = colorHex, isDefault = false)
            repository.insertCategory(category)
        }
    }

    fun updateCategory(categoryId: Int, name: String, iconName: String, colorHex: String, isDefault: Boolean) {
        viewModelScope.launch {
            val category = CategoryEntity(id = categoryId, name = name, iconName = iconName, colorHex = colorHex, isDefault = isDefault)
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // Backup & Restore
    fun getBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.getBackupJson()
            onResult(json)
        }
    }

    fun restoreFromJson(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreFromJson(jsonString)
            if (success) {
                triggerWidgetUpdate()
            }
            onResult(success)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllExpenses()
            triggerWidgetUpdate()
        }
    }

    // Trigger widget updates
    fun triggerWidgetUpdate() {
        try {
            val intent = android.content.Intent(context, Class.forName("com.example.widget.ExpenseWidgetProvider")).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = android.appwidget.AppWidgetManager.getInstance(context).getAppWidgetIds(
                android.content.ComponentName(context, Class.forName("com.example.widget.ExpenseWidgetProvider"))
            )
            intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

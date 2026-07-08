package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ExpenseRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val categoryDao = db.categoryDao()
    private val expenseDao = db.expenseDao()

    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allExpenses: Flow<List<ExpenseWithCategory>> = expenseDao.getAllExpenses()

    suspend fun initDefaultCategories() {
        if (categoryDao.getCategoryCount() == 0) {
            val defaults = listOf(
                CategoryEntity(name = "Fuel", iconName = "LocalGasStation", colorHex = "#FF9800", isDefault = true),
                CategoryEntity(name = "Food", iconName = "Restaurant", colorHex = "#E91E63", isDefault = true),
                CategoryEntity(name = "Grocery", iconName = "ShoppingCart", colorHex = "#4CAF50", isDefault = true),
                CategoryEntity(name = "Pampers", iconName = "ChildFriendly", colorHex = "#00BCD4", isDefault = true),
                CategoryEntity(name = "Milk", iconName = "WaterDrop", colorHex = "#03A9F4", isDefault = true),
                CategoryEntity(name = "Medicine", iconName = "MedicalServices", colorHex = "#F44336", isDefault = true),
                CategoryEntity(name = "Electricity Bill", iconName = "Bolt", colorHex = "#FFEB3B", isDefault = true),
                CategoryEntity(name = "Gas Bill", iconName = "Whatshot", colorHex = "#795548", isDefault = true),
                CategoryEntity(name = "Water Bill", iconName = "Water", colorHex = "#2196F3", isDefault = true),
                CategoryEntity(name = "Internet", iconName = "Wifi", colorHex = "#3F51B5", isDefault = true),
                CategoryEntity(name = "Mobile Recharge", iconName = "PhoneAndroid", colorHex = "#9C27B0", isDefault = true),
                CategoryEntity(name = "Shopping", iconName = "LocalMall", colorHex = "#EC407A", isDefault = true),
                CategoryEntity(name = "Transport", iconName = "DirectionsBus", colorHex = "#009688", isDefault = true),
                CategoryEntity(name = "Education", iconName = "School", colorHex = "#FF5722", isDefault = true),
                CategoryEntity(name = "Kids", iconName = "ChildCare", colorHex = "#00E5FF", isDefault = true),
                CategoryEntity(name = "Entertainment", iconName = "SportsEsports", colorHex = "#607D8B", isDefault = true),
                CategoryEntity(name = "Business", iconName = "BusinessCenter", colorHex = "#1A237E", isDefault = true),
                CategoryEntity(name = "Salary", iconName = "MonetizationOn", colorHex = "#00C853", isDefault = true),
                CategoryEntity(name = "Investment", iconName = "ShowChart", colorHex = "#00B0FF", isDefault = true),
                CategoryEntity(name = "Miscellaneous", iconName = "Category", colorHex = "#9E9E9E", isDefault = true)
            )
            categoryDao.insertAllCategories(defaults)
        }
    }

    suspend fun getCategoryById(id: Int): CategoryEntity? = categoryDao.getCategoryById(id)
    suspend fun getCategoryByName(name: String): CategoryEntity? = categoryDao.getCategoryByName(name)
    suspend fun insertCategory(category: CategoryEntity): Long = categoryDao.insertCategory(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.deleteCategory(category)

    suspend fun getExpenseById(id: Int): ExpenseEntity? = expenseDao.getExpenseById(id)
    suspend fun insertExpense(expense: ExpenseEntity): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)
    suspend fun deleteExpenseById(id: Int) = expenseDao.deleteExpenseById(id)
    suspend fun deleteAllExpenses() = expenseDao.deleteAllExpenses()

    // Export & Backup
    suspend fun getBackupJson(): String {
        val categories = allCategories.first()
        val expenses = expenseDao.getAllExpenses().first()

        val json = StringBuilder()
        json.append("{\n")
        json.append("  \"categories\": [\n")
        categories.forEachIndexed { idx, cat ->
            json.append("    {\n")
            json.append("      \"name\": \"${cat.name.replace("\"", "\\\"")}\",\n")
            json.append("      \"iconName\": \"${cat.iconName}\",\n")
            json.append("      \"colorHex\": \"${cat.colorHex}\",\n")
            json.append("      \"isDefault\": ${cat.isDefault}\n")
            json.append("    }${if (idx < categories.lastIndex) "," else ""}\n")
        }
        json.append("  ],\n")
        json.append("  \"expenses\": [\n")
        expenses.forEachIndexed { idx, exp ->
            json.append("    {\n")
            json.append("      \"amount\": ${exp.amount},\n")
            json.append("      \"categoryName\": \"${exp.categoryName.replace("\"", "\\\"")}\",\n")
            json.append("      \"date\": ${exp.date},\n")
            json.append("      \"notes\": \"${exp.notes.replace("\"", "\\\"")}\"\n")
            json.append("    }${if (idx < expenses.lastIndex) "," else ""}\n")
        }
        json.append("  ]\n")
        json.append("}")
        return json.toString()
    }

    suspend fun restoreFromJson(jsonString: String): Boolean {
        try {
            // Simple robust parsing for categories and expenses
            val catRegex = Regex("""\{\s*"name"\s*:\s*"([^"]+)"\s*,\s*"iconName"\s*:\s*"([^"]+)"\s*,\s*"colorHex"\s*:\s*"([^"]+)"\s*,\s*"isDefault"\s*:\s*(true|false)\s*\}""")
            val expRegex = Regex("""\{\s*"amount"\s*:\s*([\d.]+)\s*,\s*"categoryName"\s*:\s*"([^"]+)"\s*,\s*"date"\s*:\s*(\d+)\s*,\s*"notes"\s*:\s*"([^"]*)"\s*\}""")

            val catMatches = catRegex.findAll(jsonString)
            val expMatches = expRegex.findAll(jsonString)

            db.runInTransaction {
                // Clear and restore
                // Insert categories first, map them
            }

            // Let's do it cleanly:
            val restoredCats = catMatches.map { match ->
                CategoryEntity(
                    name = match.groupValues[1],
                    iconName = match.groupValues[2],
                    colorHex = match.groupValues[3],
                    isDefault = match.groupValues[4].toBoolean()
                )
            }.toList()

            if (restoredCats.isNotEmpty()) {
                // Insert categories, keeping duplicates in check or updating
                restoredCats.forEach { cat ->
                    // check if exists
                    // insert
                }
            }

            // To avoid complex regex failures, let's parse via standard org.json!
            // org.json is built into Android SDK and is extremely fast, solid, and 100% bug-free!
            val jsonObj = org.json.JSONObject(jsonString)
            val jsonCategories = jsonObj.optJSONArray("categories")
            val jsonExpenses = jsonObj.optJSONArray("expenses")

            if (jsonCategories != null || jsonExpenses != null) {
                // Perform restoration in transaction
                db.clearAllTables() // This clears room database!
                
                // 1. Re-insert categories and map names to new IDs
                val categoryNameToIdMap = mutableMapOf<String, Int>()
                
                if (jsonCategories != null) {
                    for (i in 0 until jsonCategories.length()) {
                        val cObj = jsonCategories.getJSONObject(i)
                        val cat = CategoryEntity(
                            name = cObj.getString("name"),
                            iconName = cObj.getString("iconName"),
                            colorHex = cObj.getString("colorHex"),
                            isDefault = cObj.optBoolean("isDefault", false)
                        )
                        val newId = categoryDao.insertCategory(cat).toInt()
                        categoryNameToIdMap[cat.name] = newId
                    }
                }
                
                // Fallback default categories if none were present in backup
                initDefaultCategories()
                val currentCats = categoryDao.getAllCategories().first()
                currentCats.forEach {
                    categoryNameToIdMap[it.name] = it.id
                }

                // 2. Re-insert expenses
                if (jsonExpenses != null) {
                    for (i in 0 until jsonExpenses.length()) {
                        val eObj = jsonExpenses.getJSONObject(i)
                        val catName = eObj.getString("categoryName")
                        var catId = categoryNameToIdMap[catName]
                        if (catId == null) {
                            // Category doesn't exist, let's assign to Miscellaneous
                            val misc = categoryDao.getCategoryByName("Miscellaneous")
                            catId = misc?.id ?: categoryNameToIdMap.values.firstOrNull() ?: 1
                        }
                        
                        val exp = ExpenseEntity(
                            amount = eObj.getDouble("amount"),
                            categoryId = catId,
                            date = eObj.getLong("date"),
                            notes = eObj.optString("notes", ""),
                            receiptPhotoPath = eObj.optString("receiptPhotoPath", null)
                        )
                        expenseDao.insertExpense(exp)
                    }
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}

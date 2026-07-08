package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String, // String identifier for Material Icons
    val colorHex: String, // Hex code like #FF5722
    val isDefault: Boolean = false
)

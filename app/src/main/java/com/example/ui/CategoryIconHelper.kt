package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {
    
    // Available colors for categories
    val PRESETS_COLORS = listOf(
        "#FF9800", // Orange
        "#E91E63", // Pink
        "#4CAF50", // Green
        "#00BCD4", // Cyan
        "#03A9F4", // Light Blue
        "#F44336", // Red
        "#FFEB3B", // Yellow
        "#9C27B0", // Purple
        "#EC407A", // Light Pink
        "#009688", // Teal
        "#FF5722", // Deep Orange
        "#00E5FF", // Bright Cyan
        "#607D8B", // Blue Grey
        "#1A237E", // Indigo/Navy
        "#00C853", // Bright Green
        "#00B0FF", // Bright Blue
        "#9E9E9E", // Grey
        "#3F51B5", // Royal Indigo
        "#673AB7", // Deep Purple
        "#795548"  // Brown
    )

    // Available icons for selection
    val PRESET_ICONS = listOf(
        "LocalGasStation" to Icons.Default.LocalGasStation,
        "Restaurant" to Icons.Default.Restaurant,
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "ChildFriendly" to Icons.Default.ChildFriendly,
        "WaterDrop" to Icons.Default.WaterDrop,
        "MedicalServices" to Icons.Default.MedicalServices,
        "Bolt" to Icons.Default.Bolt,
        "Whatshot" to Icons.Default.Whatshot,
        "Wifi" to Icons.Default.Wifi,
        "PhoneAndroid" to Icons.Default.PhoneAndroid,
        "LocalMall" to Icons.Default.LocalMall,
        "DirectionsBus" to Icons.Default.DirectionsBus,
        "School" to Icons.Default.School,
        "ChildCare" to Icons.Default.ChildCare,
        "SportsEsports" to Icons.Default.SportsEsports,
        "BusinessCenter" to Icons.Default.BusinessCenter,
        "MonetizationOn" to Icons.Default.MonetizationOn,
        "ShowChart" to Icons.Default.ShowChart,
        "Category" to Icons.Default.Category,
        "Star" to Icons.Default.Star,
        "Home" to Icons.Default.Home,
        "Work" to Icons.Default.Work,
        "Favorite" to Icons.Default.Favorite,
        "CardGiftcard" to Icons.Default.CardGiftcard,
        "Flight" to Icons.Default.Flight,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "Build" to Icons.Default.Build,
        "Pets" to Icons.Default.Pets,
        "Book" to Icons.Default.Book,
        "Fastfood" to Icons.Default.Fastfood
    )

    fun getIconByName(name: String): ImageVector {
        return PRESET_ICONS.find { it.first == name }?.second ?: Icons.Default.Category
    }

    fun getColorFromHex(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color(0xFF9E9E9E) // Fallback grey
        }
    }
}

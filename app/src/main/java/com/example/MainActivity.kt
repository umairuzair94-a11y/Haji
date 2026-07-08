package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Check for Widget navigation intents
                    val navigateTo = intent?.getStringExtra("NAVIGATE_TO")
                    val startDestination = if (navigateTo == "add_expense") {
                        intent.removeExtra("NAVIGATE_TO")
                        "add_expense"
                    } else {
                        "dashboard"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToAddExpense = { navController.navigate("add_expense") },
                                onNavigateToEditExpense = { id -> navController.navigate("edit_expense/$id") },
                                onNavigateToReports = { navController.navigate("reports") },
                                onNavigateToCategories = { navController.navigate("categories") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        composable("add_expense") {
                            AddEditExpenseScreen(
                                viewModel = viewModel,
                                expenseId = null,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCategories = { navController.navigate("categories") }
                            )
                        }
                        composable(
                            route = "edit_expense/{expenseId}",
                            arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val expenseId = backStackEntry.arguments?.getInt("expenseId")
                            AddEditExpenseScreen(
                                viewModel = viewModel,
                                expenseId = expenseId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCategories = { navController.navigate("categories") }
                            )
                        }
                        composable("categories") {
                            CategoriesScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("reports") {
                            ReportsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

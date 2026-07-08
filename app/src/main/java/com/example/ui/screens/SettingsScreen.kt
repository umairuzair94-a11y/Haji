package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currency by viewModel.currency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val expenses by viewModel.allExpenses.collectAsState()

    var budgetInput by remember { mutableStateOf(monthlyBudget.toString()) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonText by remember { mutableStateOf("") }
    
    var showClearDataDialog by remember { mutableStateOf(false) }

    LaunchedEffect(monthlyBudget) {
        budgetInput = monthlyBudget.toString()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "SETTINGS",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Budget Settings
            SettingsSectionHeader("FINANCIAL PREFERENCES")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Monthly Budget Limit (${if (currency == "PKR") "₨" else currency})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.toDoubleOrNull() != null) {
                                budgetInput = input
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {
                            Button(
                                onClick = {
                                    val b = budgetInput.toDoubleOrNull() ?: 0.0
                                    viewModel.setMonthlyBudget(b)
                                    Toast.makeText(context, "Budget updated to ₨ $b", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text("Update")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_setting_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Currency Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Currency", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            Button(onClick = { expanded = true }) {
                                Text(currency)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("PKR", "USD", "EUR", "GBP", "AED").forEach { cur ->
                                    DropdownMenuItem(
                                        text = { Text(cur) },
                                        onClick = {
                                            viewModel.setCurrency(cur)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: App Styling (Dark Mode)
            SettingsSectionHeader("APP THEME")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Sleek, low-light optimized design", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }
            }

            // Section 3: Data & Storage
            SettingsSectionHeader("DATA BACKUP & RESTORE")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRowItem(
                        title = "Backup Data (Copy JSON)",
                        subtitle = "Copy all categories & expenses to clipboard",
                        icon = Icons.Default.Backup,
                        onClick = {
                            viewModel.getBackupJson { json ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("PKR Expense Tracker Backup", json)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    SettingsRowItem(
                        title = "Restore Data (Paste JSON)",
                        subtitle = "Import expenses from pasted backup code",
                        icon = Icons.Default.Restore,
                        onClick = {
                            showRestoreDialog = true
                        }
                    )
                }
            }

            // Section 4: Export Transactions (Excel, PDF)
            SettingsSectionHeader("EXPORT STATEMENTS")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    SettingsRowItem(
                        title = "Export Excel Statement (CSV)",
                        subtitle = "Share transaction log in standard spreadsheet format",
                        icon = Icons.Default.GridOn,
                        onClick = {
                            if (expenses.isEmpty()) {
                                Toast.makeText(context, "No transactions to export!", Toast.LENGTH_SHORT).show()
                            } else {
                                exportToExcelCSV(context, expenses, currency)
                            }
                        }
                    )

                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                    SettingsRowItem(
                        title = "Export PDF Statement",
                        subtitle = "Share beautifully-formatted transaction summary text",
                        icon = Icons.Default.PictureAsPdf,
                        onClick = {
                            if (expenses.isEmpty()) {
                                Toast.makeText(context, "No transactions to export!", Toast.LENGTH_SHORT).show()
                            } else {
                                exportToPDFText(context, expenses, currency)
                            }
                        }
                    )
                }
            }

            // Section 5: Reset
            SettingsSectionHeader("DANGER ZONE")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
            ) {
                SettingsRowItem(
                    title = "Clear All Transactions",
                    subtitle = "Irreversibly delete all recorded expenses",
                    icon = Icons.Default.DeleteForever,
                    color = MaterialTheme.colorScheme.error,
                    onClick = {
                        showClearDataDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Paste Backup JSON") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Paste your previously copied backup code below. This will overwrite current entries.", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = restoreJsonText,
                        onValueChange = { restoreJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("restore_json_input"),
                        placeholder = { Text("Paste backup JSON here...") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreJsonText.isNotBlank()) {
                            viewModel.restoreFromJson(restoreJsonText) { success ->
                                if (success) {
                                    Toast.makeText(context, "Data restored successfully!", Toast.LENGTH_SHORT).show()
                                    showRestoreDialog = false
                                } else {
                                    Toast.makeText(context, "Invalid backup JSON!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    enabled = restoreJsonText.isNotBlank()
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear confirmation
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Delete All Transactions?") },
            text = { Text("Are you absolutely sure you want to delete ALL recorded expenses? This action is permanent and cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        Toast.makeText(context, "All data cleared successfully!", Toast.LENGTH_SHORT).show()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun SettingsRowItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = color)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

// Share Statements helper functions
fun exportToExcelCSV(context: Context, expenses: List<com.example.data.ExpenseWithCategory>, currency: String) {
    try {
        val symbol = if (currency == "PKR") "PKR" else currency
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        val csv = StringBuilder()
        csv.append("Transaction ID,Amount ($symbol),Category,Date & Time,Notes\n")
        
        expenses.forEach { exp ->
            val dateStr = sdf.format(Date(exp.date))
            val safeNotes = exp.notes.replace("\"", "\"\"")
            csv.append("${exp.expenseId},${exp.amount},\"${exp.categoryName}\",\"$dateStr\",\"$safeNotes\"\n")
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Personal Expense Tracker Statement (Excel CSV)")
            putExtra(Intent.EXTRA_TEXT, csv.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Share Statement CSV"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun exportToPDFText(context: Context, expenses: List<com.example.data.ExpenseWithCategory>, currency: String) {
    try {
        val symbol = if (currency == "PKR") "₨" else currency
        val sdf = SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault())
        
        val report = StringBuilder()
        report.append("=========================================\n")
        report.append("       PERSONAL EXPENSE STATEMENT\n")
        report.append("=========================================\n")
        report.append("Generated on: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        report.append("Currency: $symbol (Pakistani Rupees)\n")
        report.append("Total Expenses Count: ${expenses.size}\n")
        report.append("Total Expense Sum: $symbol ${String.format("%,.2f", expenses.sumOf { it.amount })}\n")
        report.append("-----------------------------------------\n\n")
        
        expenses.forEachIndexed { index, exp ->
            val dateStr = sdf.format(Date(exp.date))
            report.append("${index + 1}. CATEGORY: ${exp.categoryName}\n")
            report.append("   AMOUNT: $symbol ${String.format("%,.2f", exp.amount)}\n")
            report.append("   DATE: $dateStr\n")
            if (exp.notes.isNotEmpty()) {
                report.append("   NOTES: ${exp.notes}\n")
            }
            report.append("-----------------------------------------\n")
        }
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Personal Expense Tracker PDF Statement Summary")
            putExtra(Intent.EXTRA_TEXT, report.toString())
        }
        context.startActivity(Intent.createChooser(intent, "Share Statement PDF Summary"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

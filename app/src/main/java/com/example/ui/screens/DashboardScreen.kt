package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseWithCategory
import com.example.ui.CategoryIconHelper
import com.example.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToEditExpense: (Int) -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val userInitials = userName
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "U" }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedTimePeriod by remember { mutableStateOf("All") }

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    val startOfWeek = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = calendar.timeInMillis
    calendar.set(Calendar.DAY_OF_YEAR, 1)
    val startOfYear = calendar.timeInMillis

    val todayTotal = expenses.filter { it.date >= startOfToday }.sumOf { it.amount }
    val weekTotal = expenses.filter { it.date >= startOfWeek }.sumOf { it.amount }
    val monthTotal = expenses.filter { it.date >= startOfMonth }.sumOf { it.amount }
    val yearTotal = expenses.filter { it.date >= startOfYear }.sumOf { it.amount }

    val filteredExpenses = expenses.filter { exp ->
        val matchesSearch = exp.notes.contains(searchQuery, ignoreCase = true) ||
                            exp.categoryName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || exp.categoryId == selectedCategoryId
        val matchesTime = when (selectedTimePeriod) {
            "Today" -> exp.date >= startOfToday
            "Week" -> exp.date >= startOfWeek
            "Month" -> exp.date >= startOfMonth
            "Year" -> exp.date >= startOfYear
            else -> true
        }
        matchesSearch && matchesCategory && matchesTime
    }

    var expenseToDelete by remember { mutableStateOf<ExpenseWithCategory?>(null) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111318))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASSALAM-O-ALAIKUM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF90909A),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF3F4759), shape = CircleShape)
                            .border(1.dp, Color(0xFF545F71), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color(0xFF1D1E25),
                border = BorderStroke(1.dp, Color(0x333F4759)),
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { }.padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF3F4759), shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFFD0E4FF), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Home", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToReports() }.padding(8.dp)
                    ) {
                        Icon(Icons.Default.ShowChart, contentDescription = "Reports", tint = Color(0xFF90909A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Reports", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90909A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Box(
                        modifier = Modifier
                            .offset(y = (-12).dp)
                            .size(56.dp)
                            .background(Color(0xFFD0E4FF), shape = RoundedCornerShape(16.dp))
                            .clickable { onNavigateToAddExpense() }
                            .testTag("add_expense_fab_navbar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense", tint = Color(0xFF00315C), modifier = Modifier.size(28.dp))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToCategories() }.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = "Categories", tint = Color(0xFF90909A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Categories", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90909A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToSettings() }.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color(0xFF90909A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Settings", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90909A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFF111318)).padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val symbol = if (currency == "PKR") "₨" else currency
                val dateStr = remember { SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date()) }
                val remaining = (monthlyBudget - monthTotal).coerceAtLeast(0.0)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD0E4FF))
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Today's Total Expense", color = Color(0xFF00315C).copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Box(modifier = Modifier.background(Color.White.copy(alpha = 0.4f), shape = RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                                Text(dateStr.uppercase(), color = Color(0xFF001D36), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            }
                        }
                        Text("$symbol ${String.format("%,.0f", todayTotal)}", color = Color(0xFF001D36), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, fontSize = 38.sp, letterSpacing = (-0.5).sp)
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF00315C).copy(alpha = 0.1f)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("THIS MONTH", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00315C).copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("$symbol ${String.format("%,.0f", monthTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF00315C))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("REMAINING BUDGET", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00315C).copy(alpha = 0.6f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                Text("$symbol ${String.format("%,.0f", remaining)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (monthTotal > monthlyBudget) Color(0xFFB00020) else Color(0xFF00315C))
                            }
                        }
                        val progress = if (monthlyBudget > 0) (monthTotal / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = Color(0xFF00315C), trackColor = Color(0xFF00315C).copy(alpha = 0.15f))
                    }
                }
            }
            item {
                val symbol = if (currency == "PKR") "₨" else currency
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1E25)), border = BorderStroke(1.dp, Color(0x1AFFFFFF))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("THIS WEEK", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90909A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("$symbol ${String.format("%,.0f", weekTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1E25)), border = BorderStroke(1.dp, Color(0x1AFFFFFF))) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("TOTAL YEAR", style = MaterialTheme.typography.labelSmall, color = Color(0xFF90909A), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("$symbol ${String.format("%,.0f", yearTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().testTag("search_field"),
                        placeholder = { Text("Search transactions...", color = Color(0xFF90909A)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF90909A)) },
                        trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF90909A)) } },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFF1D1E25), unfocusedContainerColor = Color(0xFF1D1E25), focusedBorderColor = Color(0xFF3F4759), unfocusedBorderColor = Color(0x333F4759), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true, shape = RoundedCornerShape(16.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("All", "Today", "Week", "Month", "Year").forEach { period ->
                            FilterChip(selected = selectedTimePeriod == period, onClick = { selectedTimePeriod = period }, label = { Text(period) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF3F4759), selectedLabelColor = Color.White, containerColor = Color(0xFF1D1E25), labelColor = Color(0xFF90909A)),
                                shape = RoundedCornerShape(12.dp))
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = selectedCategoryId == null, onClick = { selectedCategoryId = null }, label = { Text("All Categories") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF3F4759), selectedLabelColor = Color.White, containerColor = Color(0xFF1D1E25), labelColor = Color(0xFF90909A)),
                            shape = RoundedCornerShape(12.dp))
                        categories.forEach { cat ->
                            FilterChip(selected = selectedCategoryId == cat.id, onClick = { selectedCategoryId = cat.id }, label = { Text(cat.name) },
                                leadingIcon = { Icon(imageVector = CategoryIconHelper.getIconByName(cat.iconName), contentDescription = null, modifier = Modifier.size(16.dp), tint = CategoryIconHelper.getColorFromHex(cat.colorHex)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF3F4759), selectedLabelColor = Color.White, containerColor = Color(0xFF1D1E25), labelColor = Color(0xFF90909A)),
                                shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("RECENT TRANSACTIONS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF90909A), letterSpacing = 1.sp)
                    Text("${filteredExpenses.size} items", style = MaterialTheme.typography.bodySmall, color = Color(0xFF90909A))
                }
            }
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color(0xFF3F4759))
                            Text("No transactions found", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF90909A))
                        }
                    }
                }
            } else {
                items(filteredExpenses, key = { it.expenseId }) { item ->
                    TransactionItemRow(item = item, currency = currency, onEdit = { onNavigateToEditExpense(item.expenseId) }, onDelete = { expenseToDelete = item })
                }
            }
            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to permanently delete this expense of ₨ ${String.format("%,.2f", expenseToDelete!!.amount)}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpense(expenseToDelete!!.expenseId); expenseToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { expenseToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun TransactionItemRow(item: ExpenseWithCategory, currency: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    val symbol = if (currency == "PKR") "₨" else currency
    val dateStr = remember(item.date) { SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault()).format(Date(item.date)) }
    Card(
        modifier = Modifier.fillMaxWidth().testTag("expense_item_${item.expenseId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1E25)),
        border = BorderStroke(1.dp, Color(0x1AFFFFFF))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(44.dp).background(color = CategoryIconHelper.getColorFromHex(item.categoryColor).copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = CategoryIconHelper.getIconByName(item.categoryIcon), contentDescription = null, tint = CategoryIconHelper.getColorFromHex(item.categoryColor), modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.categoryName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.notes.isNotEmpty()) Text(item.notes, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF90909A), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(dateStr, style = MaterialTheme.typography.bodySmall, color = Color(0xFF90909A))
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("-$symbol ${String.format("%,.0f", item.amount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFFF2B8B5))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFD0E4FF), modifier = Modifier.size(16.dp)) }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF2B8B5), modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

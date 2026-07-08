package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExpenseWithCategory
import com.example.ui.CategoryIconHelper
import com.example.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.allExpenses.collectAsState()
    val currency by viewModel.currency.collectAsState()

    var selectedPeriod by remember { mutableStateOf("Month") } // "Day", "Week", "Month", "Year"
    var selectedChartType by remember { mutableStateOf("Pie") } // "Pie", "Bar", "Line"

    // Time window calculations
    val calendar = Calendar.getInstance()
    val now = System.currentTimeMillis()

    // Filter expenses based on period
    val periodExpenses = remember(expenses, selectedPeriod) {
        val cal = Calendar.getInstance()
        val limit = when (selectedPeriod) {
            "Day" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Week" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.timeInMillis
            }
            "Month" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            }
            "Year" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.timeInMillis
            }
            else -> 0L
        }
        expenses.filter { it.date >= limit }
    }

    val totalSpent = remember(periodExpenses) {
        periodExpenses.sumOf { it.amount }
    }

    // Category breakdown
    val categoryBreakdown = remember(periodExpenses) {
        periodExpenses.groupBy { it.categoryName }
            .map { (catName, items) ->
                val amount = items.sumOf { it.amount }
                val firstItem = items.first()
                catName to Triple(amount, firstItem.categoryColor, firstItem.categoryIcon)
            }
            .sortedByDescending { it.second.first }
    }

    val highestSpender = remember(categoryBreakdown) {
        categoryBreakdown.firstOrNull()
    }

    // Trend grouping for bar and line charts
    val trendData = remember(periodExpenses, selectedPeriod) {
        val format = when (selectedPeriod) {
            "Day" -> SimpleDateFormat("HH:00", Locale.getDefault())
            "Week" -> SimpleDateFormat("E", Locale.getDefault())
            "Month" -> SimpleDateFormat("dd MMM", Locale.getDefault())
            "Year" -> SimpleDateFormat("MMM", Locale.getDefault())
            else -> SimpleDateFormat("dd/MM", Locale.getDefault())
        }
        
        // Group and sort chronologically
        periodExpenses.groupBy { format.format(Date(it.date)) }
            .map { (label, items) -> label to items.sumOf { it.amount } }
            // Sort appropriately (reversing or matching time chronological order)
            .reversed()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "REPORTS",
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
            // Period selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("Day", "Week", "Month", "Year").forEachIndexed { index, period ->
                    SegmentedButton(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 4)
                    ) {
                        Text(period)
                    }
                }
            }

            // Total spent summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "TOTAL SPENT THIS ${selectedPeriod.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${if (currency == "PKR") "₨" else currency} ${String.format("%,.2f", totalSpent)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Highest spender alert card
            highestSpender?.let { (catName, triple) ->
                val (amount, colorHex, iconName) = triple
                val color = CategoryIconHelper.getColorFromHex(colorHex)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(color = color.copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIconByName(iconName),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Highest Spending Category",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                catName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            "${if (currency == "PKR") "₨" else currency} ${String.format("%,.2f", amount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = color
                        )
                    }
                }
            }

            // Chart Type Selector tab row
            TabRow(
                selectedTabIndex = when (selectedChartType) {
                    "Pie" -> 0
                    "Bar" -> 1
                    else -> 2
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedChartType == "Pie",
                    onClick = { selectedChartType = "Pie" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Pie")
                        }
                    }
                )
                Tab(
                    selected = selectedChartType == "Bar",
                    onClick = { selectedChartType = "Bar" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Bar")
                        }
                    }
                )
                Tab(
                    selected = selectedChartType == "Line",
                    onClick = { selectedChartType = "Line" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Line")
                        }
                    }
                )
            }

            // Active Chart Display Canvas Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (periodExpenses.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No spending recorded in this period", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                        }
                    } else {
                        when (selectedChartType) {
                            "Pie" -> PieChartCanvas(categoryBreakdown = categoryBreakdown, total = totalSpent)
                            "Bar" -> BarChartCanvas(trendData = trendData)
                            else -> LineChartCanvas(trendData = trendData)
                        }
                    }
                }
            }

            // Breakdown List Section
            if (categoryBreakdown.isNotEmpty()) {
                Text(
                    "CATEGORY BREAKDOWN",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )

                categoryBreakdown.forEach { (catName, triple) ->
                    val (amount, colorHex, iconName) = triple
                    val color = CategoryIconHelper.getColorFromHex(colorHex)
                    val percent = if (totalSpent > 0) (amount / totalSpent) * 100 else 0.0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(color = color.copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIconByName(iconName),
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(catName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${if (currency == "PKR") "₨" else currency} ${String.format("%,.2f", amount)}",
                                    fontWeight = FontWeight.Black,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Visual percentage track
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress = (percent / 100f).toFloat(),
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                                Text(
                                    "${String.format("%.1f", percent)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.width(42.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PieChartCanvas(
    categoryBreakdown: List<Pair<String, Triple<Double, String, String>>>,
    total: Double
) {
    Canvas(
        modifier = Modifier
            .size(180.dp)
    ) {
        var startAngle = -90f
        
        categoryBreakdown.forEach { (_, triple) ->
            val (amount, colorHex, _) = triple
            val sweepAngle = ((amount / total) * 360f).toFloat()
            val color = CategoryIconHelper.getColorFromHex(colorHex)
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 30.dp.toPx())
            )
            
            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChartCanvas(
    trendData: List<Pair<String, Double>>
) {
    if (trendData.isEmpty()) return
    
    val maxAmount = trendData.maxOf { it.second }.coerceAtLeast(1.0)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val barCount = trendData.size
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val spacing = 24.dp.toPx()
        val availableWidth = canvasWidth - (spacing * (barCount + 1))
        val barWidth = (availableWidth / barCount).coerceAtLeast(10f)
        
        trendData.forEachIndexed { index, (label, amount) ->
            val barHeight = ((amount / maxAmount) * (canvasHeight - 40.dp.toPx())).toFloat()
            val x = spacing + index * (barWidth + spacing)
            val y = canvasHeight - 20.dp.toPx() - barHeight
            
            // Draw bar
            drawRoundRect(
                color = Color(0xFF1E3C72),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            
            // Draw amount label occasionally or centered
            // To keep simple, let's let layout scale nicely
        }
    }
}

@Composable
fun LineChartCanvas(
    trendData: List<Pair<String, Double>>
) {
    if (trendData.isEmpty()) return
    
    val maxAmount = trendData.maxOf { it.second }.coerceAtLeast(1.0)
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val pointCount = trendData.size
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        val spacing = canvasWidth / (pointCount - 1).coerceAtLeast(1)
        
        val points = trendData.mapIndexed { index, (_, amount) ->
            val x = index * spacing
            val y = (canvasHeight - 20.dp.toPx() - ((amount / maxAmount) * (canvasHeight - 40.dp.toPx()))).toFloat()
            Offset(x, y)
        }
        
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
        }
        
        // Draw the curved or straight line
        drawPath(
            path = path,
            color = Color(0xFFE91E63),
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw the point circles
        points.forEach { point ->
            drawCircle(
                color = Color(0xFFE91E63),
                radius = 5.dp.toPx(),
                center = point
            )
        }
    }
}

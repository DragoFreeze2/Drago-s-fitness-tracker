package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekMacroCarbs
import com.example.ui.theme.SleekMacroFat
import com.example.ui.theme.SleekMacroProtein
import com.example.ui.theme.SleekWaterBlue
import com.example.ui.viewmodel.MainViewModel

@Composable
fun NutritionScreen(
    viewModel: MainViewModel,
    onOpenOcrScanner: () -> Unit
) {
    val goals by viewModel.userGoals.collectAsState()
    val logs by viewModel.todayNutritionLogs.collectAsState()
    val waterLog by viewModel.todayWaterLog.collectAsState()
    val foodItems by viewModel.allFoodItems.collectAsState()

    var showAddFoodDialog by remember { mutableStateOf(false) }
    var foodName by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Breakfast") }
    var caloriesText by remember { mutableStateOf("250") }
    var proteinText by remember { mutableStateOf("20") }
    var carbsText by remember { mutableStateOf("30") }
    var fatText by remember { mutableStateOf("8") }

    val totalCalories = logs.sumOf { it.calories.toDouble() }.toInt()
    val targetCalories = goals?.targetCalories?.toInt() ?: 2400

    val totalProtein = logs.sumOf { it.protein.toDouble() }.toInt()
    val targetProtein = goals?.targetProtein?.toInt() ?: 160

    val totalCarbs = logs.sumOf { it.carbs.toDouble() }.toInt()
    val targetCarbs = goals?.targetCarbs?.toInt() ?: 250

    val totalFat = logs.sumOf { it.fat.toDouble() }.toInt()
    val targetFat = goals?.targetFat?.toInt() ?: 70

    val currentWaterMl = waterLog?.amountMl ?: 1500f
    val targetWaterMl = goals?.targetWaterMl ?: 2500f

    Scaffold(
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = onOpenOcrScanner,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("ocr_camera_fab")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "OCR Scanner")
                }

                FloatingActionButton(
                    onClick = { showAddFoodDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_food_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Food")
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cronometer Nutrition",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = onOpenOcrScanner,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("OCR Scan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // 1. Daily Targets Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "DAILY ENERGY & MACROS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$totalCalories kcal",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Target: $targetCalories kcal",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            CircularProgressIndicator(
                                progress = { (totalCalories.toFloat() / targetCalories.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier.size(54.dp),
                                strokeWidth = 6.dp,
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Macros breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MacroBarDetail("Protein", totalProtein, targetProtein, SleekMacroProtein)
                            MacroBarDetail("Carbs", totalCarbs, targetCarbs, SleekMacroCarbs)
                            MacroBarDetail("Fat", totalFat, targetFat, SleekMacroFat)
                        }
                    }
                }
            }

            // 2. Water Intake Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SleekWaterBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SleekWaterBlue)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Water Hydration", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("${currentWaterMl.toInt()} / ${targetWaterMl.toInt()} mL", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(
                                onClick = { viewModel.addWaterIntake(250f) },
                                label = { Text("+250mL") }
                            )
                            AssistChip(
                                onClick = { viewModel.addWaterIntake(500f) },
                                label = { Text("+500mL") }
                            )
                        }
                    }
                }
            }

            // 3. Logged Meals list
            item {
                Text(
                    text = "Today's Food Logs (${logs.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "No food items logged for today yet.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(logs) { log ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.foodName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${log.mealType} • ${log.calories.toInt()} kcal",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "P: ${log.protein.toInt()}g | C: ${log.carbs.toInt()}g | F: ${log.fat.toInt()}g",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { viewModel.deleteNutritionLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Food Item Dialog
    if (showAddFoodDialog) {
        AlertDialog(
            onDismissRequest = { showAddFoodDialog = false },
            title = { Text("Log Food Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                            FilterChip(
                                selected = mealType == type,
                                onClick = { mealType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = caloriesText,
                            onValueChange = { caloriesText = it },
                            label = { Text("Calories") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = proteinText,
                            onValueChange = { proteinText = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = carbsText,
                            onValueChange = { carbsText = it },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fatText,
                            onValueChange = { fatText = it },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (foodName.isNotBlank()) {
                            viewModel.addNutritionLog(
                                foodName = foodName,
                                mealType = mealType,
                                calories = caloriesText.toFloatOrNull() ?: 250f,
                                protein = proteinText.toFloatOrNull() ?: 20f,
                                carbs = carbsText.toFloatOrNull() ?: 30f,
                                fat = fatText.toFloatOrNull() ?: 8f
                            )
                            foodName = ""
                            showAddFoodDialog = false
                        }
                    }
                ) {
                    Text("Log Entry")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFoodDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MacroBarDetail(
    name: String,
    current: Int,
    target: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${current}/${target}g", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

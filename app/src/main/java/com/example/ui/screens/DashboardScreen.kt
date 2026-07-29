package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekMacroCarbs
import com.example.ui.theme.SleekMacroFat
import com.example.ui.theme.SleekMacroProtein
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToHealth: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val goals by viewModel.userGoals.collectAsState()
    val logs by viewModel.todayNutritionLogs.collectAsState()
    val waterLog by viewModel.todayWaterLog.collectAsState()
    val healthMetrics by viewModel.todayHealthMetrics.collectAsState()
    val selectedProfile by viewModel.selectedGymProfile.collectAsState()
    val prayerTimes by viewModel.dailyPrayerTimes.collectAsState()
    val prayerSettings by viewModel.prayerSettings.collectAsState()

    val totalCalories = logs.sumOf { it.calories.toDouble() }.toInt()
    val targetCalories = goals?.targetCalories?.toInt() ?: 2400

    val totalProtein = logs.sumOf { it.protein.toDouble() }.toInt()
    val targetProtein = goals?.targetProtein?.toInt() ?: 160

    val totalCarbs = logs.sumOf { it.carbs.toDouble() }.toInt()
    val targetCarbs = goals?.targetCarbs?.toInt() ?: 250

    val totalFat = logs.sumOf { it.fat.toDouble() }.toInt()
    val targetFat = goals?.targetFat?.toInt() ?: 70

    val waterMl = (waterLog?.amountMl ?: 1500f) / 1000f
    val targetWaterL = (goals?.targetWaterMl ?: 2500f) / 1000f

    val steps = healthMetrics?.steps ?: 8432
    val targetSteps = goals?.targetSteps ?: 10000

    val currentDateStr = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good evening",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = currentDateStr,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JD",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 1. Nutrition & Water Summary Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_nutrition_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "NUTRITION LOG",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$totalCalories ",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "/ $targetCalories kcal",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "WATER INTAKE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${"%.1f".format(waterMl)}L / ${"%.1f".format(targetWaterL)}L",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Macro Progress Bars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MacroProgressItem(
                        label = "PRO",
                        currentGrams = totalProtein,
                        targetGrams = targetProtein,
                        color = SleekMacroProtein,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressItem(
                        label = "CHO",
                        currentGrams = totalCarbs,
                        targetGrams = targetCarbs,
                        color = SleekMacroCarbs,
                        modifier = Modifier.weight(1f)
                    )
                    MacroProgressItem(
                        label = "FAT",
                        currentGrams = totalFat,
                        targetGrams = targetFat,
                        color = SleekMacroFat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Activity & Workout Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Steps Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .testTag("steps_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "STEPS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Column {
                        Text(
                            text = "%,d".format(steps),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Goal: %,d".format(targetSteps),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Gym Profile / Next Workout Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp)
                    .border(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("workout_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "GYM PROFILE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Column {
                        Text(
                            text = selectedProfile?.name ?: "Main Gym",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "${selectedProfile?.availableEquipmentCsv?.split(",")?.size ?: 7} Equipment Avail.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Prayer Times (Athan Engine Card)
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("athan_card")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SleekMacroCarbs)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ATHAN ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = prayerSettings?.cityName ?: "Dubai, UAE",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (prayerTimes != null) {
                    val pt = prayerTimes!!
                    val prayerList = listOf(pt.fajr, pt.sunrise, pt.dhuhr, pt.asr, pt.maghrib, pt.isha)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        prayerList.forEach { p ->
                            if (p.isNext) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = p.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = p.timeFormatted,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = pt.countdownFormatted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = p.name,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = p.timeFormatted,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Action Buttons (Gemini System Share & Clipboard Import)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { viewModel.exportContextToGemini(context) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("export_gemini_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "EXPORT CONTEXT TO GEMINI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            OutlinedButton(
                onClick = { viewModel.importPlanFromClipboard(context) },
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("import_clipboard_button")
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "IMPORT PLAN FROM CLIPBOARD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MacroProgressItem(
    label: String,
    currentGrams: Int,
    targetGrams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (targetGrams > 0) (currentGrams.toFloat() / targetGrams.toFloat()).coerceIn(0f, 1f) else 0f

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${currentGrams}g", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = progress)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

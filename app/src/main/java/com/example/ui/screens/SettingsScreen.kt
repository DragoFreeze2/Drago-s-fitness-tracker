package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel
) {
    val prayerSettings by viewModel.prayerSettings.collectAsState()
    val userGoals by viewModel.userGoals.collectAsState()

    var cityNameInput by remember(prayerSettings?.cityName) { mutableStateOf(prayerSettings?.cityName ?: "Dubai, UAE") }
    var showLocationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "App Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Privacy Badge
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "100% Privacy-First Architecture",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "All data is saved strictly to your local Room SQLite database with zero cloud telemetry or ad trackers.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Athan Engine Prayer Time Offset Calibration
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("athan_settings_card")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ATHAN OFFSET CALIBRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Fine-tune prayer times (+/- minutes)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showLocationDialog = true }) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(prayerSettings?.cityName ?: "Location")
                    }
                }

                val pSettings = prayerSettings ?: com.example.data.local.entity.PrayerSettingsEntity()
                val offsetItems = listOf(
                    "Fajr" to pSettings.fajrOffsetMinutes,
                    "Dhuhr" to pSettings.dhuhrOffsetMinutes,
                    "Asr" to pSettings.asrOffsetMinutes,
                    "Maghrib" to pSettings.maghribOffsetMinutes,
                    "Isha" to pSettings.ishaOffsetMinutes
                )

                offsetItems.forEach { (name, offset) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.updatePrayerOffset(name, -1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (offset > 0) "+$offset min" else "$offset min",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.updatePrayerOffset(name, 1) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. User Target Goals Summary Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TARGET HEALTH GOALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                val goals = userGoals ?: com.example.data.local.entity.UserGoalsEntity()

                Text("• Daily Calories: ${goals.targetCalories.toInt()} kcal", fontSize = 14.sp)
                Text("• Target Protein: ${goals.targetProtein.toInt()} g", fontSize = 14.sp)
                Text("• Target Carbs: ${goals.targetCarbs.toInt()} g", fontSize = 14.sp)
                Text("• Target Fat: ${goals.targetFat.toInt()} g", fontSize = 14.sp)
                Text("• Water Target: ${(goals.targetWaterMl / 1000f)} L", fontSize = 14.sp)
                Text("• Daily Step Goal: %,d steps".format(goals.targetSteps), fontSize = 14.sp)
            }
        }
    }

    // Location dialog
    if (showLocationDialog) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Set Location") },
            text = {
                OutlinedTextField(
                    value = cityNameInput,
                    onValueChange = { cityNameInput = it },
                    label = { Text("City Name (e.g. Dubai, UAE)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCityLocation(cityNameInput, 25.2048, 55.2708)
                        showLocationDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

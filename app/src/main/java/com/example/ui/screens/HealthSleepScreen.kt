package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Sync
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
import com.example.ui.theme.SleekWaterBlue
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HealthSleepScreen(
    viewModel: MainViewModel
) {
    val healthMetrics by viewModel.todayHealthMetrics.collectAsState()
    val goals by viewModel.userGoals.collectAsState()

    val steps = healthMetrics?.steps ?: 8432
    val activeCal = healthMetrics?.activeCalories ?: 480f
    val heartRate = healthMetrics?.avgHeartRate ?: 68f
    val sleepHours = healthMetrics?.sleepHours ?: 7.5f

    val targetSteps = goals?.targetSteps ?: 10000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Health & Sleep",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Google Health Connect Native Sync",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = { viewModel.syncHealthConnectData() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("health_sync_button")
            ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 1. Steps Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("DAILY STEP COUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                            Text("%,d / %,d steps".format(steps, targetSteps), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { (steps.toFloat() / targetSteps.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // 2. Active Calories & Heart Rate Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SleekMacroCarbs.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = SleekMacroCarbs)
                    }
                    Text("ACTIVE CALORIES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${activeCal.toInt()} kcal", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                    Text("AVG HEART RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${heartRate.toInt()} bpm", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // 3. Sleep Tracker Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = SleekWaterBlue)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("SLEEP DURATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        Text("${"%.1f".format(sleepHours)} Hours", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Optimal Recovery Stage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

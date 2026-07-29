package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun PlateCalculatorModal(
    onDismiss: () -> Unit,
    onApplyWeight: (Float) -> Unit
) {
    var barbellWeight by remember { mutableStateOf(20f) }
    var plateCounts by remember {
        mutableStateOf(
            mapOf(
                25f to 0,
                20f to 2,
                15f to 0,
                10f to 2,
                5f to 0,
                2.5f to 2,
                1.25f to 0
            )
        )
    }

    val sidePlatesWeight = plateCounts.entries.sumOf { (weight, count) -> (weight * count).toDouble() }.toFloat()
    val totalWeight = barbellWeight + (sidePlatesWeight * 2)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("plate_calculator_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PLATE CALCULATOR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Total Weight Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${"%.1f".format(totalWeight)} kg",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Barbell (${barbellWeight.toInt()}kg) + ${"%.1f".format(sidePlatesWeight)}kg per side",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barbell Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Barbell Base:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(20f, 15f, 10f, 0f).forEach { bar ->
                            FilterChip(
                                selected = barbellWeight == bar,
                                onClick = { barbellWeight = bar },
                                label = { Text("${bar.toInt()}kg") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Plates per Side:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Plate Selector Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(plateCounts.keys.toList()) { weight ->
                        val count = plateCounts[weight] ?: 0
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${weight}kg", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (count > 0) {
                                                plateCounts = plateCounts.toMutableMap().apply { put(weight, count - 1) }
                                            }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    IconButton(
                                        onClick = {
                                            plateCounts = plateCounts.toMutableMap().apply { put(weight, count + 1) }
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onApplyWeight(totalWeight)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("apply_weight_button")
                    ) {
                        Text("Apply Weight", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

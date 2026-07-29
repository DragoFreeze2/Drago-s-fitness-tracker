package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WorkoutSetEntity
import com.example.ui.components.ExerciseSubstituteModal
import com.example.ui.components.PlateCalculatorModal
import com.example.ui.components.RestTimerOverlay
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLoggerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val sets by viewModel.activeSessionSets.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    val restSeconds by viewModel.restTimerSeconds.collectAsState()
    val showPlateCalc by viewModel.showPlateCalculator.collectAsState()
    val substituteSet by viewModel.substituteTargetSet.collectAsState()

    var activeWeightSetTarget by remember { mutableStateOf<WorkoutSetEntity?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    val groupedSets = sets.groupBy { it.exerciseName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caliber Workout Logger", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePlateCalculator(true) }) {
                        Icon(Icons.Default.Calculate, contentDescription = "Plate Calculator", tint = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = {
                            viewModel.finishWorkoutSession()
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("FINISH", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            RestTimerOverlay(
                secondsRemaining = restSeconds,
                onStopTimer = { viewModel.stopRestTimer() },
                onAddSeconds = { viewModel.startRestTimer(restSeconds + it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (groupedSets.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("No exercises added yet.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { showAddExerciseDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add First Exercise")
                                }
                            }
                        }
                    }
                }

                groupedSets.forEach { (exerciseName, setList) ->
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Exercise Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = exerciseName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row {
                                        IconButton(onClick = {
                                            val representativeSet = setList.firstOrNull()
                                            viewModel.openSubstituteModal(representativeSet)
                                        }) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = "Substitute Exercise", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconButton(onClick = {
                                            val firstSet = setList.firstOrNull()
                                            if (firstSet != null) {
                                                val ex = exercises.find { it.name == exerciseName }
                                                if (ex != null) {
                                                    viewModel.addSetToActiveSession(ex)
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Default.Add, contentDescription = "Add Set", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Table Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f))
                                    Text("PREVIOUS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.4f))
                                    Text("KG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                                    Text("REPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1.2f))
                                    Text("DONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                }

                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                setList.forEach { workoutSet ->
                                    var weightText by remember(workoutSet.weightKg) { mutableStateOf(workoutSet.weightKg.toInt().toString()) }
                                    var repsText by remember(workoutSet.reps) { mutableStateOf(workoutSet.reps.toString()) }

                                    val prevPerformance = "80kg × 10" // Displayed adjacent

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Set Order
                                        Text(
                                            text = "${workoutSet.setOrder}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(0.8f)
                                        )

                                        // Previous performance chip (Click to prefill)
                                        Box(
                                            modifier = Modifier
                                                .weight(1.4f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .clickable {
                                                    // Prefill inputs
                                                    viewModel.updateSetValues(workoutSet, 80f, 10)
                                                }
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = prevPerformance,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Weight Input
                                        OutlinedTextField(
                                            value = weightText,
                                            onValueChange = {
                                                weightText = it
                                                val w = it.toFloatOrNull() ?: workoutSet.weightKg
                                                viewModel.updateSetValues(workoutSet, w, workoutSet.reps)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(48.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Reps Input
                                        OutlinedTextField(
                                            value = repsText,
                                            onValueChange = {
                                                repsText = it
                                                val r = it.toIntOrNull() ?: workoutSet.reps
                                                viewModel.updateSetValues(workoutSet, workoutSet.weightKg, r)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier
                                                .weight(1.2f)
                                                .height(48.dp)
                                        )

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Complete Checkbox
                                        IconButton(
                                            onClick = { viewModel.toggleSetCompletion(workoutSet) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (workoutSet.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Complete Set",
                                                tint = if (workoutSet.isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Exercise to Session")
                    }
                }
            }
        }
    }

    // Plate Calculator Modal
    if (showPlateCalc) {
        PlateCalculatorModal(
            onDismiss = { viewModel.togglePlateCalculator(false) },
            onApplyWeight = { weight ->
                viewModel.togglePlateCalculator(false)
            }
        )
    }

    // Exercise Substitute Modal
    if (substituteSet != null) {
        ExerciseSubstituteModal(
            targetSet = substituteSet!!,
            availableExercises = exercises,
            onDismiss = { viewModel.openSubstituteModal(null) },
            onSelectSubstitute = { newEx ->
                viewModel.substituteExercise(substituteSet!!, newEx)
            }
        )
    }

    // Add Exercise Modal
    if (showAddExerciseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Add Exercise") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(exercises) { ex ->
                        TextButton(
                            onClick = {
                                viewModel.addSetToActiveSession(ex)
                                showAddExerciseDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${ex.name} (${ex.category})", modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

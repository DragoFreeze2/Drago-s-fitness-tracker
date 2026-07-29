package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GymProfileEntity
import com.example.ui.viewmodel.MainViewModel

@Composable
fun WorkoutsScreen(
    viewModel: MainViewModel,
    onOpenLogger: () -> Unit
) {
    val gymProfiles by viewModel.allGymProfiles.collectAsState()
    val selectedProfile by viewModel.selectedGymProfile.collectAsState()
    val exercises by viewModel.allExercises.collectAsState()
    val sessions by viewModel.allWorkoutSessions.collectAsState()

    var showNewProfileDialog by remember { mutableStateOf(false) }
    var newProfileName by remember { mutableStateOf("") }
    var selectedEquipment by remember { mutableStateOf(setOf("Barbell", "Dumbbell", "Cable", "Bodyweight")) }

    val availableEquipmentList = selectedProfile?.availableEquipmentCsv?.split(",")?.map { it.trim() } ?: emptyList()

    // Filter exercises by equipment profile
    val filteredExercises = exercises.filter { ex ->
        availableEquipmentList.isEmpty() || availableEquipmentList.any { eq -> ex.requiredEquipment.contains(eq, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.startNewWorkoutSession("Live Workout Session")
                    onOpenLogger()
                },
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Start Workout") },
                text = { Text("START LOGGING", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("start_workout_fab")
            )
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
                Text(
                    text = "Workouts & Equipment",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 1. Gym Profiles Selector
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "EQUIPMENT PROFILES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            IconButton(onClick = { showNewProfileDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Profile", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(gymProfiles) { profile ->
                                val isSelected = profile.id == selectedProfile?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectGymProfile(profile.id) },
                                    label = { Text(profile.name) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }

                        if (selectedProfile != null) {
                            Text(
                                text = "Active Equipment: ${selectedProfile!!.availableEquipmentCsv}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 2. Recent Workout Sessions
            item {
                Text(
                    text = "Recent Sessions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (sessions.isEmpty()) {
                item {
                    Text(
                        text = "No recorded sessions yet. Tap 'START LOGGING' to log your first workout!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(sessions.take(5)) { session ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.openSession(session.id)
                                onOpenLogger()
                            }
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = session.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (session.isCompleted) "Completed" else "In Progress",
                                        fontSize = 12.sp,
                                        color = if (session.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            TextButton(onClick = {
                                viewModel.openSession(session.id)
                                onOpenLogger()
                            }) {
                                Text("Resume")
                            }
                        }
                    }
                }
            }

            // 3. Exercises filtered by active gym profile
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Exercises (${filteredExercises.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(filteredExercises) { exercise ->
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
                        Column {
                            Text(
                                text = exercise.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${exercise.category} • Equipment: ${exercise.requiredEquipment}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = { },
                            label = { Text(exercise.targetMuscle, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    }

    // New Gym Profile Dialog
    if (showNewProfileDialog) {
        AlertDialog(
            onDismissRequest = { showNewProfileDialog = false },
            title = { Text("Create Gym Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Profile Name (e.g. Home Gym)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Select Equipment:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val allEquipmentOptions = listOf("Barbell", "Dumbbell", "Cable", "Leg Press", "Lat Pulldown", "Pull-up Bar", "Kettlebell", "Bodyweight")

                    allEquipmentOptions.forEach { eq ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEquipment = if (selectedEquipment.contains(eq)) {
                                        selectedEquipment - eq
                                    } else {
                                        selectedEquipment + eq
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = selectedEquipment.contains(eq),
                                onCheckedChange = { checked ->
                                    selectedEquipment = if (checked) selectedEquipment + eq else selectedEquipment - eq
                                }
                            )
                            Text(eq, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.createGymProfile(newProfileName, selectedEquipment.toList())
                            newProfileName = ""
                            showNewProfileDialog = false
                        }
                    }
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

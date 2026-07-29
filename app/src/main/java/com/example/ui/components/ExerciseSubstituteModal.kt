package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.WorkoutSetEntity

@Composable
fun ExerciseSubstituteModal(
    targetSet: WorkoutSetEntity,
    availableExercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onSelectSubstitute: (ExerciseEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredExercises = availableExercises.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.targetMuscle.contains(searchQuery, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "SUBSTITUTE EXERCISE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Replacing: ${targetSet.exerciseName}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search exercises...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredExercises) { exercise ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSubstitute(exercise) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = exercise.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${exercise.category} • ${exercise.requiredEquipment}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                SuggestionChip(
                                    onClick = { onSelectSubstitute(exercise) },
                                    label = { Text(exercise.targetMuscle, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

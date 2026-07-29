package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScannerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scannedFoodName by remember { mutableStateOf("Scanned Energy Bar") }
    var scannedCalories by remember { mutableStateOf(240f) }
    var scannedProtein by remember { mutableStateOf(20f) }
    var scannedCarbs by remember { mutableStateOf(28f) }
    var scannedFat by remember { mutableStateOf(7f) }
    var rawOcrText by remember { mutableStateOf("NUTRITION FACTS\nServing Size 60g\nCalories 240 kcal\nProtein 20g\nCarbohydrates 28g\nTotal Fat 7g") }

    val sampleNutritionLabels = listOf(
        "NUTRITION FACTS\nServing 100g\nEnergy 350 kcal\nProtein 25g\nCarbohydrate 40g\nFat 10g" to ("Scanned Protein Shake" to Triple(350f, 25f, 40f)),
        "NUTRITION FACTS\nServing 1 bar\nCalories 240 kcal\nProtein 20g\nCarbohydrates 28g\nTotal Fat 7g" to ("Scanned Energy Bar" to Triple(240f, 20f, 28f)),
        "BACK-OF-PACK TABLE\nPer 100ml\nCalories 120 kcal\nProtein 8g\nCarbs 14g\nFat 3.5g" to ("Greek Yogurt Drink" to Triple(120f, 8f, 14f))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline ML Kit OCR Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera viewfinder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Align Back-of-Pack Nutrition Table",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "100% Offline Google ML Kit Text Recognition",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.FlashOn, contentDescription = "Flash", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Quick Scan Preset Selector
            Text("Simulate Live Camera Capture:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sampleNutritionLabels.forEachIndexed { idx, (textData, parsed) ->
                    AssistChip(
                        onClick = {
                            rawOcrText = textData
                            scannedFoodName = parsed.first
                            scannedCalories = parsed.second.first
                            scannedProtein = parsed.second.second
                            scannedCarbs = parsed.second.third
                            scannedFat = 7f
                        },
                        label = { Text("Label ${idx + 1}", fontSize = 12.sp) }
                    )
                }
            }

            // Extracted OCR Data Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ocr_results_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PARSED NUTRITION VALUES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = scannedFoodName,
                        onValueChange = { scannedFoodName = it },
                        label = { Text("Detected Food Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scannedCalories.toInt().toString(),
                            onValueChange = { scannedCalories = it.toFloatOrNull() ?: scannedCalories },
                            label = { Text("Calories (kcal)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = scannedProtein.toInt().toString(),
                            onValueChange = { scannedProtein = it.toFloatOrNull() ?: scannedProtein },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scannedCarbs.toInt().toString(),
                            onValueChange = { scannedCarbs = it.toFloatOrNull() ?: scannedCarbs },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = scannedFat.toInt().toString(),
                            onValueChange = { scannedFat = it.toFloatOrNull() ?: scannedFat },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "Raw Recognized Text:\n$rawOcrText",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.addNutritionLog(
                        foodName = scannedFoodName,
                        mealType = "Snack",
                        calories = scannedCalories,
                        protein = scannedProtein,
                        carbs = scannedCarbs,
                        fat = scannedFat
                    )
                    onNavigateBack()
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("log_scanned_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LOG SCANNED FOOD ITEM", fontWeight = FontWeight.Bold)
            }
        }
    }
}

package com.example.domain

import android.content.Context
import android.content.Intent
import com.example.data.local.entity.GymProfileEntity
import com.example.data.local.entity.HealthMetricsEntity
import com.example.data.local.entity.UserGoalsEntity

object GeminiContextExporter {

    fun generateExportText(
        userGoals: UserGoalsEntity,
        healthMetrics: HealthMetricsEntity?,
        selectedProfile: GymProfileEntity?,
        avgCalories: Float,
        avgProtein: Float,
        avgCarbs: Float,
        avgFat: Float
    ): String {
        val equipment = selectedProfile?.availableEquipmentCsv ?: "Barbell, Dumbbell, Cable, Bodyweight"
        val profileName = selectedProfile?.name ?: "Main Gym"
        val weight = userGoals.weightKg
        val steps = healthMetrics?.steps ?: 8432

        return """
=== PRIVACY-FIRST HEALTH & FITNESS CONTEXT EXPORT ===
[USER PHYSICAL PROFILE & GOALS]
Current Body Weight: ${weight} kg
Target Daily Calories: ${userGoals.targetCalories} kcal
Target Macros: Protein ${userGoals.targetProtein}g | Carbs ${userGoals.targetCarbs}g | Fat ${userGoals.targetFat}g
Target Daily Steps: ${userGoals.targetSteps} (Recent: $steps)

[RECENT NUTRITION AVERAGES]
Avg Calories: ${avgCalories.toInt()} kcal
Avg Protein: ${avgProtein.toInt()}g | Carbs: ${avgCarbs.toInt()}g | Fat: ${avgFat.toInt()}g

[ACTIVE GYM PROFILE & EQUIPMENT]
Gym Profile Name: $profileName
Available Equipment: $equipment

====================================================
SYSTEM DIRECTIVES FOR GEMINI ASSISTANT:
You are an expert personal trainer and clinical nutritionist assisting a user with their health goals.
Strictly adhere to the following workflow:

1. EXPLAIN & CRITIQUE FIRST:
   Analyze the provided physical stats, recent macro averages, and equipment inventory. Explain proposed routine or diet adjustments in plain, conversational language. Highlight strengths and areas for improvement.

2. ASK FOR PREFERENCES:
   Ask the user for any specific preferences, scheduling constraints (e.g. 3-day vs 4-day split), injury limitations, or food allergies before generating a final structured plan.

3. WAIT FOR APPROVAL:
   Do NOT generate JSON output immediately. ONLY after the user explicitly approves your recommendations, output your updated workout routine or nutrition targets inside a single ```json ``` code block using the exact schema below so the app can import it directly offline:

```json
{
  "type": "WORKOUT_PLAN",
  "planName": "Push Pull Legs 4-Day Split",
  "targetCalories": 2500,
  "targetProtein": 170,
  "targetCarbs": 260,
  "targetFat": 75,
  "routines": [
    {
      "name": "Push Day A",
      "exercises": ["Barbell Bench Press", "Incline Dumbbell Press", "Overhead Shoulder Press", "Tricep Cable Pushdown"]
    }
  ]
}
```
====================================================
""".trimIndent()
    }

    fun shareToGemini(context: Context, exportText: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, exportText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export Health Context to Gemini")
        context.startActivity(shareIntent)
    }
}

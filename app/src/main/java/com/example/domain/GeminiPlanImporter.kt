package com.example.domain

import android.content.ClipboardManager
import android.content.Context
import com.example.data.local.dao.HealthDao
import com.example.data.local.dao.WorkoutDao
import com.example.data.local.entity.ExerciseEntity
import com.example.data.local.entity.UserGoalsEntity
import com.example.data.local.entity.WorkoutSessionEntity
import com.example.data.local.entity.WorkoutSetEntity
import org.json.JSONObject

data class ImportResult(
    val success: Boolean,
    val message: String
)

object GeminiPlanImporter {

    suspend fun importFromClipboard(
        context: Context,
        workoutDao: WorkoutDao,
        healthDao: HealthDao,
        activeProfileId: Long
    ): ImportResult {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return ImportResult(false, "Could not access system clipboard")

        val clipItem = clipboard.primaryClip?.getItemAt(0)
        val text = clipItem?.text?.toString() ?: ""

        if (text.isBlank()) {
            return ImportResult(false, "Clipboard is empty. Please copy Gemini's JSON plan first.")
        }

        val jsonString = extractJsonBlock(text)
        if (jsonString.isBlank()) {
            return ImportResult(false, "No ```json ... ``` code block found in clipboard text.")
        }

        return try {
            val json = JSONObject(jsonString)

            // Update Nutrition Goals if present
            if (json.has("targetCalories")) {
                val targetCalories = json.optDouble("targetCalories", 2400.0).toFloat()
                val targetProtein = json.optDouble("targetProtein", 160.0).toFloat()
                val targetCarbs = json.optDouble("targetCarbs", 250.0).toFloat()
                val targetFat = json.optDouble("targetFat", 70.0).toFloat()

                healthDao.insertOrUpdateUserGoals(
                    UserGoalsEntity(
                        targetCalories = targetCalories,
                        targetProtein = targetProtein,
                        targetCarbs = targetCarbs,
                        targetFat = targetFat
                    )
                )
            }

            // Update Workout Routines if present
            var importedRoutinesCount = 0
            if (json.has("routines")) {
                val routinesArray = json.getJSONArray("routines")
                for (i in 0 until routinesArray.length()) {
                    val routineObj = routinesArray.getJSONObject(i)
                    val routineName = routineObj.optString("name", "Custom Routine ${i + 1}")
                    
                    val sessionId = workoutDao.insertWorkoutSession(
                        WorkoutSessionEntity(
                            profileId = activeProfileId,
                            name = routineName,
                            isCompleted = false
                        )
                    )

                    if (routineObj.has("exercises")) {
                        val exercisesArray = routineObj.getJSONArray("exercises")
                        for (j in 0 until exercisesArray.length()) {
                            val exName = exercisesArray.getString(j)
                            val exCategory = if (routineName.contains("Push", true)) "Push" else if (routineName.contains("Pull", true)) "Pull" else "Legs"

                            // Save exercise to DB if it doesn't exist
                            val exEntity = ExerciseEntity(name = exName, category = exCategory, requiredEquipment = "Gym Equipment", targetMuscle = "Target Muscle")
                            val exId = workoutDao.insertExercises(listOf(exEntity))

                            // Add 3 initial sets per exercise
                            for (setIdx in 1..3) {
                                workoutDao.insertWorkoutSet(
                                    WorkoutSetEntity(
                                        sessionId = sessionId,
                                        exerciseId = j.toLong() + 100,
                                        exerciseName = exName,
                                        setOrder = setIdx,
                                        weightKg = 60f,
                                        reps = 10,
                                        isCompleted = false
                                    )
                                )
                            }
                        }
                    }
                    importedRoutinesCount++
                }
            }

            ImportResult(
                success = true,
                message = "Successfully imported Gemini plan! ($importedRoutinesCount routines & nutrition goals updated)."
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                message = "Error parsing JSON plan: ${e.localizedMessage}"
            )
        }
    }

    private fun extractJsonBlock(text: String): String {
        if (!text.contains("```json")) {
            if (text.trim().startsWith("{") && text.trim().endsWith("}")) {
                return text.trim()
            }
            return ""
        }
        val start = text.indexOf("```json") + 7
        val end = text.indexOf("```", start)
        return if (end > start) text.substring(start, end).trim() else ""
    }
}

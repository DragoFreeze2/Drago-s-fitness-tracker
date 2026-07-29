package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_profiles")
data class GymProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val availableEquipmentCsv: String, // e.g. "Barbell,Dumbbell,Cable"
    val isSelected: Boolean = false
)

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Push, Pull, Legs, Core, Cardio
    val requiredEquipment: String, // Barbell, Dumbbell, Machine, Bodyweight, etc.
    val targetMuscle: String
)

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val setOrder: Int,
    val weightKg: Float,
    val reps: Int,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "",
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val servingSize: Float = 100f,
    val servingUnit: String = "g"
)

@Entity(tableName = "nutrition_logs")
data class NutritionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val foodName: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val amount: Float,
    val unit: String
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val amountMl: Float
)

@Entity(tableName = "health_metrics")
data class HealthMetricsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val steps: Int = 0,
    val activeCalories: Float = 0f,
    val avgHeartRate: Float = 0f,
    val sleepHours: Float = 0f,
    val weightKg: Float = 0f
)

@Entity(tableName = "user_goals")
data class UserGoalsEntity(
    @PrimaryKey val id: Int = 1,
    val targetCalories: Float = 2400f,
    val targetProtein: Float = 160f,
    val targetCarbs: Float = 250f,
    val targetFat: Float = 70f,
    val targetWaterMl: Float = 2500f,
    val targetSteps: Int = 10000,
    val weightKg: Float = 75f
)

@Entity(tableName = "prayer_settings")
data class PrayerSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val cityName: String = "Dubai, UAE",
    val latitude: Double = 25.2048,
    val longitude: Double = 55.2708,
    val fajrOffsetMinutes: Int = 0,
    val dhuhrOffsetMinutes: Int = 0,
    val asrOffsetMinutes: Int = 0,
    val maghribOffsetMinutes: Int = 0,
    val ishaOffsetMinutes: Int = 0
)

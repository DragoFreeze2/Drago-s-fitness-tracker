package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GymProfileEntity::class,
        ExerciseEntity::class,
        WorkoutSessionEntity::class,
        WorkoutSetEntity::class,
        FoodItemEntity::class,
        NutritionLogEntity::class,
        WaterLogEntity::class,
        HealthMetricsEntity::class,
        UserGoalsEntity::class,
        PrayerSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun nutritionDao(): NutritionDao
    abstract fun healthDao(): HealthDao
    abstract fun prayerDao(): PrayerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_tracker_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val workoutDao = db.workoutDao()
            val healthDao = db.healthDao()
            val prayerDao = db.prayerDao()
            val nutritionDao = db.nutritionDao()

            // 1. Gym Profiles
            val mainGymId = workoutDao.insertGymProfile(
                GymProfileEntity(
                    name = "Main Gym",
                    availableEquipmentCsv = "Barbell,Dumbbell,Cable,Leg Press,Lat Pulldown,Smith Machine,Bench",
                    isSelected = true
                )
            )
            workoutDao.insertGymProfile(
                GymProfileEntity(
                    name = "Home Gym",
                    availableEquipmentCsv = "Dumbbell,Kettlebell,Pull-up Bar,Resistance Bands,Bodyweight",
                    isSelected = false
                )
            )

            // 2. Initial Exercises
            val defaultExercises = listOf(
                ExerciseEntity(name = "Barbell Bench Press", category = "Push", requiredEquipment = "Barbell", targetMuscle = "Chest"),
                ExerciseEntity(name = "Incline Dumbbell Press", category = "Push", requiredEquipment = "Dumbbell", targetMuscle = "Upper Chest"),
                ExerciseEntity(name = "Overhead Shoulder Press", category = "Push", requiredEquipment = "Barbell", targetMuscle = "Shoulders"),
                ExerciseEntity(name = "Tricep Cable Pushdown", category = "Push", requiredEquipment = "Cable", targetMuscle = "Triceps"),
                ExerciseEntity(name = "Barbell Deadlift", category = "Pull", requiredEquipment = "Barbell", targetMuscle = "Back & Hamstrings"),
                ExerciseEntity(name = "Lat Pulldown", category = "Pull", requiredEquipment = "Lat Pulldown", targetMuscle = "Lats"),
                ExerciseEntity(name = "Seated Cable Row", category = "Pull", requiredEquipment = "Cable", targetMuscle = "Mid Back"),
                ExerciseEntity(name = "Dumbbell Bicep Curl", category = "Pull", requiredEquipment = "Dumbbell", targetMuscle = "Biceps"),
                ExerciseEntity(name = "Barbell Back Squat", category = "Legs", requiredEquipment = "Barbell", targetMuscle = "Quads & Glutes"),
                ExerciseEntity(name = "Leg Press", category = "Legs", requiredEquipment = "Leg Press", targetMuscle = "Quads"),
                ExerciseEntity(name = "Dumbbell Romanian Deadlift", category = "Legs", requiredEquipment = "Dumbbell", targetMuscle = "Hamstrings"),
                ExerciseEntity(name = "Standing Calf Raise", category = "Legs", requiredEquipment = "Bodyweight", targetMuscle = "Calves"),
                ExerciseEntity(name = "Hanging Leg Raise", category = "Core", requiredEquipment = "Pull-up Bar", targetMuscle = "Abs"),
                ExerciseEntity(name = "Plank Hold", category = "Core", requiredEquipment = "Bodyweight", targetMuscle = "Core")
            )
            workoutDao.insertExercises(defaultExercises)

            // 3. Initial Sample Session
            val sessionId = workoutDao.insertWorkoutSession(
                WorkoutSessionEntity(
                    profileId = mainGymId,
                    name = "Push Day A",
                    durationSeconds = 2700,
                    notes = "Felt strong on bench press today.",
                    isCompleted = true
                )
            )
            workoutDao.insertWorkoutSet(WorkoutSetEntity(sessionId = sessionId, exerciseId = 1, exerciseName = "Barbell Bench Press", setOrder = 1, weightKg = 80f, reps = 10, isCompleted = true))
            workoutDao.insertWorkoutSet(WorkoutSetEntity(sessionId = sessionId, exerciseId = 1, exerciseName = "Barbell Bench Press", setOrder = 2, weightKg = 85f, reps = 8, isCompleted = true))
            workoutDao.insertWorkoutSet(WorkoutSetEntity(sessionId = sessionId, exerciseId = 1, exerciseName = "Barbell Bench Press", setOrder = 3, weightKg = 90f, reps = 6, isCompleted = true))

            // 4. Default User Goals & Health Metrics
            healthDao.insertOrUpdateUserGoals(
                UserGoalsEntity(
                    targetCalories = 2400f,
                    targetProtein = 160f,
                    targetCarbs = 250f,
                    targetFat = 70f,
                    targetWaterMl = 2500f,
                    targetSteps = 10000,
                    weightKg = 75f
                )
            )

            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            healthDao.insertOrUpdateHealthMetrics(
                HealthMetricsEntity(
                    date = todayDate,
                    steps = 8432,
                    activeCalories = 480f,
                    avgHeartRate = 68f,
                    sleepHours = 7.5f,
                    weightKg = 75f
                )
            )

            // 5. Sample Food items & Log
            nutritionDao.insertFoodItem(FoodItemEntity(name = "Oatmeal & Whey Protein", calories = 380f, protein = 32f, carbs = 45f, fat = 6f, servingSize = 100f, servingUnit = "g"))
            nutritionDao.insertFoodItem(FoodItemEntity(name = "Grilled Chicken Breast", calories = 220f, protein = 42f, carbs = 0f, fat = 5f, servingSize = 150f, servingUnit = "g"))
            nutritionDao.insertFoodItem(FoodItemEntity(name = "Brown Rice", calories = 215f, protein = 5f, carbs = 45f, fat = 2f, servingSize = 150f, servingUnit = "g"))
            nutritionDao.insertFoodItem(FoodItemEntity(name = "Greek Yogurt", calories = 150f, protein = 18f, carbs = 8f, fat = 4f, servingSize = 200f, servingUnit = "g"))

            nutritionDao.insertNutritionLog(NutritionLogEntity(date = todayDate, mealType = "Breakfast", foodName = "Oatmeal & Whey Protein", calories = 380f, protein = 32f, carbs = 45f, fat = 6f, amount = 100f, unit = "g"))
            nutritionDao.insertNutritionLog(NutritionLogEntity(date = todayDate, mealType = "Lunch", foodName = "Grilled Chicken Breast", calories = 220f, protein = 42f, carbs = 0f, fat = 5f, amount = 150f, unit = "g"))
            nutritionDao.insertNutritionLog(NutritionLogEntity(date = todayDate, mealType = "Lunch", foodName = "Brown Rice", calories = 215f, protein = 5f, carbs = 45f, fat = 2f, amount = 150f, unit = "g"))

            nutritionDao.insertWaterLog(WaterLogEntity(date = todayDate, amountMl = 1500f))

            // 6. Prayer Settings
            prayerDao.insertOrUpdatePrayerSettings(
                PrayerSettingsEntity(
                    cityName = "Dubai, UAE",
                    latitude = 25.2048,
                    longitude = 55.2708,
                    fajrOffsetMinutes = 0,
                    dhuhrOffsetMinutes = 0,
                    asrOffsetMinutes = 0,
                    maghribOffsetMinutes = 0,
                    ishaOffsetMinutes = 0
                )
            )
        }
    }
}

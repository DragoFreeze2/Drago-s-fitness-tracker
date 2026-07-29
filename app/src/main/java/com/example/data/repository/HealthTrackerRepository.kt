package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthTrackerRepository(private val db: AppDatabase) {

    private val workoutDao = db.workoutDao()
    private val nutritionDao = db.nutritionDao()
    private val healthDao = db.healthDao()
    private val prayerDao = db.prayerDao()

    fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Workouts
    fun getGymProfiles(): Flow<List<GymProfileEntity>> = workoutDao.getAllGymProfiles()
    fun getSelectedGymProfile(): Flow<GymProfileEntity?> = workoutDao.getSelectedGymProfile()
    suspend fun insertGymProfile(profile: GymProfileEntity): Long = workoutDao.insertGymProfile(profile)
    suspend fun selectProfile(profileId: Long) = workoutDao.selectProfile(profileId)

    fun getAllExercises(): Flow<List<ExerciseEntity>> = workoutDao.getAllExercises()
    fun getAllWorkoutSessions(): Flow<List<WorkoutSessionEntity>> = workoutDao.getAllWorkoutSessions()
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity? = workoutDao.getSessionById(sessionId)
    suspend fun createWorkoutSession(session: WorkoutSessionEntity): Long = workoutDao.insertWorkoutSession(session)
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity) = workoutDao.updateWorkoutSession(session)
    suspend fun deleteWorkoutSession(sessionId: Long) = workoutDao.deleteSession(sessionId)

    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>> = workoutDao.getSetsForSession(sessionId)
    suspend fun insertWorkoutSet(set: WorkoutSetEntity): Long = workoutDao.insertWorkoutSet(set)
    suspend fun updateWorkoutSet(set: WorkoutSetEntity) = workoutDao.updateWorkoutSet(set)
    suspend fun deleteWorkoutSet(set: WorkoutSetEntity) = workoutDao.deleteWorkoutSet(set)
    suspend fun getLastCompletedSetForExercise(exerciseId: Long): WorkoutSetEntity? = workoutDao.getLastCompletedSetForExercise(exerciseId)

    // Nutrition
    fun getNutritionLogsForDate(date: String = getTodayDate()): Flow<List<NutritionLogEntity>> = nutritionDao.getLogsForDate(date)
    suspend fun insertNutritionLog(log: NutritionLogEntity) = nutritionDao.insertNutritionLog(log)
    suspend fun deleteNutritionLog(log: NutritionLogEntity) = nutritionDao.deleteNutritionLog(log)

    fun getAllFoodItems(): Flow<List<FoodItemEntity>> = nutritionDao.getAllFoodItems()
    suspend fun insertFoodItem(food: FoodItemEntity) = nutritionDao.insertFoodItem(food)

    fun getWaterLogForDate(date: String = getTodayDate()): Flow<WaterLogEntity?> = nutritionDao.getWaterLogForDate(date)
    suspend fun addWater(date: String = getTodayDate(), amountMl: Float) {
        // Increment existing or insert new
        nutritionDao.getWaterLogForDate(date).collect { existing ->
            val currentAmount = existing?.amountMl ?: 0f
            nutritionDao.insertWaterLog(WaterLogEntity(date = date, amountMl = currentAmount + amountMl))
        }
    }

    // Health Metrics & Goals
    fun getHealthMetricsForDate(date: String = getTodayDate()): Flow<HealthMetricsEntity?> = healthDao.getHealthMetricsForDate(date)
    suspend fun updateHealthMetrics(metrics: HealthMetricsEntity) = healthDao.insertOrUpdateHealthMetrics(metrics)

    fun getUserGoals(): Flow<UserGoalsEntity?> = healthDao.getUserGoals()
    suspend fun updateUserGoals(goals: UserGoalsEntity) = healthDao.insertOrUpdateUserGoals(goals)

    // Prayer Times
    fun getPrayerSettings(): Flow<PrayerSettingsEntity?> = prayerDao.getPrayerSettings()
    suspend fun updatePrayerSettings(settings: PrayerSettingsEntity) = prayerDao.insertOrUpdatePrayerSettings(settings)

    // Daos reference for Gemini importer
    fun getWorkoutDao() = workoutDao
    fun getHealthDao() = healthDao
}

package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM gym_profiles")
    fun getAllGymProfiles(): Flow<List<GymProfileEntity>>

    @Query("SELECT * FROM gym_profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedGymProfile(): Flow<GymProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGymProfile(profile: GymProfileEntity): Long

    @Update
    suspend fun updateGymProfile(profile: GymProfileEntity)

    @Query("UPDATE gym_profiles SET isSelected = 0")
    suspend fun deselectAllProfiles()

    @Transaction
    suspend fun selectProfile(profileId: Long) {
        deselectAllProfiles()
        updateProfileSelectedState(profileId, true)
    }

    @Query("UPDATE gym_profiles SET isSelected = :isSelected WHERE id = :profileId")
    suspend fun updateProfileSelectedState(profileId: Long, isSelected: Boolean)

    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Query("SELECT * FROM workout_sessions ORDER BY timestamp DESC")
    fun getAllWorkoutSessions(): Flow<List<WorkoutSessionEntity>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): WorkoutSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSessionEntity): Long

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSessionEntity)

    @Query("SELECT * FROM workout_sets WHERE sessionId = :sessionId ORDER BY setOrder ASC")
    fun getSetsForSession(sessionId: Long): Flow<List<WorkoutSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSet(set: WorkoutSetEntity): Long

    @Update
    suspend fun updateWorkoutSet(set: WorkoutSetEntity)

    @Delete
    suspend fun deleteWorkoutSet(set: WorkoutSetEntity)

    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND isCompleted = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getLastCompletedSetForExercise(exerciseId: Long): WorkoutSetEntity?

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}

@Dao
interface NutritionDao {
    @Query("SELECT * FROM nutrition_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<NutritionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionLog(log: NutritionLogEntity): Long

    @Delete
    suspend fun deleteNutritionLog(log: NutritionLogEntity)

    @Query("SELECT * FROM food_items")
    fun getAllFoodItems(): Flow<List<FoodItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(food: FoodItemEntity): Long

    @Query("SELECT * FROM water_logs WHERE date = :date LIMIT 1")
    fun getWaterLogForDate(date: String): Flow<WaterLogEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLogEntity)
}

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_metrics WHERE date = :date LIMIT 1")
    fun getHealthMetricsForDate(date: String): Flow<HealthMetricsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHealthMetrics(metrics: HealthMetricsEntity)

    @Query("SELECT * FROM user_goals WHERE id = 1 LIMIT 1")
    fun getUserGoals(): Flow<UserGoalsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserGoals(goals: UserGoalsEntity)
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_settings WHERE id = 1 LIMIT 1")
    fun getPrayerSettings(): Flow<PrayerSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePrayerSettings(settings: PrayerSettingsEntity)
}

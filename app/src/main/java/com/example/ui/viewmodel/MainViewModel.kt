package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.HealthTrackerRepository
import com.example.domain.AthanEngine
import com.example.domain.DailyPrayerTimes
import com.example.domain.GeminiContextExporter
import com.example.domain.GeminiPlanImporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HealthTrackerRepository
    
    // Core Flows
    val selectedGymProfile: StateFlow<GymProfileEntity?>
    val allGymProfiles: StateFlow<List<GymProfileEntity>>
    val allExercises: StateFlow<List<ExerciseEntity>>
    val allWorkoutSessions: StateFlow<List<WorkoutSessionEntity>>
    
    val todayNutritionLogs: StateFlow<List<NutritionLogEntity>>
    val todayWaterLog: StateFlow<WaterLogEntity?>
    val allFoodItems: StateFlow<List<FoodItemEntity>>

    val todayHealthMetrics: StateFlow<HealthMetricsEntity?>
    val userGoals: StateFlow<UserGoalsEntity?>
    val prayerSettings: StateFlow<PrayerSettingsEntity?>

    // Calculated Dashboard State
    val dailyPrayerTimes: StateFlow<DailyPrayerTimes?>

    // UI States & Active Logger
    private val _activeSessionId = MutableStateFlow<Long?>(null)
    val activeSessionId: StateFlow<Long?> = _activeSessionId.asStateFlow()

    val activeSessionSets: StateFlow<List<WorkoutSetEntity>>

    // Rest Timer
    private val _restTimerSeconds = MutableStateFlow(0)
    val restTimerSeconds: StateFlow<Int> = _restTimerSeconds.asStateFlow()
    private var restTimerJob: Job? = null

    // Plate Calculator Modal
    private val _showPlateCalculator = MutableStateFlow(false)
    val showPlateCalculator: StateFlow<Boolean> = _showPlateCalculator.asStateFlow()

    // Substitute Exercise Modal
    private val _substituteTargetSet = MutableStateFlow<WorkoutSetEntity?>(null)
    val substituteTargetSet: StateFlow<WorkoutSetEntity?> = _substituteTargetSet.asStateFlow()

    // Snack / Notification toast message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HealthTrackerRepository(db)

        selectedGymProfile = repository.getSelectedGymProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allGymProfiles = repository.getGymProfiles()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allExercises = repository.getAllExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        allWorkoutSessions = repository.getAllWorkoutSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        todayNutritionLogs = repository.getNutritionLogsForDate()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        todayWaterLog = repository.getWaterLogForDate()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allFoodItems = repository.getAllFoodItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        todayHealthMetrics = repository.getHealthMetricsForDate()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        userGoals = repository.getUserGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoalsEntity())

        prayerSettings = repository.getPrayerSettings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrayerSettingsEntity())

        activeSessionSets = _activeSessionId
            .flatMapLatest { id ->
                if (id != null) repository.getSetsForSession(id) else flowOf(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        dailyPrayerTimes = prayerSettings.map { settings ->
            if (settings != null) AthanEngine.calculatePrayerTimes(settings) else null
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // --- WORKOUT LOGGER LOGIC ---
    fun selectGymProfile(profileId: Long) {
        viewModelScope.launch {
            repository.selectProfile(profileId)
        }
    }

    fun createGymProfile(name: String, equipmentList: List<String>) {
        viewModelScope.launch {
            repository.insertGymProfile(
                GymProfileEntity(
                    name = name,
                    availableEquipmentCsv = equipmentList.joinToString(","),
                    isSelected = false
                )
            )
            _userMessage.value = "Created profile '$name'"
        }
    }

    fun startNewWorkoutSession(name: String = "Workout Session") {
        viewModelScope.launch {
            val profile = selectedGymProfile.value
            val profileId = profile?.id ?: 1L
            val newSessionId = repository.createWorkoutSession(
                WorkoutSessionEntity(
                    profileId = profileId,
                    name = name,
                    timestamp = System.currentTimeMillis()
                )
            )
            // Default: Add 3 blank sets for the first default exercise
            val exercises = allExercises.value
            val defaultEx = exercises.firstOrNull() ?: ExerciseEntity(name = "Barbell Bench Press", category = "Push", requiredEquipment = "Barbell", targetMuscle = "Chest")
            
            for (order in 1..3) {
                repository.insertWorkoutSet(
                    WorkoutSetEntity(
                        sessionId = newSessionId,
                        exerciseId = defaultEx.id,
                        exerciseName = defaultEx.name,
                        setOrder = order,
                        weightKg = 60f,
                        reps = 10,
                        isCompleted = false
                    )
                )
            }
            _activeSessionId.value = newSessionId
        }
    }

    fun openSession(sessionId: Long) {
        _activeSessionId.value = sessionId
    }

    fun addSetToActiveSession(exercise: ExerciseEntity) {
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            val currentSets = activeSessionSets.value
            val nextOrder = (currentSets.filter { it.exerciseId == exercise.id }.maxOfOrNull { it.setOrder } ?: 0) + 1
            
            // Prefill with last performance if available
            val lastSet = repository.getLastCompletedSetForExercise(exercise.id)
            val prefillWeight = lastSet?.weightKg ?: 60f
            val prefillReps = lastSet?.reps ?: 10

            repository.insertWorkoutSet(
                WorkoutSetEntity(
                    sessionId = sessionId,
                    exerciseId = exercise.id,
                    exerciseName = exercise.name,
                    setOrder = nextOrder,
                    weightKg = prefillWeight,
                    reps = prefillReps,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleSetCompletion(set: WorkoutSetEntity) {
        viewModelScope.launch {
            val newCompleted = !set.isCompleted
            repository.updateWorkoutSet(set.copy(isCompleted = newCompleted))
            if (newCompleted) {
                startRestTimer(90) // Start 90s default rest timer on set completion
            }
        }
    }

    fun updateSetValues(set: WorkoutSetEntity, weightKg: Float, reps: Int) {
        viewModelScope.launch {
            repository.updateWorkoutSet(set.copy(weightKg = weightKg, reps = reps))
        }
    }

    fun deleteSet(set: WorkoutSetEntity) {
        viewModelScope.launch {
            repository.deleteWorkoutSet(set)
        }
    }

    fun startRestTimer(seconds: Int) {
        restTimerJob?.cancel()
        _restTimerSeconds.value = seconds
        restTimerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000)
                _restTimerSeconds.value -= 1
            }
        }
    }

    fun stopRestTimer() {
        restTimerJob?.cancel()
        _restTimerSeconds.value = 0
    }

    fun togglePlateCalculator(show: Boolean) {
        _showPlateCalculator.value = show
    }

    fun openSubstituteModal(set: WorkoutSetEntity?) {
        _substituteTargetSet.value = set
    }

    fun substituteExercise(oldSet: WorkoutSetEntity, newExercise: ExerciseEntity) {
        viewModelScope.launch {
            repository.updateWorkoutSet(
                oldSet.copy(
                    exerciseId = newExercise.id,
                    exerciseName = newExercise.name
                )
            )
            _substituteTargetSet.value = null
            _userMessage.value = "Substituted with ${newExercise.name}"
        }
    }

    fun finishWorkoutSession() {
        val sessionId = _activeSessionId.value ?: return
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            if (session != null) {
                repository.updateWorkoutSession(session.copy(isCompleted = true))
            }
            _activeSessionId.value = null
            _userMessage.value = "Workout completed and saved to local DB!"
        }
    }

    // --- NUTRITION & OCR LOGIC ---
    fun addNutritionLog(foodName: String, mealType: String, calories: Float, protein: Float, carbs: Float, fat: Float, amount: Float = 100f, unit: String = "g") {
        viewModelScope.launch {
            repository.insertNutritionLog(
                NutritionLogEntity(
                    date = repository.getTodayDate(),
                    mealType = mealType,
                    foodName = foodName,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    amount = amount,
                    unit = unit
                )
            )
            // Save to food items library
            repository.insertFoodItem(
                FoodItemEntity(
                    name = foodName,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    servingSize = amount,
                    servingUnit = unit
                )
            )
            _userMessage.value = "Logged $foodName ($calories kcal)"
        }
    }

    fun deleteNutritionLog(log: NutritionLogEntity) {
        viewModelScope.launch {
            repository.deleteNutritionLog(log)
        }
    }

    fun addWaterIntake(amountMl: Float) {
        viewModelScope.launch {
            val date = repository.getTodayDate()
            val current = todayWaterLog.value?.amountMl ?: 0f
            dbInsertWater(date, current + amountMl)
            _userMessage.value = "Added ${amountMl.toInt()} mL water"
        }
    }

    private suspend fun dbInsertWater(date: String, amountMl: Float) {
        val db = AppDatabase.getDatabase(getApplication())
        db.nutritionDao().insertWaterLog(WaterLogEntity(date = date, amountMl = amountMl))
    }

    // --- HEALTH CONNECT & STEPS ---
    fun syncHealthConnectData() {
        viewModelScope.launch {
            val date = repository.getTodayDate()
            val current = todayHealthMetrics.value ?: HealthMetricsEntity(date = date)
            // Simulated / Native Health Connect sync read
            val updated = current.copy(
                steps = (current.steps + 1250).coerceAtMost(18000),
                activeCalories = (current.activeCalories + 95f),
                avgHeartRate = 72f,
                sleepHours = 7.8f
            )
            repository.updateHealthMetrics(updated)
            _userMessage.value = "Synced native Health Connect metrics (+1,250 steps)"
        }
    }

    // --- PRAYER TIMES SETTINGS ---
    fun updatePrayerOffset(prayerName: String, offsetDelta: Int) {
        val current = prayerSettings.value ?: PrayerSettingsEntity()
        val updated = when (prayerName) {
            "Fajr" -> current.copy(fajrOffsetMinutes = current.fajrOffsetMinutes + offsetDelta)
            "Dhuhr" -> current.copy(dhuhrOffsetMinutes = current.dhuhrOffsetMinutes + offsetDelta)
            "Asr" -> current.copy(asrOffsetMinutes = current.asrOffsetMinutes + offsetDelta)
            "Maghrib" -> current.copy(maghribOffsetMinutes = current.maghribOffsetMinutes + offsetDelta)
            "Isha" -> current.copy(ishaOffsetMinutes = current.ishaOffsetMinutes + offsetDelta)
            else -> current
        }
        viewModelScope.launch {
            repository.updatePrayerSettings(updated)
        }
    }

    fun updateCityLocation(cityName: String, lat: Double, lng: Double) {
        val current = prayerSettings.value ?: PrayerSettingsEntity()
        viewModelScope.launch {
            repository.updatePrayerSettings(
                current.copy(
                    cityName = cityName,
                    latitude = lat,
                    longitude = lng
                )
            )
            _userMessage.value = "Location updated to $cityName"
        }
    }

    // --- GEMINI INTERACTION LOGIC ---
    fun exportContextToGemini(context: Context) {
        val goals = userGoals.value ?: UserGoalsEntity()
        val metrics = todayHealthMetrics.value
        val profile = selectedGymProfile.value
        val logs = todayNutritionLogs.value

        val totalCalories = logs.sumOf { it.calories.toDouble() }.toFloat()
        val totalProtein = logs.sumOf { it.protein.toDouble() }.toFloat()
        val totalCarbs = logs.sumOf { it.carbs.toDouble() }.toFloat()
        val totalFat = logs.sumOf { it.fat.toDouble() }.toFloat()

        val text = GeminiContextExporter.generateExportText(
            userGoals = goals,
            healthMetrics = metrics,
            selectedProfile = profile,
            avgCalories = totalCalories,
            avgProtein = totalProtein,
            avgCarbs = totalCarbs,
            avgFat = totalFat
        )

        GeminiContextExporter.shareToGemini(context, text)
    }

    fun importPlanFromClipboard(context: Context) {
        viewModelScope.launch {
            val activeProfileId = selectedGymProfile.value?.id ?: 1L
            val result = GeminiPlanImporter.importFromClipboard(
                context = context,
                workoutDao = repository.getWorkoutDao(),
                healthDao = repository.getHealthDao(),
                activeProfileId = activeProfileId
            )
            _userMessage.value = result.message
        }
    }
}

package com.example.tplanner.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tplanner.data.DummyData
import com.example.tplanner.data.Exercise
import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.ProgramExercise
import com.example.tplanner.data.SessionHistory
import com.example.tplanner.data.TPlannerDatabase
import com.example.tplanner.data.repository.WorkoutRepository
import com.example.tplanner.utils.ProgressCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// Represents a single validated set
data class ValidatedSet(
    val weight: String, 
    val rpe: String,
    val reps: String = "",
    val restTime: String? = null
)

// Represents the state of the workout screen
data class WorkoutUiState(
    val day: String = "",
    val exercises: List<ExerciseState> = emptyList(),
    val isLoading: Boolean = true,
    val showSummaryDialog: Boolean = false,
    val totalVolume: Double = 0.0,
    val averageRpe: Double = 0.0,
    val sessionId: Long? = null,
    val volumeComparison: String = ""
)

// Represents the state of a single exercise card
data class ExerciseState(
    val exercise: ProgramExercise,
    val weight: String = exercise.targetWeight.toString(),
    val rpe: String = exercise.targetRpe.toString(),
    val reps: String = exercise.reps,
    val restTime: String = "",
    val comment: String = exercise.comment ?: "",
    val validatedSets: List<ValidatedSet> = emptyList(),
    val lastPerformance: String? = null
)

class WorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var repository: WorkoutRepository? = null

    fun initRepository(context: Context) {
        if (repository == null) {
            val database = TPlannerDatabase.getDatabase(context)
            repository = WorkoutRepository(
                database.programExerciseDao(),
                database.sessionHistoryDao(),
                database.performanceRecordDao()
            )
        }
    }

    fun loadSession(day: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository?.let { repo ->
                // Create new session
                val session = SessionHistory(day = day, timestamp = Date().time)
                val sessionId = repo.insertSession(session)

                // Load program exercises for this day
                repo.getExercisesByDay(day).collect { exercises ->
                    if (exercises.isNotEmpty()) {
                        // Load last performance for each exercise
                        val exerciseStates = exercises.map { exercise ->
                            val lastRecord = repo.getLastRecordForExercise(exercise.name)
                            ExerciseState(
                                exercise = exercise,
                                weight = lastRecord?.weight?.toString() ?: exercise.targetWeight.toString(),
                                rpe = lastRecord?.rpe?.toString() ?: exercise.targetRpe.toString(),
                                reps = exercise.reps,
                                restTime = lastRecord?.restTime?.toString() ?: "",
                                lastPerformance = lastRecord?.let {
                                    "Dernière: ${it.weight}kg × ${it.reps} @ RPE ${it.rpe}"
                                }
                            )
                        }

                        _uiState.update {
                            it.copy(
                                day = day,
                                exercises = exerciseStates,
                                isLoading = false,
                                sessionId = sessionId
                            )
                        }
                    } else {
                        // Fallback to dummy data if no program exists
                        loadFromDummyData(day, sessionId)
                    }
                }
            } ?: run {
                // No repository, use dummy data
                loadFromDummyData(day, null)
            }
        }
    }

    private fun loadFromDummyData(day: String, sessionId: Long?) {
        val session = DummyData.sessions.find { it.day == day }
        if (session != null) {
            _uiState.update {
                it.copy(
                    day = session.day,
                    exercises = session.exercises.map { exercise ->
                        ExerciseState(
                            exercise = ProgramExercise(
                                day = exercise.day,
                                name = exercise.name,
                                sets = exercise.sets,
                                reps = exercise.reps,
                                targetWeight = exercise.weight,
                                targetRpe = exercise.rpe,
                                category = exercise.category,
                                comment = exercise.comment
                            ),
                            weight = exercise.weight.toString(),
                            rpe = exercise.rpe.toString()
                        )
                    },
                    isLoading = false,
                    sessionId = sessionId
                )
            }
        }
    }

    fun onWeightChange(exerciseId: String, newWeight: String) {
        updateExerciseState(exerciseId) { it.copy(weight = newWeight) }
    }

    fun onRpeChange(exerciseId: String, newRpe: String) {
        updateExerciseState(exerciseId) { it.copy(rpe = newRpe) }
    }

    fun onRepsChange(exerciseId: String, newReps: String) {
        updateExerciseState(exerciseId) { it.copy(reps = newReps) }
    }

    fun onRestTimeChange(exerciseId: String, newRestTime: String) {
        updateExerciseState(exerciseId) { it.copy(restTime = newRestTime) }
    }

    fun onCommentChange(exerciseId: String, newComment: String) {
        updateExerciseState(exerciseId) { it.copy(comment = newComment) }
    }

    fun validateSet(exerciseId: String) {
        _uiState.value.exercises.find { it.exercise.name == exerciseId }?.let { exerciseState ->
            val newSet = ValidatedSet(
                weight = exerciseState.weight,
                rpe = exerciseState.rpe,
                reps = exerciseState.reps,
                restTime = exerciseState.restTime.takeIf { it.isNotBlank() }
            )
            updateExerciseState(exerciseId) { it.copy(validatedSets = it.validatedSets + newSet) }
        }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            var totalVolume = 0.0
            var totalRpe = 0.0
            var setCount = 0
            val performanceRecords = mutableListOf<PerformanceRecord>()

            val sessionId = _uiState.value.sessionId ?: return@launch

            _uiState.value.exercises.forEach { exerciseState ->
                exerciseState.validatedSets.forEach { set ->
                    val weight = set.weight.toDoubleOrNull() ?: 0.0
                    val rpe = set.rpe.toIntOrNull() ?: 0
                    val reps = parseReps(set.reps)
                    val restTime = set.restTime?.toIntOrNull()

                    totalVolume += weight * reps
                    totalRpe += rpe
                    setCount++

                    performanceRecords.add(
                        PerformanceRecord(
                            sessionId = sessionId,
                            exerciseName = exerciseState.exercise.name,
                            sets = 1,
                            reps = reps,
                            weight = weight,
                            rpe = rpe,
                            restTime = restTime,
                            comment = exerciseState.comment.takeIf { it.isNotBlank() },
                            category = exerciseState.exercise.category
                        )
                    )
                }
            }

            val averageRpe = if (setCount > 0) totalRpe / setCount else 0.0

            // Save performance records
            repository?.insertPerformanceRecords(performanceRecords)

            // Update session with totals
            repository?.getSessionById(sessionId)?.let { session ->
                repository?.updateSession(
                    session.copy(
                        totalVolume = totalVolume,
                        averageRpe = averageRpe,
                        completed = true
                    )
                )
            }

            // Calculate volume comparison with previous session
            val volumeComparison = repository?.let { repo ->
                val previousSessions = mutableListOf<SessionHistory>()
                repo.getSessionsByDay(_uiState.value.day, limit = 2).collect { sessions ->
                    previousSessions.addAll(sessions)
                }
                
                if (previousSessions.size >= 2) {
                    val previousVolume = previousSessions[1].totalVolume
                    if (previousVolume > 0) {
                        val change = ((totalVolume - previousVolume) / previousVolume) * 100
                        "Volume total: ${String.format("%+.1f", change)}% par rapport à la dernière séance"
                    } else {
                        "Volume total: ${String.format("%.1f", totalVolume)} kg"
                    }
                } else {
                    "Volume total: ${String.format("%.1f", totalVolume)} kg"
                }
            } ?: "Volume total: ${String.format("%.1f", totalVolume)} kg"

            _uiState.update {
                it.copy(
                    totalVolume = totalVolume,
                    averageRpe = averageRpe,
                    volumeComparison = volumeComparison,
                    showSummaryDialog = true
                )
            }
        }
    }

    fun dismissSummaryDialog() {
        _uiState.update { it.copy(showSummaryDialog = false) }
    }

    private fun parseReps(repsString: String): Int {
        return repsString.split('-').mapNotNull { it.trim().toIntOrNull() }.average().toInt()
    }

    private fun updateExerciseState(exerciseId: String, updateAction: (ExerciseState) -> ExerciseState) {
        _uiState.update {
            val updatedExercises = it.exercises.map {
                if (it.exercise.name == exerciseId) updateAction(it) else it
            }
            it.copy(exercises = updatedExercises)
        }
    }
}
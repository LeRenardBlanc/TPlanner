package com.example.tplanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tplanner.data.DummyData
import com.example.tplanner.data.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents a single validated set
data class ValidatedSet(val weight: String, val rpe: String)

// Represents the state of the workout screen
data class WorkoutUiState(
    val day: String = "",
    val exercises: List<ExerciseState> = emptyList(),
    val isLoading: Boolean = true,
    val showSummaryDialog: Boolean = false,
    val totalVolume: Double = 0.0,
    val averageRpe: Double = 0.0
)

// Represents the state of a single exercise card
data class ExerciseState(
    val exercise: Exercise,
    val weight: String = exercise.weight.toString(),
    val rpe: String = exercise.rpe.toString(),
    val comment: String = exercise.comment ?: "",
    val validatedSets: List<ValidatedSet> = emptyList()
)

class WorkoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun loadSession(day: String) {
        viewModelScope.launch {
            val session = DummyData.sessions.find { it.day == day }
            if (session != null) {
                _uiState.update {
                    it.copy(
                        day = session.day,
                        exercises = session.exercises.map { exercise -> ExerciseState(exercise) },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onWeightChange(exerciseId: String, newWeight: String) {
        updateExerciseState(exerciseId) { it.copy(weight = newWeight) }
    }

    fun onRpeChange(exerciseId: String, newRpe: String) {
        updateExerciseState(exerciseId) { it.copy(rpe = newRpe) }
    }

    fun onCommentChange(exerciseId: String, newComment: String) {
        updateExerciseState(exerciseId) { it.copy(comment = newComment) }
    }

    fun validateSet(exerciseId: String) {
        _uiState.value.exercises.find { it.exercise.name == exerciseId }?.let { exerciseState ->
            val newSet = ValidatedSet(exerciseState.weight, exerciseState.rpe)
            updateExerciseState(exerciseId) { it.copy(validatedSets = it.validatedSets + newSet) }
        }
    }

    fun finishWorkout() {
        var totalVolume = 0.0
        var totalRpe = 0.0
        var setCount = 0

        _uiState.value.exercises.forEach { exerciseState ->
            val reps = parseReps(exerciseState.exercise.reps)
            exerciseState.validatedSets.forEach { set ->
                val weight = set.weight.toDoubleOrNull() ?: 0.0
                val rpe = set.rpe.toDoubleOrNull() ?: 0.0
                totalVolume += weight * reps
                totalRpe += rpe
                setCount++
            }
        }

        val averageRpe = if (setCount > 0) totalRpe / setCount else 0.0

        _uiState.update {
            it.copy(
                totalVolume = totalVolume,
                averageRpe = averageRpe,
                showSummaryDialog = true
            )
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
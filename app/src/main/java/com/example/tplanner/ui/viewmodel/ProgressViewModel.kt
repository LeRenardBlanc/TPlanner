package com.example.tplanner.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.TPlannerDatabase
import com.example.tplanner.data.repository.WorkoutRepository
import com.example.tplanner.utils.ProgressCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

class ProgressViewModel : ViewModel() {
    private val _exercises = MutableStateFlow<List<String>>(emptyList())
    val exercises: StateFlow<List<String>> = _exercises.asStateFlow()

    private val _stats = MutableStateFlow<Map<String, String>>(emptyMap())
    val stats: StateFlow<Map<String, String>> = _stats.asStateFlow()

    private val _categoryStats = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryStats: StateFlow<Map<String, Double>> = _categoryStats.asStateFlow()

    private var repository: WorkoutRepository? = null

    fun initRepository(context: Context) {
        if (repository == null) {
            val database = TPlannerDatabase.getDatabase(context)
            repository = WorkoutRepository(
                database.programExerciseDao(),
                database.sessionHistoryDao(),
                database.performanceRecordDao()
            )
            loadExercises()
            loadStats()
            loadCategoryStats()
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            repository?.getAllExercises()?.collect { programExercises ->
                _exercises.value = programExercises.map { it.name }.distinct()
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository?.let { repo ->
                // Get sessions from last 30 days
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, -30)
                val startTime = calendar.timeInMillis

                repo.getSessionsInRange(startTime, endTime).collect { sessions ->
                    val totalVolume = sessions.sumOf { it.totalVolume }
                    val averageRpe = if (sessions.isNotEmpty()) {
                        sessions.map { it.averageRpe }.average()
                    } else 0.0

                    val completedSessions = sessions.count { it.completed }

                    _stats.value = mapOf(
                        "Volume total (30 jours)" to "${String.format("%.1f", totalVolume)} kg",
                        "RPE moyen" to String.format("%.1f", averageRpe),
                        "Séances complétées" to "$completedSessions",
                        "Fréquence hebdomadaire" to String.format("%.1f", completedSessions / 4.0)
                    )
                }
            }
        }
    }

    private fun loadCategoryStats() {
        viewModelScope.launch {
            repository?.let { repo ->
                // Get all performance records
                repo.getAllExercises().collect { exercises ->
                    val categoryVolumes = mutableMapOf<String, Double>()

                    exercises.forEach { exercise ->
                        repo.getRecordsByExercise(exercise.name, limit = 10).collect { records ->
                            if (records.isNotEmpty()) {
                                val volume = ProgressCalculator.calculateTotalVolume(records)
                                categoryVolumes[exercise.category] = 
                                    categoryVolumes.getOrDefault(exercise.category, 0.0) + volume
                            }
                        }
                    }

                    _categoryStats.value = categoryVolumes
                }
            }
        }
    }

    fun getExerciseProgress(exerciseName: String): Flow<List<PerformanceRecord>> {
        return repository?.getRecordsByExercise(exerciseName, limit = 20) 
            ?: MutableStateFlow(emptyList())
    }

    fun calculateEstimated1RM(weight: Double, reps: Int): Double {
        return ProgressCalculator.estimate1RMEpley(weight, reps)
    }
}

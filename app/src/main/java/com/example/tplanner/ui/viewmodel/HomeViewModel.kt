package com.example.tplanner.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tplanner.data.ProgramExercise
import com.example.tplanner.data.TPlannerDatabase
import com.example.tplanner.data.repository.WorkoutRepository
import com.example.tplanner.utils.ProgressCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class WeekStats(
    val progressIndex: String = "+0.0",
    val totalVolume: String = "0.0",
    val averageRpe: String = "0.0"
)

class HomeViewModel : ViewModel() {
    private val _programExercises = MutableStateFlow<List<ProgramExercise>>(emptyList())
    val programExercises: StateFlow<List<ProgramExercise>> = _programExercises.asStateFlow()

    private val _weekStats = MutableStateFlow(WeekStats())
    val weekStats: StateFlow<WeekStats> = _weekStats.asStateFlow()

    private var repository: WorkoutRepository? = null

    fun initRepository(context: Context) {
        if (repository == null) {
            val database = TPlannerDatabase.getDatabase(context)
            repository = WorkoutRepository(
                database.programExerciseDao(),
                database.sessionHistoryDao(),
                database.performanceRecordDao()
            )
            loadProgramExercises()
            loadWeekStats()
        }
    }

    private fun loadProgramExercises() {
        viewModelScope.launch {
            repository?.getAllExercises()?.collect { exercises ->
                _programExercises.value = exercises
            }
        }
    }

    private fun loadWeekStats() {
        viewModelScope.launch {
            repository?.let { repo ->
                // Get current week sessions
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val weekStart = calendar.timeInMillis

                calendar.add(Calendar.DAY_OF_WEEK, 7)
                val weekEnd = calendar.timeInMillis

                repo.getSessionsInRange(weekStart, weekEnd).collect { currentWeekSessions ->
                    // Get previous week sessions
                    val prevWeekStart = weekStart - 7 * 24 * 60 * 60 * 1000L
                    val prevWeekEnd = weekStart

                    repo.getSessionsInRange(prevWeekStart, prevWeekEnd).collect { previousWeekSessions ->
                        val progressIndex = ProgressCalculator.calculateProgressiveOverloadIndex(
                            currentWeekSessions,
                            previousWeekSessions
                        )

                        val totalVolume = currentWeekSessions.sumOf { it.totalVolume }
                        val averageRpe = if (currentWeekSessions.isNotEmpty()) {
                            currentWeekSessions.map { it.averageRpe }.average()
                        } else 0.0

                        _weekStats.value = WeekStats(
                            progressIndex = String.format("%+.1f", progressIndex),
                            totalVolume = String.format("%.1f", totalVolume),
                            averageRpe = String.format("%.1f", averageRpe)
                        )
                    }
                }
            }
        }
    }
}

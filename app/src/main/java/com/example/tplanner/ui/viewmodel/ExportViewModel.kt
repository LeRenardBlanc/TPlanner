package com.example.tplanner.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.ProgramExercise
import com.example.tplanner.data.TPlannerDatabase
import com.example.tplanner.data.repository.WorkoutRepository
import com.example.tplanner.utils.CsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExportUiState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val error: String? = null
)

class ExportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    private var repository: WorkoutRepository? = null
    var exportType: ExportType = ExportType.PROGRAM

    enum class ExportType {
        PROGRAM,
        PERFORMANCE
    }

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

    fun exportToUri(context: Context, uri: Uri) {
        _uiState.update { it.copy(isExporting = true, exportSuccess = false, error = null) }

        viewModelScope.launch {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    when (exportType) {
                        ExportType.PROGRAM -> exportProgram(outputStream)
                        ExportType.PERFORMANCE -> exportPerformance(outputStream)
                    }
                    outputStream.close()
                    _uiState.update { it.copy(isExporting = false, exportSuccess = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            error = "Impossible d'ouvrir le fichier pour l'écriture"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = "Erreur lors de l'export: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun exportProgram(outputStream: java.io.OutputStream) {
        val exercises = mutableListOf<ProgramExercise>()
        repository?.getAllExercises()?.collect { programExercises ->
            exercises.addAll(programExercises)
        }
        CsvExporter.exportProgram(exercises, outputStream)
    }

    private suspend fun exportPerformance(outputStream: java.io.OutputStream) {
        val allRecords = mutableListOf<PerformanceRecord>()
        repository?.getAllExercises()?.collect { exercises ->
            exercises.forEach { exercise ->
                repository?.getRecordsByExercise(exercise.name, limit = 1000)?.collect { records ->
                    allRecords.addAll(records)
                }
            }
        }
        CsvExporter.exportPerformanceHistory(allRecords, outputStream)
    }
}

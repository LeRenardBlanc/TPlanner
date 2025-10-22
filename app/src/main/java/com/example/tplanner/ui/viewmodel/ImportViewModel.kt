package com.example.tplanner.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tplanner.data.ProgramExercise
import com.example.tplanner.data.TPlannerDatabase
import com.example.tplanner.data.repository.WorkoutRepository
import com.example.tplanner.utils.CsvImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportUiState(
    val exercises: List<ProgramExercise> = emptyList(),
    val errors: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val showSuccessDialog: Boolean = false
)

class ImportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

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

    fun importFromCsv(context: Context, uri: Uri) {
        initRepository(context)
        _uiState.update { it.copy(isLoading = true, errors = emptyList()) }

        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val result = CsvImporter.importFromCsv(inputStream)
                    inputStream.close()

                    _uiState.update {
                        it.copy(
                            exercises = result.exercises,
                            errors = result.errors,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            errors = listOf("Impossible d'ouvrir le fichier"),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errors = listOf("Erreur: ${e.message}"),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun confirmImport() {
        val exercises = _uiState.value.exercises
        if (exercises.isEmpty()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                repository?.let { repo ->
                    // Clear existing program
                    repo.deleteAllExercises()
                    // Insert new program
                    repo.insertExercises(exercises)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showSuccessDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errors = listOf("Erreur lors de la sauvegarde: ${e.message}"),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearImport() {
        _uiState.update {
            it.copy(
                exercises = emptyList(),
                errors = emptyList()
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
    }
}

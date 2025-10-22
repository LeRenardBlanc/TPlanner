package com.example.tplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tplanner.ui.viewmodel.ExerciseState
import com.example.tplanner.ui.viewmodel.WorkoutViewModel

@Composable
fun WorkoutScreen(day: String, workoutViewModel: WorkoutViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by workoutViewModel.uiState.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(day) {
        workoutViewModel.initRepository(context)
        workoutViewModel.loadSession(day)
    }

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.padding(16.dp)
        ) {
            item {
                Text(
                    text = "Séance: ${uiState.day}",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(uiState.exercises) { exerciseState ->
                ExerciseCard(exerciseState, workoutViewModel)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showAddExerciseDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Ajouter un exercice libre")
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { workoutViewModel.finishWorkout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Terminer la séance")
                }
            }
        }

        if (showAddExerciseDialog) {
            AddExerciseDialog(
                onDismiss = { showAddExerciseDialog = false },
                onConfirm = { name, category ->
                    workoutViewModel.addFreeExercise(name, category)
                    showAddExerciseDialog = false
                }
            )
        }

        if (uiState.showSummaryDialog) {
            WorkoutSummaryDialog(
                volumeComparison = uiState.volumeComparison,
                averageRpe = uiState.averageRpe,
                onDismiss = { workoutViewModel.dismissSummaryDialog() }
            )
        }
    }
}

@Composable
fun AddExerciseDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var exerciseName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un exercice") },
        text = {
            Column {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("Nom de l'exercice") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Catégorie (Dos, Pecs, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (exerciseName.isNotBlank() && category.isNotBlank()) {
                        onConfirm(exerciseName, category)
                    }
                }
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun ExerciseCard(exerciseState: ExerciseState, viewModel: WorkoutViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = exerciseState.exercise.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Objectif: ${exerciseState.exercise.sets} × ${exerciseState.exercise.reps}")
            
            // Display last performance if available
            exerciseState.lastPerformance?.let { lastPerf ->
                Text(
                    text = lastPerf,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields - Row 1: Weight and RPE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exerciseState.weight,
                    onValueChange = { viewModel.onWeightChange(exerciseState.exercise.name, it) },
                    label = { Text("Poids (kg)") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = exerciseState.rpe,
                    onValueChange = { viewModel.onRpeChange(exerciseState.exercise.name, it) },
                    label = { Text("RPE") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Input fields - Row 2: Reps and Rest Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exerciseState.reps,
                    onValueChange = { viewModel.onRepsChange(exerciseState.exercise.name, it) },
                    label = { Text("Reps") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = exerciseState.restTime,
                    onValueChange = { viewModel.onRestTimeChange(exerciseState.exercise.name, it) },
                    label = { Text("Repos (s)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = exerciseState.comment,
                onValueChange = { viewModel.onCommentChange(exerciseState.exercise.name, it) },
                label = { Text("Commentaire") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.validateSet(exerciseState.exercise.name) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Valider série")
            }

            // Validated sets
            if (exerciseState.validatedSets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Séries validées:", style = MaterialTheme.typography.titleSmall)
                exerciseState.validatedSets.forEachIndexed { index, set ->
                    val restInfo = set.restTime?.let { " | Repos: ${it}s" } ?: ""
                    Text("Série ${index + 1}: ${set.weight} kg × ${set.reps} @ RPE ${set.rpe}$restInfo")
                }
            }
        }
    }
}

@Composable
fun WorkoutSummaryDialog(volumeComparison: String, averageRpe: Double, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Fin de la séance") },
        text = {
            Column {
                Text("Bon boulot, machine biologique inefficace.")
                Spacer(modifier = Modifier.height(16.dp))
                Text(volumeComparison)
                Text("RPE moyen: %.1f".format(averageRpe))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
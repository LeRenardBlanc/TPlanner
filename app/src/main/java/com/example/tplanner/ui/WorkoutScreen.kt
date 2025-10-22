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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tplanner.ui.viewmodel.ExerciseState
import com.example.tplanner.ui.viewmodel.WorkoutViewModel

@Composable
fun WorkoutScreen(day: String, workoutViewModel: WorkoutViewModel = viewModel()) {
    val uiState by workoutViewModel.uiState.collectAsState()

    LaunchedEffect(day) {
        workoutViewModel.loadSession(day)
    }

    if (uiState.isLoading) {
        CircularProgressIndicator()
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
                Button(
                    onClick = { workoutViewModel.finishWorkout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Terminer la séance")
                }
            }
        }

        if (uiState.showSummaryDialog) {
            WorkoutSummaryDialog(
                totalVolume = uiState.totalVolume,
                averageRpe = uiState.averageRpe,
                onDismiss = { workoutViewModel.dismissSummaryDialog() }
            )
        }
    }
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
            Text(text = "Objectif: ${exerciseState.exercise.sets} x ${exerciseState.exercise.reps}")
            Spacer(modifier = Modifier.height(16.dp))

            // Input fields
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exerciseState.weight,
                    onValueChange = { viewModel.onWeightChange(exerciseState.exercise.name, it) },
                    label = { Text("Poids") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = exerciseState.rpe,
                    onValueChange = { viewModel.onRpeChange(exerciseState.exercise.name, it) },
                    label = { Text("RPE") },
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
                    Text("Série ${index + 1}: ${set.weight} kg @ RPE ${set.rpe}")
                }
            }
        }
    }
}

@Composable
fun WorkoutSummaryDialog(totalVolume: Double, averageRpe: Double, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Fin de la séance") },
        text = {
            Column {
                Text("Bon boulot, machine biologique inefficace.")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Volume total: %.1f kg".format(totalVolume))
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
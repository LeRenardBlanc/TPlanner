package com.example.tplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tplanner.data.DummyData
import com.example.tplanner.data.ProgramExercise
import com.example.tplanner.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    homeViewModel.initRepository(context)

    val programExercises by homeViewModel.programExercises.collectAsState()
    val weekStats by homeViewModel.weekStats.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.Import.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Import program")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Section: Séances de la semaine
            item {
                Text(
                    text = "Séances de la semaine",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            if (programExercises.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Aucun programme importé",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Appuyez sur + pour importer un programme CSV",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            } else {
                items(programExercises.groupBy { it.day }.entries.toList()) { (day, exercises) ->
                    WorkoutSessionCard(day, exercises, navController)
                }
            }

            // Section: Progression globale
            item {
                Text(
                    text = "Progression globale",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                PlaceholderCard(text = "Indice de progression: ${weekStats.progressIndex}%")
            }

            // Section: Statistiques rapides
            item {
                Text(
                    text = "Statistiques rapides",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                PlaceholderCard(text = "Volume total: ${weekStats.totalVolume} kg")
            }
        }
    }
}

@Composable
fun WorkoutSessionCard(day: String, exercises: List<ProgramExercise>, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate(Screen.WorkoutSession.createRoute(day)) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = day, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                exercises.forEach { exercise ->
                    Text(
                        text = "- ${exercise.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
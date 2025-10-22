package com.example.tplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tplanner.ui.viewmodel.ProgressViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun ProgressScreen(progressViewModel: ProgressViewModel = viewModel()) {
    val context = LocalContext.current
    progressViewModel.initRepository(context)

    val exercises by progressViewModel.exercises.collectAsState()
    val stats by progressViewModel.stats.collectAsState()
    val categoryStats by progressViewModel.categoryStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Exercices", "Groupes musculaires", "Statistiques")

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Progression",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ExerciseProgressTab(exercises, progressViewModel)
            1 -> MuscleGroupTab(categoryStats)
            2 -> StatisticsTab(stats)
        }
    }
}

@Composable
fun ExerciseProgressTab(exercises: List<String>, viewModel: ProgressViewModel) {
    if (exercises.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = "Aucune donnée disponible",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Complétez quelques séances pour voir vos progressions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises) { exerciseName ->
                ExerciseProgressCard(exerciseName, viewModel)
            }
        }
    }
}

@Composable
fun ExerciseProgressCard(exerciseName: String, viewModel: ProgressViewModel) {
    val progressData by viewModel.getExerciseProgress(exerciseName).collectAsState(initial = emptyList())

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.titleMedium
            )

            if (progressData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                // Display recent PR
                val maxWeight = progressData.maxByOrNull { it.weight }
                maxWeight?.let {
                    Text(
                        text = "PR: ${it.weight} kg × ${it.reps} reps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Calculate estimated 1RM
                maxWeight?.let {
                    val estimated1RM = viewModel.calculateEstimated1RM(it.weight, it.reps)
                    Text(
                        text = "1RM estimé: ${String.format("%.1f", estimated1RM)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simple chart
                if (progressData.size > 1) {
                    Text(
                        text = "Évolution du poids",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val modelProducer = remember { CartesianChartModelProducer.build() }
                    modelProducer.tryRunTransaction {
                        lineSeries {
                            series(progressData.map { it.weight })
                        }
                    }

                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            } else {
                Text(
                    text = "Pas encore de données",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MuscleGroupTab(categoryStats: Map<String, Double>) {
    if (categoryStats.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = "Aucune donnée disponible",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryStats.entries.toList()) { (category, volume) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${String.format("%.1f", volume)} kg",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsTab(stats: Map<String, String>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stats.entries.toList()) { (label, value) ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
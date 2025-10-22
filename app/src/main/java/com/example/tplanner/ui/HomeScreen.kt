package com.example.tplanner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tplanner.data.DummyData
import com.example.tplanner.data.WorkoutSession

@Composable
fun HomeScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Section: Séances de la semaine
        item {
            Text(
                text = "Séances de la semaine",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
        }
        items(DummyData.sessions) { session ->
            WorkoutSessionCard(session, navController)
        }

        // Section: Progression globale
        item {
            Text(
                text = "Progression globale",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            PlaceholderCard(text = "Indice de progression: +5.2%")
        }

        // Section: Statistiques rapides
        item {
            Text(
                text = "Statistiques rapides",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            PlaceholderCard(text = "Graphique du volume total")
        }
    }
}

@Composable
fun WorkoutSessionCard(session: WorkoutSession, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { navController.navigate(Screen.WorkoutSession.createRoute(session.day)) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = session.day, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                session.exercises.forEach { exercise ->
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
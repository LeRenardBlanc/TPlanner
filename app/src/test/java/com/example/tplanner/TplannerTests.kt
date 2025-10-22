package com.example.tplanner

import com.example.tplanner.utils.CsvImporter
import com.example.tplanner.utils.ProgressCalculator
import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.SessionHistory
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream

class CsvImportTest {

    @Test
    fun testCsvImportParsing() {
        val csvContent = """
            Jour,Exercice,Séries,Reps,Poids,RPE,Catégorie,Commentaire
            Mercredi,Tirage vertical,4,8-10,59,8,Dos,
            Samedi,Développé décliné,4,8-10,55,7,Pecs,Bas des pecs
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray())
        val result = CsvImporter.importFromCsv(inputStream)

        assertEquals(2, result.exercises.size)
        assertEquals(0, result.errors.size)

        val firstExercise = result.exercises[0]
        assertEquals("Mercredi", firstExercise.day)
        assertEquals("Tirage vertical", firstExercise.name)
        assertEquals(4, firstExercise.sets)
        assertEquals("8-10", firstExercise.reps)
        assertEquals(59.0, firstExercise.targetWeight, 0.01)
        assertEquals(8, firstExercise.targetRpe)
        assertEquals("Dos", firstExercise.category)

        val secondExercise = result.exercises[1]
        assertEquals("Samedi", secondExercise.day)
        assertEquals("Bas des pecs", secondExercise.comment)
    }

    @Test
    fun testCsvImportWithMissingData() {
        val csvContent = """
            Jour,Exercice,Séries,Reps,Poids,RPE,Catégorie,Commentaire
            Lundi,Squat,invalid,10,80,8,Jambes,
        """.trimIndent()

        val inputStream = ByteArrayInputStream(csvContent.toByteArray())
        val result = CsvImporter.importFromCsv(inputStream)

        // Should use default value for invalid sets
        assertEquals(1, result.exercises.size)
        assertEquals(3, result.exercises[0].sets) // Default value
    }
}

class ProgressCalculatorTest {

    @Test
    fun testVolumeCalculation() {
        val record = PerformanceRecord(
            sessionId = 1,
            exerciseName = "Squat",
            sets = 4,
            reps = 10,
            weight = 100.0,
            rpe = 8,
            category = "Jambes"
        )

        val volume = ProgressCalculator.calculateVolume(record)
        assertEquals(4000.0, volume, 0.01) // 100kg × 10 reps × 4 sets
    }

    @Test
    fun testProgressiveOverloadIndex() {
        val currentWeek = listOf(
            SessionHistory(day = "Lundi", totalVolume = 5000.0),
            SessionHistory(day = "Mercredi", totalVolume = 4500.0)
        )

        val previousWeek = listOf(
            SessionHistory(day = "Lundi", totalVolume = 4500.0),
            SessionHistory(day = "Mercredi", totalVolume = 4000.0)
        )

        val index = ProgressCalculator.calculateProgressiveOverloadIndex(currentWeek, previousWeek)
        
        // Current total: 9500, Previous total: 8500
        // Expected: (9500 / 8500 - 1) * 100 = 11.76%
        assertEquals(11.76, index, 0.1)
    }

    @Test
    fun testEstimate1RMEpley() {
        val estimated1RM = ProgressCalculator.estimate1RMEpley(100.0, 10)
        // Formula: 100 × (1 + 10/30) = 100 × 1.333 = 133.3
        assertEquals(133.3, estimated1RM, 0.1)
    }

    @Test
    fun testEstimate1RMBrzycki() {
        val estimated1RM = ProgressCalculator.estimate1RMBrzycki(100.0, 10)
        // Formula: 100 × (36 / (37 - 10)) = 100 × (36 / 27) = 133.3
        assertEquals(133.3, estimated1RM, 0.1)
    }

    @Test
    fun testAverageRpe() {
        val records = listOf(
            PerformanceRecord(sessionId = 1, exerciseName = "Test", sets = 1, reps = 10, weight = 100.0, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 1, exerciseName = "Test", sets = 1, reps = 10, weight = 100.0, rpe = 9, category = "Test"),
            PerformanceRecord(sessionId = 1, exerciseName = "Test", sets = 1, reps = 10, weight = 100.0, rpe = 7, category = "Test")
        )

        val averageRpe = ProgressCalculator.calculateAverageRpe(records)
        assertEquals(8.0, averageRpe, 0.01)
    }

    @Test
    fun testStagnationDetection() {
        // Create records with minimal progression (< 3%)
        val records = listOf(
            PerformanceRecord(sessionId = 1, exerciseName = "Test", sets = 1, reps = 10, weight = 100.0, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 2, exerciseName = "Test", sets = 1, reps = 10, weight = 101.0, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 3, exerciseName = "Test", sets = 1, reps = 10, weight = 101.5, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 4, exerciseName = "Test", sets = 1, reps = 10, weight = 102.0, rpe = 8, category = "Test")
        )

        val hasStagnation = ProgressCalculator.detectStagnation(records, threshold = 3.0)
        assertTrue(hasStagnation)
    }

    @Test
    fun testNoStagnationWithGoodProgression() {
        // Create records with good progression (> 3%)
        val records = listOf(
            PerformanceRecord(sessionId = 1, exerciseName = "Test", sets = 1, reps = 10, weight = 100.0, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 2, exerciseName = "Test", sets = 1, reps = 10, weight = 105.0, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 3, exerciseName = "Test", sets = 1, reps = 10, weight = 110.0, rpe = 8, category = "Test"),
            PerformanceRecord(sessionId = 4, exerciseName = "Test", sets = 1, reps = 10, weight = 115.0, rpe = 8, category = "Test")
        )

        val hasStagnation = ProgressCalculator.detectStagnation(records, threshold = 3.0)
        assertFalse(hasStagnation)
    }

    @Test
    fun testVolumeByCategory() {
        val records = listOf(
            PerformanceRecord(sessionId = 1, exerciseName = "Squat", sets = 1, reps = 10, weight = 100.0, rpe = 8, category = "Jambes"),
            PerformanceRecord(sessionId = 1, exerciseName = "Leg Press", sets = 1, reps = 10, weight = 200.0, rpe = 8, category = "Jambes"),
            PerformanceRecord(sessionId = 1, exerciseName = "Bench Press", sets = 1, reps = 10, weight = 80.0, rpe = 8, category = "Pecs")
        )

        val volumeByCategory = ProgressCalculator.calculateVolumeByCategory(records)
        assertEquals(3000.0, volumeByCategory["Jambes"], 0.01) // (100×10) + (200×10)
        assertEquals(800.0, volumeByCategory["Pecs"], 0.01) // 80×10
    }
}

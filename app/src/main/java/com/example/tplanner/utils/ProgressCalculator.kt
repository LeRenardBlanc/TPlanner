package com.example.tplanner.utils

import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.SessionHistory

object ProgressCalculator {
    /**
     * Calculate progressive overload index between two sessions
     * Formula: (Volume week n) / (Volume week n-1)
     */
    fun calculateProgressiveOverloadIndex(
        currentWeekSessions: List<SessionHistory>,
        previousWeekSessions: List<SessionHistory>
    ): Double {
        val currentVolume = currentWeekSessions.sumOf { it.totalVolume }
        val previousVolume = previousWeekSessions.sumOf { it.totalVolume }
        
        return if (previousVolume > 0) {
            (currentVolume / previousVolume - 1.0) * 100.0
        } else {
            0.0
        }
    }

    /**
     * Calculate volume for a single performance record
     * Volume = Weight × Reps × Sets
     */
    fun calculateVolume(record: PerformanceRecord): Double {
        return record.weight * record.reps * record.sets
    }

    /**
     * Calculate total volume for a list of records
     */
    fun calculateTotalVolume(records: List<PerformanceRecord>): Double {
        return records.sumOf { calculateVolume(it) }
    }

    /**
     * Calculate average RPE for a list of records
     */
    fun calculateAverageRpe(records: List<PerformanceRecord>): Double {
        if (records.isEmpty()) return 0.0
        return records.map { it.rpe }.average()
    }

    /**
     * Estimate 1RM using Epley formula
     * 1RM = weight × (1 + reps / 30)
     */
    fun estimate1RMEpley(weight: Double, reps: Int): Double {
        return weight * (1 + reps / 30.0)
    }

    /**
     * Estimate 1RM using Brzycki formula
     * 1RM = weight × (36 / (37 - reps))
     */
    fun estimate1RMBrzycki(weight: Double, reps: Int): Double {
        return if (reps >= 37) weight else weight * (36.0 / (37.0 - reps))
    }

    /**
     * Detect stagnation in exercise progression
     * Returns true if progression is < 3% over the last 3 sessions
     */
    fun detectStagnation(records: List<PerformanceRecord>, threshold: Double = 3.0): Boolean {
        if (records.size < 4) return false

        // Take last 4 records to compare last 3 sessions
        val recentRecords = records.takeLast(4)
        val volumes = recentRecords.map { calculateVolume(it) }

        // Calculate average change between consecutive sessions
        var totalChange = 0.0
        for (i in 1 until volumes.size) {
            val change = if (volumes[i - 1] > 0) {
                ((volumes[i] - volumes[i - 1]) / volumes[i - 1]) * 100.0
            } else {
                0.0
            }
            totalChange += change
        }

        val averageChange = totalChange / (volumes.size - 1)
        return averageChange < threshold
    }

    /**
     * Calculate volume by muscle group
     */
    fun calculateVolumeByCategory(records: List<PerformanceRecord>): Map<String, Double> {
        return records.groupBy { it.category }
            .mapValues { (_, categoryRecords) -> calculateTotalVolume(categoryRecords) }
    }

    /**
     * Find personal records (highest weight for each exercise)
     */
    fun findPersonalRecords(records: List<PerformanceRecord>): Map<String, PerformanceRecord> {
        return records.groupBy { it.exerciseName }
            .mapValues { (_, exerciseRecords) ->
                exerciseRecords.maxByOrNull { it.weight } ?: exerciseRecords.first()
            }
    }
}

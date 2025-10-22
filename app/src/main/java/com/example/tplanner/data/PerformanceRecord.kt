package com.example.tplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "performance_records")
data class PerformanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val weight: Double,
    val rpe: Int,
    val restTime: Int? = null, // in seconds
    val comment: String? = null,
    val timestamp: Long = Date().time,
    val category: String
)

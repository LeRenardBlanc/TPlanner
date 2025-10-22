package com.example.tplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "program_exercises")
data class ProgramExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val day: String,
    val name: String,
    val sets: Int,
    val reps: String, // Can be a range like "8-10"
    val targetWeight: Double = 0.0,
    val targetRpe: Int = 8,
    val category: String,
    val comment: String? = null,
    val orderIndex: Int = 0
)

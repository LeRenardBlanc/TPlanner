package com.example.tplanner.data

data class Exercise(
    val day: String,
    val name: String,
    val sets: Int,
    val reps: String, // Can be a range like "8-10"
    val weight: Double,
    val rpe: Int,
    val category: String,
    val comment: String? = null
)
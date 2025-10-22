package com.example.tplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "session_history")
data class SessionHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val day: String,
    val timestamp: Long = Date().time,
    val totalVolume: Double = 0.0,
    val averageRpe: Double = 0.0,
    val duration: Int? = null, // in minutes
    val completed: Boolean = false
)

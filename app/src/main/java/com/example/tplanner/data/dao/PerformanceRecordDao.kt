package com.example.tplanner.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tplanner.data.PerformanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceRecordDao {
    @Insert
    suspend fun insert(record: PerformanceRecord): Long

    @Insert
    suspend fun insertAll(records: List<PerformanceRecord>)

    @Query("SELECT * FROM performance_records WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getRecordsBySession(sessionId: Long): Flow<List<PerformanceRecord>>

    @Query("SELECT * FROM performance_records WHERE exerciseName = :exerciseName ORDER BY timestamp DESC LIMIT :limit")
    fun getRecordsByExercise(exerciseName: String, limit: Int = 50): Flow<List<PerformanceRecord>>

    @Query("SELECT * FROM performance_records WHERE exerciseName = :exerciseName ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastRecordForExercise(exerciseName: String): PerformanceRecord?

    @Query("SELECT * FROM performance_records WHERE category = :category ORDER BY timestamp DESC")
    fun getRecordsByCategory(category: String): Flow<List<PerformanceRecord>>

    @Query("DELETE FROM performance_records WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: Long)
}

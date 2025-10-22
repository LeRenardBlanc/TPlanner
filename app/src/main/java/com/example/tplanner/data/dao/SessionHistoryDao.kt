package com.example.tplanner.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tplanner.data.SessionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionHistoryDao {
    @Insert
    suspend fun insert(session: SessionHistory): Long

    @Update
    suspend fun update(session: SessionHistory)

    @Query("SELECT * FROM session_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionHistory>>

    @Query("SELECT * FROM session_history WHERE day = :day ORDER BY timestamp DESC LIMIT :limit")
    fun getSessionsByDay(day: String, limit: Int = 10): Flow<List<SessionHistory>>

    @Query("SELECT * FROM session_history WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionHistory?

    @Query("SELECT * FROM session_history WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<SessionHistory>>

    @Query("DELETE FROM session_history WHERE id = :id")
    suspend fun deleteById(id: Long)
}

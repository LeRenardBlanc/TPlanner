package com.example.tplanner.data.repository

import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.ProgramExercise
import com.example.tplanner.data.SessionHistory
import com.example.tplanner.data.dao.PerformanceRecordDao
import com.example.tplanner.data.dao.ProgramExerciseDao
import com.example.tplanner.data.dao.SessionHistoryDao
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val programExerciseDao: ProgramExerciseDao,
    private val sessionHistoryDao: SessionHistoryDao,
    private val performanceRecordDao: PerformanceRecordDao
) {
    // Program Exercises
    fun getAllExercises(): Flow<List<ProgramExercise>> = programExerciseDao.getAllExercises()

    fun getExercisesByDay(day: String): Flow<List<ProgramExercise>> = programExerciseDao.getExercisesByDay(day)

    fun getAllDays(): Flow<List<String>> = programExerciseDao.getAllDays()

    suspend fun insertExercises(exercises: List<ProgramExercise>) = programExerciseDao.insertAll(exercises)

    suspend fun deleteAllExercises() = programExerciseDao.deleteAll()

    suspend fun getExerciseByName(name: String): ProgramExercise? = programExerciseDao.getExerciseByName(name)

    // Session History
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionHistory>> = sessionHistoryDao.getRecentSessions(limit)

    fun getSessionsByDay(day: String, limit: Int = 10): Flow<List<SessionHistory>> = sessionHistoryDao.getSessionsByDay(day, limit)

    suspend fun getSessionById(id: Long): SessionHistory? = sessionHistoryDao.getSessionById(id)

    suspend fun insertSession(session: SessionHistory): Long = sessionHistoryDao.insert(session)

    suspend fun updateSession(session: SessionHistory) = sessionHistoryDao.update(session)

    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<SessionHistory>> = 
        sessionHistoryDao.getSessionsInRange(startTime, endTime)

    // Performance Records
    fun getRecordsBySession(sessionId: Long): Flow<List<PerformanceRecord>> = performanceRecordDao.getRecordsBySession(sessionId)

    fun getRecordsByExercise(exerciseName: String, limit: Int = 50): Flow<List<PerformanceRecord>> = 
        performanceRecordDao.getRecordsByExercise(exerciseName, limit)

    suspend fun getLastRecordForExercise(exerciseName: String): PerformanceRecord? = 
        performanceRecordDao.getLastRecordForExercise(exerciseName)

    fun getRecordsByCategory(category: String): Flow<List<PerformanceRecord>> = 
        performanceRecordDao.getRecordsByCategory(category)

    suspend fun insertPerformanceRecord(record: PerformanceRecord): Long = performanceRecordDao.insert(record)

    suspend fun insertPerformanceRecords(records: List<PerformanceRecord>) = performanceRecordDao.insertAll(records)
}

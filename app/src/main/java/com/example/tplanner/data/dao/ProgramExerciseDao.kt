package com.example.tplanner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tplanner.data.ProgramExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgramExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ProgramExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ProgramExercise>)

    @Update
    suspend fun update(exercise: ProgramExercise)

    @Delete
    suspend fun delete(exercise: ProgramExercise)

    @Query("SELECT * FROM program_exercises ORDER BY day, orderIndex")
    fun getAllExercises(): Flow<List<ProgramExercise>>

    @Query("SELECT * FROM program_exercises WHERE day = :day ORDER BY orderIndex")
    fun getExercisesByDay(day: String): Flow<List<ProgramExercise>>

    @Query("SELECT DISTINCT day FROM program_exercises ORDER BY orderIndex")
    fun getAllDays(): Flow<List<String>>

    @Query("DELETE FROM program_exercises")
    suspend fun deleteAll()

    @Query("SELECT * FROM program_exercises WHERE name = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): ProgramExercise?
}

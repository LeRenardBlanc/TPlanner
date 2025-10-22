package com.example.tplanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.tplanner.data.dao.PerformanceRecordDao
import com.example.tplanner.data.dao.ProgramExerciseDao
import com.example.tplanner.data.dao.SessionHistoryDao

@Database(
    entities = [
        ProgramExercise::class,
        SessionHistory::class,
        PerformanceRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TPlannerDatabase : RoomDatabase() {
    abstract fun programExerciseDao(): ProgramExerciseDao
    abstract fun sessionHistoryDao(): SessionHistoryDao
    abstract fun performanceRecordDao(): PerformanceRecordDao

    companion object {
        @Volatile
        private var INSTANCE: TPlannerDatabase? = null

        fun getDatabase(context: Context): TPlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TPlannerDatabase::class.java,
                    "tplanner_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

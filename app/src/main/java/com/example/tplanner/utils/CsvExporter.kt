package com.example.tplanner.utils

import com.example.tplanner.data.PerformanceRecord
import com.example.tplanner.data.ProgramExercise
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun exportProgram(exercises: List<ProgramExercise>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        val csvPrinter = CSVPrinter(
            writer,
            CSVFormat.DEFAULT.builder()
                .setHeader("Jour", "Exercice", "Séries", "Reps", "Poids", "RPE", "Catégorie", "Commentaire")
                .build()
        )

        exercises.forEach { exercise ->
            csvPrinter.printRecord(
                exercise.day,
                exercise.name,
                exercise.sets,
                exercise.reps,
                exercise.targetWeight,
                exercise.targetRpe,
                exercise.category,
                exercise.comment ?: ""
            )
        }

        csvPrinter.flush()
        csvPrinter.close()
    }

    fun exportPerformanceHistory(records: List<PerformanceRecord>, outputStream: OutputStream) {
        val writer = OutputStreamWriter(outputStream)
        val csvPrinter = CSVPrinter(
            writer,
            CSVFormat.DEFAULT.builder()
                .setHeader("Date", "Exercice", "Séries", "Reps", "Poids", "RPE", "Catégorie", "Repos (s)", "Commentaire")
                .build()
        )

        records.forEach { record ->
            csvPrinter.printRecord(
                dateFormat.format(Date(record.timestamp)),
                record.exerciseName,
                record.sets,
                record.reps,
                record.weight,
                record.rpe,
                record.category,
                record.restTime ?: "",
                record.comment ?: ""
            )
        }

        csvPrinter.flush()
        csvPrinter.close()
    }
}

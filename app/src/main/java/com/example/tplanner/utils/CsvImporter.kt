package com.example.tplanner.utils

import com.example.tplanner.data.ProgramExercise
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.InputStream
import java.io.InputStreamReader

object CsvImporter {
    data class ImportResult(
        val exercises: List<ProgramExercise>,
        val errors: List<String>
    )

    fun importFromCsv(inputStream: InputStream): ImportResult {
        val exercises = mutableListOf<ProgramExercise>()
        val errors = mutableListOf<String>()

        try {
            val reader = InputStreamReader(inputStream)
            val csvParser = CSVParser.parse(
                reader,
                CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
            )

            var orderIndex = 0
            csvParser.forEach { record ->
                try {
                    val day = record.get("Jour")
                    val name = record.get("Exercice")
                    val sets = record.get("Séries").toIntOrNull() ?: 3
                    val reps = record.get("Reps")
                    val weight = record.get("Poids").toDoubleOrNull() ?: 0.0
                    val rpe = record.get("RPE").toIntOrNull() ?: 8
                    val category = record.get("Catégorie")
                    val comment = record.get("Commentaire")?.takeIf { it.isNotBlank() }

                    exercises.add(
                        ProgramExercise(
                            day = day,
                            name = name,
                            sets = sets,
                            reps = reps,
                            targetWeight = weight,
                            targetRpe = rpe,
                            category = category,
                            comment = comment,
                            orderIndex = orderIndex++
                        )
                    )
                } catch (e: Exception) {
                    errors.add("Error parsing row ${record.recordNumber}: ${e.message}")
                }
            }

            csvParser.close()
        } catch (e: Exception) {
            errors.add("Error reading CSV file: ${e.message}")
        }

        return ImportResult(exercises, errors)
    }
}

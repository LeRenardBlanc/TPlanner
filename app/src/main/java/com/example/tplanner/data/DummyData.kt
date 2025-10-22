package com.example.tplanner.data

object DummyData {
    val sessions = listOf(
        WorkoutSession(
            day = "Mercredi",
            exercises = listOf(
                Exercise(day="Mercredi", name="Tirage vertical prise neutre", sets=4, reps="8-10", weight=59.0, rpe=8, category="Dos"),
                Exercise(day="Mercredi", name="Rowing haltère", sets=3, reps="10-12", weight=22.0, rpe=8, category="Dos"),
            )
        ),
        WorkoutSession(
            day = "Samedi",
            exercises = listOf(
                Exercise(day="Samedi", name="Développé décliné haltères", sets=4, reps="8-10", weight=55.0, rpe=7, category="Pecs", comment="Bas des pecs"),
                Exercise(day="Samedi", name="Dips", sets=4, reps="10-12", weight=0.0, rpe=9, category="Pecs"),
            )
        )
    )
}
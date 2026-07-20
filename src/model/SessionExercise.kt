package model

data class SessionExercise(
    val exerciseId: Int,
    val exerciseName: String,
    val sets: MutableList<ExerciseSet>
)

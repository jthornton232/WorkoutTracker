package model

data class ExerciseSet(
    var weight: Double?,
    var reps: Int?,
    var durationSeconds: Int?,
    var isWarmup: Boolean = false,
    var completed: Boolean = false
)

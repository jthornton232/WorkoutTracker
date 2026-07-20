package data

import service.WorkoutTracker

fun addSampleData(tracker: WorkoutTracker) {
    val benchPress = tracker.createExercise("Bench Press")
    val overheadPress = tracker.createExercise("Overhead Press")
    val squat = tracker.createExercise("Squat")
    val deadlift = tracker.createExercise("Deadlift")

    tracker.createTemplate(
        name = "Push Day",
        exerciseIds = mutableListOf(
            benchPress.id,
            overheadPress.id
        )
    )

    tracker.createTemplate(
        name = "Full Body",
        exerciseIds = mutableListOf(
            squat.id,
            benchPress.id,
            deadlift.id
        )
    )
}
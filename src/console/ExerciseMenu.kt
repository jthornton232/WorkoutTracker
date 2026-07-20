package console

import service.WorkoutTracker
import model.Exercise

fun manageExercises(tracker: WorkoutTracker) {
    while (true) {
        println()
        println("Exercises")
        println("=========")

        if (tracker.exercises.isEmpty()) {
            println("No exercises created.")
        } else {
            tracker.exercises
                .sortedBy { it.name.lowercase() }
                .forEachIndexed { index, exercise ->
                    println("${index + 1}. ${exercise.name}")
                }
        }

        println()
        println("1. Add exercise")
        println("0. Back")

        when (readInt("Choice: ", 0..1)) {
            0 -> return

            1 -> {
                val name = readRequiredString("Exercise name: ")
                val previousCount = tracker.exercises.size

                val exercise = tracker.createExercise(name)

                if (tracker.exercises.size == previousCount) {
                    println("${exercise.name} already exists.")
                } else {
                    println("${exercise.name} added.")
                }
            }
        }
    }
}

fun chooseExistingExercise(
    tracker: WorkoutTracker
): Exercise? {
    if (tracker.exercises.isEmpty()) {
        println("No exercises exist.")
        return null
    }

    val sortedExercises = tracker.exercises
        .sortedBy { it.name.lowercase() }

    println()

    sortedExercises.forEachIndexed { index, exercise ->
        println("${index + 1}. ${exercise.name}")
    }

    println("0. Cancel")

    val choice = readInt(
        prompt = "Exercise: ",
        validRange = 0..sortedExercises.size
    )

    return if (choice == 0) {
        null
    } else {
        sortedExercises[choice - 1]
    }
}
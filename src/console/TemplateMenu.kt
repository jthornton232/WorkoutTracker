package console

import service.WorkoutTracker

fun createWorkoutTemplate(tracker: WorkoutTracker) {
    val name = readRequiredString("Template name: ")
    val selectedExerciseIds = mutableListOf<Int>()

    while (true) {
        println()
        println("Template: $name")
        println("=".repeat("Template: $name".length))

        if (selectedExerciseIds.isEmpty()) {
            println("No exercises added.")
        } else {
            selectedExerciseIds.forEachIndexed { index, id ->
                val exerciseName = tracker.exercises
                    .find { it.id == id }
                    ?.name
                    ?: "Unknown exercise"

                println("${index + 1}. $exerciseName")
            }
        }

        println()
        println("1. Add existing exercise")
        println("2. Create and add exercise")
        println("3. Remove exercise")
        println("4. Save template")
        println("0. Cancel")

        when (readInt("Choice: ", 0..4)) {
            0 -> return

            1 -> {
                val exercise = chooseExistingExercise(tracker)

                if (exercise != null) {
                    if (exercise.id in selectedExerciseIds) {
                        println("That exercise is already in the template.")
                    } else {
                        selectedExerciseIds.add(exercise.id)
                    }
                }
            }

            2 -> {
                val exercise = tracker.createExercise(
                    readRequiredString("Exercise name: ")
                )

                if (exercise.id in selectedExerciseIds) {
                    println("That exercise is already in the template.")
                } else {
                    selectedExerciseIds.add(exercise.id)
                }
            }

            3 -> {
                removeExerciseFromTemplate(
                    tracker = tracker,
                    selectedExerciseIds = selectedExerciseIds
                )
            }

            4 -> {
                if (selectedExerciseIds.isEmpty()) {
                    println("A template must contain at least one exercise.")
                    continue
                }

                tracker.createTemplate(
                    name = name,
                    exerciseIds = selectedExerciseIds.toMutableList()
                )

                println("Template saved.")
                return
            }
        }
    }
}

fun removeExerciseFromTemplate(
    tracker: WorkoutTracker,
    selectedExerciseIds: MutableList<Int>
) {
    if (selectedExerciseIds.isEmpty()) {
        println("There are no exercises to remove.")
        return
    }

    println()

    selectedExerciseIds.forEachIndexed { index, exerciseId ->
        val name = tracker.exercises
            .find { it.id == exerciseId }
            ?.name
            ?: "Unknown exercise"

        println("${index + 1}. $name")
    }

    println("0. Cancel")

    val choice = readInt(
        prompt = "Exercise to remove: ",
        validRange = 0..selectedExerciseIds.size
    )

    if (choice != 0) {
        selectedExerciseIds.removeAt(choice - 1)
    }
}

fun viewTemplates(tracker: WorkoutTracker) {
    println()
    println("Workout Templates")
    println("=================")

    if (tracker.templates.isEmpty()) {
        println("No templates created.")
        return
    }

    tracker.templates.forEach { template ->
        println()
        println(template.name)

        template.exerciseIds.forEachIndexed { index, exerciseId ->
            val exerciseName = tracker.exercises
                .find { it.id == exerciseId }
                ?.name
                ?: "Unknown exercise"

            println("  ${index + 1}. $exerciseName")
        }
    }
}
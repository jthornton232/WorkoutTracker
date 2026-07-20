package console

import service.WorkoutTracker

fun runMainMenu(tracker: WorkoutTracker) {
    while (true) {
        println()
        println("Workout Tracker")
        println("================")
        println("1. Start workout")
        println("2. Create workout template")
        println("3. Manage exercises")
        println("4. View templates")
        println("5. View history")
        println("6. Exit")

        when (readInt("Choice: ", 1..6)) {
            1 -> startWorkout(tracker)
            2 -> createWorkoutTemplate(tracker)
            3 -> manageExercises(tracker)
            4 -> viewTemplates(tracker)
            5 -> viewHistory(tracker)
            6 -> return
        }
    }
}

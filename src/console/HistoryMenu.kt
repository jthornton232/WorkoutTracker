package console

import util.formatSet
import model.WorkoutSession
import service.WorkoutTracker
import java.time.Duration
import java.time.format.DateTimeFormatter

fun viewHistory(tracker: WorkoutTracker) {
    println()
    println("History")
    println("=======")
    println("1. All workouts")
    println("2. History by workout name")
    println("3. History by exercise")
    println("0. Back")

    when (readInt("Choice: ", 0..3)) {
        0 -> return
        1 -> viewAllWorkouts(tracker)
        2 -> viewHistoryByWorkoutName(tracker)
        3 -> viewHistoryByExercise(tracker)
    }
}

fun viewAllWorkouts(tracker: WorkoutTracker) {
    val completedSessions = tracker.sessions
        .filter { it.isCompleted }
        .sortedByDescending { it.finishedAt }

    if (completedSessions.isEmpty()) {
        println("No completed workouts.")
        return
    }

    completedSessions.forEach(::printCompletedWorkout)
}

fun viewHistoryByWorkoutName(tracker: WorkoutTracker) {
    val names = tracker.sessions
        .filter { it.isCompleted }
        .map { it.name }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }

    if (names.isEmpty()) {
        println("No completed workouts.")
        return
    }

    println()

    names.forEachIndexed { index, name ->
        println("${index + 1}. $name")
    }

    println("0. Cancel")

    val choice = readInt(
        prompt = "Workout: ",
        validRange = 0..names.size
    )

    if (choice == 0) {
        return
    }

    val selectedName = names[choice - 1]

    tracker.sessions
        .filter {
            it.isCompleted &&
                    it.name.equals(selectedName, ignoreCase = true)
        }
        .sortedByDescending { it.finishedAt }
        .forEach(::printCompletedWorkout)
}

fun viewHistoryByExercise(tracker: WorkoutTracker) {
    val exercise = chooseExistingExercise(tracker) ?: return

    val results = tracker.sessions
        .filter { it.isCompleted }
        .sortedByDescending { it.finishedAt }
        .mapNotNull { session ->
            val performedExercise = session.exercises.find {
                it.exerciseId == exercise.id
            }

            if (performedExercise == null) {
                null
            } else {
                session to performedExercise
            }
        }

    if (results.isEmpty()) {
        println("No history for ${exercise.name}.")
        return
    }

    println()
    println("${exercise.name} History")
    println("=".repeat("${exercise.name} History".length))

    results.forEach { (session, performedExercise) ->
        println()
        println(
            "${formatSessionDate(session)} — ${session.name}"
        )

        performedExercise.sets.forEachIndexed { index, set ->
            println("  Set ${index + 1}: ${formatSet(set)}")
        }
    }
}

fun printCompletedWorkout(session: WorkoutSession) {
    println()
    println("${session.name}")
    println("-".repeat(session.name.length))
    println("Finished: ${formatSessionDate(session)}")
    println("Duration: ${formatWorkoutDuration(session)}")

    if (session.exercises.isEmpty()) {
        println("No exercises recorded.")
        return
    }

    session.exercises.forEach { exercise ->
        println()
        println("  ${exercise.exerciseName}")

        if (exercise.sets.isEmpty()) {
            println("    No sets recorded.")
        } else {
            exercise.sets.forEachIndexed { index, set ->
                println("    Set ${index + 1}: ${formatSet(set)}")
            }
        }
    }
}

fun formatSessionDate(session: WorkoutSession): String {
    val date = session.finishedAt ?: session.startedAt
    return date.format(displayDateFormat)
}

fun formatWorkoutDuration(session: WorkoutSession): String {
    val finishTime = session.finishedAt ?: return "In progress"

    val duration = Duration.between(
        session.startedAt,
        finishTime
    )

    val totalMinutes = duration.toMinutes()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}
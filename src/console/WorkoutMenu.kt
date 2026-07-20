package console

import util.formatSet
import service.WorkoutTracker
import model.SessionExercise
import model.Exercise
import model.ExerciseSet
import model.WorkoutSession
import java.time.LocalDateTime

fun startWorkout(tracker: WorkoutTracker) {
    println()
    println("Start Workout")
    println("=============")
    println("1. Choose a template")
    println("2. Start an empty workout")
    println("0. Cancel")

    when (readInt("Choice: ", 0..2)) {
        0 -> return
        1 -> startWorkoutFromTemplate(tracker)
        2 -> startEmptyWorkout(tracker)
    }
}

fun startWorkoutFromTemplate(tracker: WorkoutTracker) {
    if (tracker.templates.isEmpty()) {
        println("No workout templates exist.")
        return
    }

    println()
    println("Choose a template:")

    tracker.templates.forEachIndexed { index, template ->
        println("${index + 1}. ${template.name}")
    }

    println("0. Cancel")

    val choice = readInt(
        prompt = "Choice: ",
        validRange = 0..tracker.templates.size
    )

    if (choice == 0) {
        return
    }

    val template = tracker.templates[choice - 1]

    val sessionExercises = template.exerciseIds
        .mapNotNull { exerciseId ->
            val exercise = tracker.exercises.find {
                it.id == exerciseId
            }

            exercise?.let {
                createSessionExercise(tracker, it)
            }
        }
        .toMutableList()

    val session = tracker.createSession(
        name = template.name,
        templateId = template.id,
        sessionExercises = sessionExercises
    )

    runActiveWorkout(tracker, session)
}

fun startEmptyWorkout(tracker: WorkoutTracker) {
    val name = readRequiredString("Workout name: ")

    val session = tracker.createSession(
        name = name,
        templateId = null,
        sessionExercises = mutableListOf()
    )

    runActiveWorkout(tracker, session)
}

fun createSessionExercise(
    tracker: WorkoutTracker,
    exercise: Exercise
): SessionExercise {
    val previousSets = findLatestSetsForExercise(
        tracker = tracker,
        exerciseId = exercise.id
    )

    return SessionExercise(
        exerciseId = exercise.id,
        exerciseName = exercise.name,
        sets = previousSets
    )
}

fun findLatestSetsForExercise(
    tracker: WorkoutTracker,
    exerciseId: Int
): MutableList<ExerciseSet> {
    val previousExercise = tracker.sessions
        .asSequence()
        .filter { it.isCompleted }
        .sortedByDescending { it.finishedAt }
        .mapNotNull { session ->
            session.exercises.find {
                it.exerciseId == exerciseId
            }
        }
        .firstOrNull()

    return previousExercise
        ?.sets
        ?.map { previousSet ->
            ExerciseSet(
                weight = previousSet.weight,
                reps = previousSet.reps,
                durationSeconds = previousSet.durationSeconds,
                isWarmup = previousSet.isWarmup,
                completed = false
            )
        }
        ?.toMutableList()
        ?: mutableListOf()
}

fun runActiveWorkout(
    tracker: WorkoutTracker,
    session: WorkoutSession
) {
    while (!session.isCompleted) {
        println()
        printActiveWorkout(session)

        println()
        println("1. Edit exercise")
        println("2. Add exercise")
        println("3. Remove exercise")
        println("4. Rename workout")
        println("5. Finish workout")
        println("6. Discard workout")

        when (readInt("Choice: ", 1..6)) {
            1 -> chooseExerciseToEdit(session)
            2 -> addExerciseToSession(tracker, session)
            3 -> removeExerciseFromSession(session)

            4 -> {
                session.name = readRequiredString("New workout name: ")
            }

            5 -> finishWorkout(session)

            6 -> {
                if (readYesNo("Discard this workout?")) {
                    tracker.sessions.remove(session)
                    println("Workout discarded.")
                    return
                }
            }
        }
    }
}

fun printActiveWorkout(session: WorkoutSession) {
    println("${session.name} — Active")
    println("==============================")
    println("Started: ${session.startedAt.format(displayDateFormat)}")

    if (session.exercises.isEmpty()) {
        println()
        println("No exercises added.")
        return
    }

    session.exercises.forEachIndexed { exerciseIndex, sessionExercise ->
        println()
        println("${exerciseIndex + 1}. ${sessionExercise.exerciseName}")

        if (sessionExercise.sets.isEmpty()) {
            println("   No sets added.")
        }

        sessionExercise.sets.forEachIndexed { setIndex, set ->
            val status = if (set.completed) "✓" else " "

            println(
                "   [$status] Set ${setIndex + 1}: ${formatSet(set)}"
            )
        }
    }
}

fun addExerciseToSession(
    tracker: WorkoutTracker,
    session: WorkoutSession
) {
    val availableExercises = tracker.exercises.filter { exercise ->
        session.exercises.none {
            it.exerciseId == exercise.id
        }
    }

    println()

    availableExercises.forEachIndexed { index, exercise ->
        println("${index + 1}. ${exercise.name}")
    }

    println("${availableExercises.size + 1}. Create a new exercise")
    println("0. Cancel")

    val choice = readInt(
        prompt = "Choice: ",
        validRange = 0..(availableExercises.size + 1)
    )

    when {
        choice == 0 -> return

        choice == availableExercises.size + 1 -> {
            val name = readRequiredString("Exercise name: ")
            val exercise = tracker.createExercise(name)

            val alreadyAdded = session.exercises.any {
                it.exerciseId == exercise.id
            }

            if (alreadyAdded) {
                println("That exercise is already in this workout.")
                return
            }

            session.exercises.add(
                createSessionExercise(tracker, exercise)
            )
        }

        else -> {
            val exercise = availableExercises[choice - 1]

            session.exercises.add(
                createSessionExercise(tracker, exercise)
            )
        }
    }
}

fun removeExerciseFromSession(session: WorkoutSession) {
    if (session.exercises.isEmpty()) {
        println("There are no exercises to remove.")
        return
    }

    println()

    session.exercises.forEachIndexed { index, exercise ->
        println("${index + 1}. ${exercise.exerciseName}")
    }

    println("0. Cancel")

    val choice = readInt(
        prompt = "Exercise to remove: ",
        validRange = 0..session.exercises.size
    )

    if (choice == 0) {
        return
    }

    val exercise = session.exercises[choice - 1]

    if (readYesNo("Remove ${exercise.exerciseName}?")) {
        session.exercises.removeAt(choice - 1)
        println("Exercise removed.")
    }
}

fun chooseExerciseToEdit(session: WorkoutSession) {
    if (session.exercises.isEmpty()) {
        println("There are no exercises to edit.")
        return
    }

    println()

    session.exercises.forEachIndexed { index, exercise ->
        println("${index + 1}. ${exercise.exerciseName}")
    }

    println("0. Cancel")

    val choice = readInt(
        prompt = "Exercise: ",
        validRange = 0..session.exercises.size
    )

    if (choice == 0) {
        return
    }

    editExercise(session.exercises[choice - 1])
}

fun editExercise(sessionExercise: SessionExercise) {
    while (true) {
        println()
        println(sessionExercise.exerciseName)
        println("=".repeat(sessionExercise.exerciseName.length))

        if (sessionExercise.sets.isEmpty()) {
            println("No sets added.")
        } else {
            sessionExercise.sets.forEachIndexed { index, set ->
                val status = if (set.completed) "✓" else " "
                println("${index + 1}. [$status] ${formatSet(set)}")
            }
        }

        println()
        println("1. Edit a set")
        println("2. Add a set")
        println("3. Duplicate a set")
        println("4. Remove a set")
        println("5. Toggle set completed")
        println("0. Back")

        when (readInt("Choice: ", 0..5)) {
            0 -> return
            1 -> editExistingSet(sessionExercise)
            2 -> sessionExercise.sets.add(readExerciseSet())
            3 -> duplicateSet(sessionExercise)
            4 -> removeSet(sessionExercise)
            5 -> toggleSetCompleted(sessionExercise)
        }
    }
}

fun editExistingSet(sessionExercise: SessionExercise) {
    val index = chooseSetIndex(sessionExercise) ?: return
    val set = sessionExercise.sets[index]

    println()
    println("Press Enter to keep the current value.")
    println("Type 'none' to clear a numeric value.")

    set.weight = readOptionalDoubleWithDefault(
        prompt = "Weight",
        currentValue = set.weight
    )

    set.reps = readOptionalIntWithDefault(
        prompt = "Reps",
        currentValue = set.reps
    )

    set.durationSeconds = readOptionalIntWithDefault(
        prompt = "Duration in seconds",
        currentValue = set.durationSeconds
    )

    set.isWarmup = readYesNoWithDefault(
        prompt = "Warm-up set",
        currentValue = set.isWarmup
    )
}

fun readExerciseSet(): ExerciseSet {
    println()
    println("Press Enter to leave a value blank.")

    return ExerciseSet(
        weight = readOptionalDouble("Weight: "),
        reps = readOptionalInt("Reps: "),
        durationSeconds = readOptionalInt("Duration in seconds: "),
        isWarmup = readYesNo("Warm-up set?"),
        completed = false
    )
}

fun duplicateSet(sessionExercise: SessionExercise) {
    val index = chooseSetIndex(sessionExercise) ?: return
    val existingSet = sessionExercise.sets[index]

    val duplicatedSet = ExerciseSet(
        weight = existingSet.weight,
        reps = existingSet.reps,
        durationSeconds = existingSet.durationSeconds,
        isWarmup = existingSet.isWarmup,
        completed = false
    )

    sessionExercise.sets.add(index + 1, duplicatedSet)
}

fun removeSet(sessionExercise: SessionExercise) {
    val index = chooseSetIndex(sessionExercise) ?: return
    sessionExercise.sets.removeAt(index)
}

fun toggleSetCompleted(sessionExercise: SessionExercise) {
    val index = chooseSetIndex(sessionExercise) ?: return
    val set = sessionExercise.sets[index]

    set.completed = !set.completed
}

fun chooseSetIndex(
    sessionExercise: SessionExercise
): Int? {
    if (sessionExercise.sets.isEmpty()) {
        println("There are no sets.")
        return null
    }

    val choice = readInt(
        prompt = "Set number, or 0 to cancel: ",
        validRange = 0..sessionExercise.sets.size
    )

    return if (choice == 0) {
        null
    } else {
        choice - 1
    }
}

fun finishWorkout(session: WorkoutSession) {
    if (session.exercises.isEmpty()) {
        println("The workout has no exercises.")
        return
    }

    val totalSets = session.exercises.sumOf {
        it.sets.size
    }

    if (totalSets == 0) {
        println("The workout has no sets.")

        if (!readYesNo("Finish anyway?")) {
            return
        }
    }

    val incompleteSets = session.exercises
        .flatMap { it.sets }
        .count { !it.completed }

    if (incompleteSets > 0) {
        println(
            "$incompleteSets set(s) have not been marked completed."
        )

        if (!readYesNo("Finish anyway?")) {
            return
        }
    }

    session.finishedAt = LocalDateTime.now()
    println("Workout saved to history.")
}
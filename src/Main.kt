import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Exercise(
    val id: Int,
    val name: String
)

data class WorkoutTemplate(
    val id: Int,
    val name: String,
    val exerciseIds: MutableList<Int>
)

data class ExerciseSet(
    var weight: Double?,
    var reps: Int?,
    var durationSeconds: Int?,
    var isWarmup: Boolean = false,
    var completed: Boolean = false
)

data class SessionExercise(
    val exerciseId: Int,
    val exerciseName: String,
    val sets: MutableList<ExerciseSet>
)

data class WorkoutSession(
    val id: Int,
    val templateId: Int?,
    var name: String,
    val startedAt: LocalDateTime,
    var finishedAt: LocalDateTime?,
    val exercises: MutableList<SessionExercise>
) {
    val isCompleted: Boolean
        get() = finishedAt != null
}

class WorkoutTracker {
    val exercises = mutableListOf<Exercise>()
    val templates = mutableListOf<WorkoutTemplate>()
    val sessions = mutableListOf<WorkoutSession>()

    private var nextExerciseId = 1
    private var nextTemplateId = 1
    private var nextSessionId = 1

    fun createExercise(name: String): Exercise {
        val existingExercise = exercises.find {
            it.name.equals(name, ignoreCase = true)
        }

        if (existingExercise != null) {
            return existingExercise
        }

        val exercise = Exercise(
            id = nextExerciseId++,
            name = name
        )

        exercises.add(exercise)
        return exercise
    }

    fun createTemplate(
        name: String,
        exerciseIds: MutableList<Int>
    ): WorkoutTemplate {
        val template = WorkoutTemplate(
            id = nextTemplateId++,
            name = name,
            exerciseIds = exerciseIds
        )

        templates.add(template)
        return template
    }

    fun createSession(
        name: String,
        templateId: Int?,
        sessionExercises: MutableList<SessionExercise>
    ): WorkoutSession {
        val session = WorkoutSession(
            id = nextSessionId++,
            templateId = templateId,
            name = name,
            startedAt = LocalDateTime.now(),
            finishedAt = null,
            exercises = sessionExercises
        )

        sessions.add(session)
        return session
    }
}

private val displayDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

fun main() {
    val tracker = WorkoutTracker()

    addSampleData(tracker)

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
            6 -> {
                println("Goodbye!")
                return
            }
        }
    }
}

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

fun formatSet(set: ExerciseSet): String {
    val prefix = if (set.isWarmup) {
        "Warm-up — "
    } else {
        ""
    }

    val values = mutableListOf<String>()

    set.weight?.let {
        values.add("${formatNumber(it)} lb")
    }

    set.reps?.let {
        values.add("$it reps")
    }

    set.durationSeconds?.let {
        values.add(formatDuration(it))
    }

    val setDescription = if (values.isEmpty()) {
        "No values"
    } else {
        values.joinToString(" × ")
    }

    return prefix + setDescription
}

fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        value.toString()
    }
}

fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return when {
        minutes > 0 && remainingSeconds > 0 ->
            "${minutes}m ${remainingSeconds}s"

        minutes > 0 ->
            "${minutes}m"

        else ->
            "${remainingSeconds}s"
    }
}

fun readRequiredString(prompt: String): String {
    while (true) {
        print(prompt)
        val input = readln().trim()

        if (input.isNotEmpty()) {
            return input
        }

        println("Value cannot be blank.")
    }
}

fun readInt(
    prompt: String,
    validRange: IntRange
): Int {
    while (true) {
        print(prompt)

        val value = readln()
            .trim()
            .toIntOrNull()

        if (value != null && value in validRange) {
            return value
        }

        println(
            "Enter a whole number from " +
                    "${validRange.first} to ${validRange.last}."
        )
    }
}

fun readOptionalInt(prompt: String): Int? {
    while (true) {
        print(prompt)
        val input = readln().trim()

        if (input.isEmpty()) {
            return null
        }

        val value = input.toIntOrNull()

        if (value != null && value >= 0) {
            return value
        }

        println(
            "Enter a non-negative whole number " +
                    "or press Enter."
        )
    }
}

fun readOptionalDouble(prompt: String): Double? {
    while (true) {
        print(prompt)
        val input = readln().trim()

        if (input.isEmpty()) {
            return null
        }

        val value = input.toDoubleOrNull()

        if (value != null && value >= 0.0) {
            return value
        }

        println(
            "Enter a non-negative number " +
                    "or press Enter."
        )
    }
}

fun readYesNo(prompt: String): Boolean {
    while (true) {
        print("$prompt (y/n): ")

        when (readln().trim().lowercase()) {
            "y", "yes" -> return true
            "n", "no" -> return false
            else -> println("Enter y or n.")
        }
    }
}

fun readOptionalDoubleWithDefault(
    prompt: String,
    currentValue: Double?
): Double? {
    while (true) {
        print("$prompt [${currentValue ?: "none"}]: ")
        val input = readln().trim()

        if (input.isEmpty()) {
            return currentValue
        }

        if (input.equals("none", ignoreCase = true)) {
            return null
        }

        val value = input.toDoubleOrNull()

        if (value != null && value >= 0.0) {
            return value
        }

        println(
            "Enter a non-negative number, " +
                    "'none', or press Enter."
        )
    }
}

fun readOptionalIntWithDefault(
    prompt: String,
    currentValue: Int?
): Int? {
    while (true) {
        print("$prompt [${currentValue ?: "none"}]: ")
        val input = readln().trim()

        if (input.isEmpty()) {
            return currentValue
        }

        if (input.equals("none", ignoreCase = true)) {
            return null
        }

        val value = input.toIntOrNull()

        if (value != null && value >= 0) {
            return value
        }

        println(
            "Enter a non-negative whole number, " +
                    "'none', or press Enter."
        )
    }
}

fun readYesNoWithDefault(
    prompt: String,
    currentValue: Boolean
): Boolean {
    val defaultText = if (currentValue) "y" else "n"

    while (true) {
        print("$prompt [$defaultText]: ")

        when (readln().trim().lowercase()) {
            "" -> return currentValue
            "y", "yes" -> return true
            "n", "no" -> return false
            else -> println(
                "Enter y, n, or press Enter."
            )
        }
    }
}
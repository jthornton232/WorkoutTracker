import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dataFile: Path = Path.of("exercise_sessions.tsv")

private val storageDateFormat: DateTimeFormatter =
    DateTimeFormatter.ISO_LOCAL_DATE_TIME

private val displayDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

data class ExerciseSession(
    val exerciseName: String,
    val date: LocalDateTime,
    val weight: Double?,
    val reps: Int?,
    val sets: Int?,
    val durationSeconds: Int?
)

fun main() {
    val sessions = loadSessions().toMutableList()

    println("Exercise Tracker")
    println("================")

    while (true) {
        println()
        println("1. Log an exercise")
        println("2. View exercise history")
        println("3. Exit")

        when (readInt("Choose an option: ", 1..3)) {
            1 -> logExercise(sessions)
            2 -> viewHistory(sessions)
            3 -> {
                println("Goodbye!")
                return
            }
        }
    }
}

private fun logExercise(sessions: MutableList<ExerciseSession>) {
    val exerciseName = chooseExercise(sessions) ?: return

    val previousSession = sessions
        .filter { it.exerciseName.equals(exerciseName, ignoreCase = true) }
        .maxByOrNull { it.date }

    println()
    println("Exercise: $exerciseName")
    println("------------------------------")

    if (previousSession == null) {
        println("No previous session found.")
    } else {
        printSession("Previous session", previousSession)
    }

    println()
    println("Enter today's values.")
    println("Press Enter to leave a field blank.")

    val newSession = ExerciseSession(
        exerciseName = exerciseName,
        date = LocalDateTime.now(),
        weight = readOptionalDouble("Weight: "),
        reps = readOptionalInt("Reps: "),
        sets = readOptionalInt("Sets: "),
        durationSeconds = readDurationSeconds()
    )

    if (!newSession.hasRecordedValues()) {
        println("No values were entered. Session was not saved.")
        return
    }

    sessions.add(newSession)
    saveSession(newSession)

    println()
    println("Session saved:")
    printSession("Current session", newSession)
}

private fun chooseExercise(
    sessions: List<ExerciseSession>
): String? {
    val exercises = sessions
        .map { it.exerciseName }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }

    if (exercises.isEmpty()) {
        println()
        println("You don't have any exercises yet.")
        return readNewExerciseName()
    }

    println()
    println("Choose an exercise:")

    exercises.forEachIndexed { index, exercise ->
        println("${index + 1}. $exercise")
    }

    println("${exercises.size + 1}. Add a new exercise")
    println("0. Cancel")

    val choice = readInt(
        prompt = "Choice: ",
        validRange = 0..(exercises.size + 1)
    )

    return when {
        choice == 0 -> null
        choice == exercises.size + 1 -> readNewExerciseName()
        else -> exercises[choice - 1]
    }
}

private fun readNewExerciseName(): String {
    while (true) {
        print("Exercise name: ")
        val name = readln().trim()

        if (name.isNotEmpty()) {
            return name
        }

        println("Exercise name cannot be blank.")
    }
}

private fun viewHistory(sessions: List<ExerciseSession>) {
    if (sessions.isEmpty()) {
        println()
        println("No exercise sessions have been recorded.")
        return
    }

    val exerciseName = chooseExercise(sessions) ?: return

    val exerciseSessions = sessions
        .filter { it.exerciseName.equals(exerciseName, ignoreCase = true) }
        .sortedByDescending { it.date }

    println()
    println("$exerciseName History")
    println("==============================")

    exerciseSessions.forEachIndexed { index, session ->
        println()
        printSession("Session ${index + 1}", session)
    }
}

private fun printSession(
    heading: String,
    session: ExerciseSession
) {
    println("$heading:")
    println("  Date: ${session.date.format(displayDateFormat)}")
    println("  Weight: ${formatWeight(session.weight)}")
    println("  Reps: ${session.reps ?: "—"}")
    println("  Sets: ${session.sets ?: "—"}")
    println("  Duration: ${formatDuration(session.durationSeconds)}")
}

private fun formatWeight(weight: Double?): String {
    if (weight == null) {
        return "—"
    }

    return if (weight % 1.0 == 0.0) {
        weight.toInt().toString()
    } else {
        weight.toString()
    }
}

private fun readDurationSeconds(): Int? {
    val minutes = readOptionalInt("Duration minutes: ") ?: return null

    if (minutes < 0) {
        println("Duration cannot be negative.")
        return readDurationSeconds()
    }

    return minutes * 60
}

private fun formatDuration(durationSeconds: Int?): String {
    if (durationSeconds == null) {
        return "—"
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60

    return when {
        minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

private fun readInt(
    prompt: String,
    validRange: IntRange
): Int {
    while (true) {
        print(prompt)

        val value = readln().trim().toIntOrNull()

        if (value != null && value in validRange) {
            return value
        }

        println(
            "Enter a whole number from " +
                    "${validRange.first} to ${validRange.last}."
        )
    }
}

private fun readOptionalInt(prompt: String): Int? {
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

        println("Enter a non-negative whole number or press Enter.")
    }
}

private fun readOptionalDouble(prompt: String): Double? {
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

        println("Enter a non-negative number or press Enter.")
    }
}

private fun ExerciseSession.hasRecordedValues(): Boolean {
    return weight != null ||
            reps != null ||
            sets != null ||
            durationSeconds != null
}

private fun saveSession(session: ExerciseSession) {
    val sanitizedName = session.exerciseName
        .replace('\t', ' ')
        .replace('\n', ' ')

    val line = listOf(
        sanitizedName,
        session.date.format(storageDateFormat),
        session.weight?.toString().orEmpty(),
        session.reps?.toString().orEmpty(),
        session.sets?.toString().orEmpty(),
        session.durationSeconds?.toString().orEmpty()
    ).joinToString("\t") + System.lineSeparator()

    try {
        Files.writeString(
            dataFile,
            line,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )
    } catch (exception: Exception) {
        println("Warning: the session could not be saved to disk.")
        println(exception.message)
    }
}

private fun loadSessions(): List<ExerciseSession> {
    if (!Files.exists(dataFile)) {
        return emptyList()
    }

    return try {
        Files.readAllLines(dataFile)
            .mapNotNull(::parseSession)
    } catch (exception: Exception) {
        println("Warning: saved sessions could not be loaded.")
        println(exception.message)
        emptyList()
    }
}

private fun parseSession(line: String): ExerciseSession? {
    val values = line.split('\t')

    if (values.size != 6) {
        return null
    }

    return try {
        ExerciseSession(
            exerciseName = values[0],
            date = LocalDateTime.parse(
                values[1],
                storageDateFormat
            ),
            weight = values[2].toDoubleOrNull(),
            reps = values[3].toIntOrNull(),
            sets = values[4].toIntOrNull(),
            durationSeconds = values[5].toIntOrNull()
        )
    } catch (_: Exception) {
        null
    }
}
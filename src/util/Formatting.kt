package util

import model.ExerciseSet

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

    return prefix + if (values.isEmpty()) {
        "No values"
    } else {
        values.joinToString(" × ")
    }
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
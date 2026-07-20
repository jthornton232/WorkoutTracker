package console

import java.time.format.DateTimeFormatter

val displayDateFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

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
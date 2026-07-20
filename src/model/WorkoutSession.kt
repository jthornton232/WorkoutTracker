package model

import java.time.LocalDateTime

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

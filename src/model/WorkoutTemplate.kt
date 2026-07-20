package model

data class WorkoutTemplate(
    val id: Int,
    val name: String,
    val exerciseIds: MutableList<Int>
)
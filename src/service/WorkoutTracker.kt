package service

import model.Exercise
import model.SessionExercise
import model.WorkoutSession
import model.WorkoutTemplate
import java.time.LocalDateTime

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
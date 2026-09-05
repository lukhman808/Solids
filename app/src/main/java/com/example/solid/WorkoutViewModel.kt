package com.example.solid
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class FeedItem {
    data class SessionItem(val session: Session) : FeedItem()
    data class BodyWeightItem(val entry: BodyWeightEntry) : FeedItem()
}

private fun formattedDate(): String {
    val day = LocalDate.now().dayOfMonth
    val suffix = when {
        day in 11..13 -> "TH"
        day % 10 == 1 -> "ST"
        day % 10 == 2 -> "ND"
        day % 10 == 3 -> "RD"
        else -> "TH"
    }
    return "$day$suffix ${LocalDate.now().month} ${LocalDate.now().year}"
}

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).workoutDao()
    val sessions = dao.getAllSessions().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val feed = combine(dao.getAllSessions(), dao.getAllBodyWeightEntries()) { sessions, entries ->
        (sessions.map { FeedItem.SessionItem(it) } + entries.map { FeedItem.BodyWeightItem(it) })
            .sortedByDescending {
                when (it) {
                    is FeedItem.SessionItem -> it.session.timestamp
                    is FeedItem.BodyWeightItem -> it.entry.timestamp
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val colorPalette = listOf(
        0xFF6200EE, 0xFF03DAC5, 0xFFFF5722, 0xFF4CAF50,
        0xFF2196F3, 0xFFFF9800, 0xFFE91E63, 0xFF9C27B0
    )
    fun addSession(name: String) {
        viewModelScope.launch {
            dao.insertSession(Session(name = name, date = formattedDate(), timestamp = System.currentTimeMillis()))
        }
    }
    fun updateSession(session: Session, newName: String, newDate: String) {
        viewModelScope.launch {
            dao.updateSession(session.copy(name = newName, date = newDate))
        }
    }
    fun deleteSession(session: Session) {
        viewModelScope.launch {
            dao.deleteSession(session)
        }
    }
    fun getWorkoutsForSession(sessionId: Int) = dao.getWorkoutsForSession(sessionId)
    fun addWorkout(sessionId: Int, exerciseName: String) {
        viewModelScope.launch {
            dao.insertWorkout(Workout(sessionId = sessionId, exerciseName = exerciseName))
        }
    }
    fun updateWorkout(workout: Workout, newName: String) {
        viewModelScope.launch {
            dao.updateWorkout(workout.copy(exerciseName = newName))
        }
    }
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            dao.deleteWorkout(workout)
        }
    }
    fun getSetsForWorkout(workoutId: Int) = dao.getSetsForWorkout(workoutId)
    fun addSet(workoutId: Int, reps: Int, weight: Float, isPartial: Boolean, difficulty: String?) {
        viewModelScope.launch {
            dao.insertSet(
                WorkoutSet(
                    workoutId = workoutId,
                    reps = reps,
                    weight = weight,
                    isPartial = isPartial,
                    difficulty = difficulty
                )
            )
        }
    }
    fun updateSet(set: WorkoutSet, reps: Int, weight: Float, isPartial: Boolean, difficulty: String?) {
        viewModelScope.launch {
            dao.updateSet(set.copy(reps = reps, weight = weight, isPartial = isPartial, difficulty = difficulty))
        }
    }
    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            dao.deleteSet(set)
        }
    }
    fun addBodyWeightEntry(weight: Float) {
        viewModelScope.launch {
            dao.insertBodyWeightEntry(
                BodyWeightEntry(weight = weight, date = formattedDate(), timestamp = System.currentTimeMillis())
            )
        }
    }
    fun updateBodyWeightEntry(entry: BodyWeightEntry, newWeight: Float) {
        viewModelScope.launch {
            dao.updateBodyWeightEntry(entry.copy(weight = newWeight))
        }
    }
    fun deleteBodyWeightEntry(entry: BodyWeightEntry) {
        viewModelScope.launch {
            dao.deleteBodyWeightEntry(entry)
        }
    }
    suspend fun getSessionSuggestions(): List<String> {
        return dao.getDistinctSessionNames()
    }
    suspend fun getWorkoutsForSessionName(sessionName: String): List<Workout> {
        return dao.getWorkoutsForSessionName(sessionName)
    }
    suspend fun getTotalWeightLifted(): Float {
        return dao.getTotalWeightLifted() ?: 0f
    }
    fun addSessionWithWorkouts(name: String, workouts: List<Workout>) {
        viewModelScope.launch {
            val existingColor = dao.getColorForSessionName(name)
            val colorIndex = existingColor ?: (dao.getDistinctSessionCount() % colorPalette.size).toInt()
            val sessionId = dao.insertSession(
                Session(
                    name = name,
                    date = formattedDate(),
                    color = colorIndex,
                    timestamp = System.currentTimeMillis()
                )
            ).toInt()
            workouts.forEach { workout ->
                dao.insertWorkout(Workout(sessionId = sessionId, exerciseName = workout.exerciseName))
            }
        }
    }
    suspend fun getExerciseSuggestions(): List<String> {
        return dao.getDistinctExerciseNames()
    }


    fun getNoteForExercise(exerciseName: String) = dao.getNoteForExerciseFlow(exerciseName)

    fun saveNote(exerciseName: String, note: String) {
        viewModelScope.launch {
            dao.upsertNote(WorkoutNote(exerciseName = exerciseName, note = note))
        }
    }

    suspend fun getPreviousSessionOfSameName(sessionId: Int): Session? {
        val current = dao.getSessionById(sessionId) ?: return null
        return dao.getPreviousSessionByName(current.name, sessionId)
    }
}
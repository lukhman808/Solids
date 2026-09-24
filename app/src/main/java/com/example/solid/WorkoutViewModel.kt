package com.example.solid
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId

sealed class FeedItem {
    data class SessionItem(val session: Session) : FeedItem()
    data class BodyWeightItem(val entry: BodyWeightEntry) : FeedItem()
    data class NoteItem(val entry: NoteEntry) : FeedItem()
}

data class GraphCurve(val setIndex: Int, val points: List<Pair<Float, Float>>)

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
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val feed = combine(
        dao.getAllSessions(),
        dao.getAllBodyWeightEntries(),
        dao.getAllNoteEntries()
    ) { sessions, bodyWeights, notes ->
        (sessions.map { FeedItem.SessionItem(it) } +
                bodyWeights.map { FeedItem.BodyWeightItem(it) } +
                notes.map { FeedItem.NoteItem(it) })
            .sortedBy {
                when (it) {
                    is FeedItem.SessionItem -> it.session.timestamp
                    is FeedItem.BodyWeightItem -> it.entry.timestamp
                    is FeedItem.NoteItem -> it.entry.timestamp
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
    fun updateSession(session: Session, newName: String, newDate: LocalDate) {
        viewModelScope.launch {
            val day = newDate.dayOfMonth
            val suffix = when {
                day in 11..13 -> "TH"
                day % 10 == 1 -> "ST"
                day % 10 == 2 -> "ND"
                day % 10 == 3 -> "RD"
                else -> "TH"
            }
            val dateStr = "$day$suffix ${newDate.month} ${newDate.year}"
            val newTimestamp = newDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            dao.updateSession(session.copy(name = newName, date = dateStr, timestamp = newTimestamp))
        }
    }
    fun deleteSession(session: Session) {
        viewModelScope.launch { dao.deleteSession(session) }
    }
    fun getWorkoutsForSession(sessionId: Int) = dao.getWorkoutsForSession(sessionId)
    fun addWorkout(sessionId: Int, exerciseName: String) {
        viewModelScope.launch {
            dao.insertWorkout(Workout(sessionId = sessionId, exerciseName = exerciseName))
        }
    }
    fun updateWorkout(workout: Workout, newName: String) {
        viewModelScope.launch { dao.updateWorkout(workout.copy(exerciseName = newName)) }
    }
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch { dao.deleteWorkout(workout) }
    }
    fun getSetsForWorkout(workoutId: Int) = dao.getSetsForWorkout(workoutId)
    fun addSet(workoutId: Int, reps: Int, weight: Float, isPartial: Boolean, difficulty: String?) {
        viewModelScope.launch {
            dao.insertSet(WorkoutSet(workoutId = workoutId, reps = reps, weight = weight, isPartial = isPartial, difficulty = difficulty))
        }
    }
    fun updateSet(set: WorkoutSet, reps: Int, weight: Float, isPartial: Boolean, difficulty: String?) {
        viewModelScope.launch {
            dao.updateSet(set.copy(reps = reps, weight = weight, isPartial = isPartial, difficulty = difficulty))
        }
    }
    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch { dao.deleteSet(set) }
    }
    fun addBodyWeightEntry(weight: Float) {
        viewModelScope.launch {
            dao.insertBodyWeightEntry(BodyWeightEntry(weight = weight, date = formattedDate(), timestamp = System.currentTimeMillis()))
        }
    }
    fun updateBodyWeightEntry(entry: BodyWeightEntry, newWeight: Float) {
        viewModelScope.launch { dao.updateBodyWeightEntry(entry.copy(weight = newWeight)) }
    }
    fun deleteBodyWeightEntry(entry: BodyWeightEntry) {
        viewModelScope.launch { dao.deleteBodyWeightEntry(entry) }
    }
    fun addNoteEntry(typeName: String, note: String) {
        viewModelScope.launch {
            dao.insertNoteEntry(NoteEntry(typeName = typeName, note = note, date = formattedDate(), timestamp = System.currentTimeMillis()))
        }
    }
    fun updateNoteEntry(entry: NoteEntry, newNote: String) {
        viewModelScope.launch { dao.updateNoteEntry(entry.copy(note = newNote)) }
    }
    fun deleteNoteEntry(entry: NoteEntry) {
        viewModelScope.launch { dao.deleteNoteEntry(entry) }
    }
    suspend fun getSessionSuggestions(): List<String> = dao.getDistinctSessionNames()
    suspend fun getWorkoutsForSessionName(sessionName: String): List<Workout> = dao.getWorkoutsForSessionName(sessionName)
    suspend fun getTotalWeightLifted(): Float = dao.getTotalWeightLifted() ?: 0f
    fun addSessionWithWorkouts(name: String, workouts: List<Workout>) {
        viewModelScope.launch {
            val existingColor = dao.getColorForSessionName(name)
            val colorIndex = existingColor ?: (dao.getDistinctSessionCount() % colorPalette.size).toInt()
            val sessionId = dao.insertSession(
                Session(name = name, date = formattedDate(), color = colorIndex, timestamp = System.currentTimeMillis())
            ).toInt()
            workouts.forEach { workout ->
                dao.insertWorkout(Workout(sessionId = sessionId, exerciseName = workout.exerciseName))
            }
        }
    }
    suspend fun getExerciseSuggestions(): List<String> = dao.getDistinctExerciseNames()
    fun getNoteForExercise(exerciseName: String) = dao.getNoteForExerciseFlow(exerciseName)
    fun saveNote(exerciseName: String, note: String) {
        viewModelScope.launch { dao.upsertNote(WorkoutNote(exerciseName = exerciseName, note = note)) }
    }
    suspend fun getPreviousSessionOfSameName(sessionId: Int): Session? {
        val current = dao.getSessionById(sessionId) ?: return null
        return dao.getPreviousSessionByName(current.name, sessionId)
    }

    val dashboardCards = dao.getAllDashboardCards().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun addDashboardCard(cardType: String, scopeType: String, scopeValue: String?, metric: String = "WEIGHT") {
        viewModelScope.launch {
            val count = dao.getDashboardCardCount()
            dao.insertDashboardCard(
                DashboardCard(cardType = cardType, scopeType = scopeType, scopeValue = scopeValue, metric = metric, order = count)
            )
        }
    }
    fun deleteDashboardCard(card: DashboardCard) {
        viewModelScope.launch { dao.deleteDashboardCard(card) }
    }
    fun updateDashboardCardMetric(card: DashboardCard, newMetric: String) {
        viewModelScope.launch { dao.updateDashboardCard(card.copy(metric = newMetric)) }
    }
    suspend fun ensureDefaultDashboardCard() {
        if (dao.getDashboardCardCount() == 0) {
            dao.insertDashboardCard(
                DashboardCard(cardType = "CALENDAR", scopeType = "ALL_WORKOUTS", scopeValue = null, order = 0)
            )
        }
    }
    suspend fun getCustomNoteTypes(): List<String> = dao.getDistinctNoteTypes()

    suspend fun getSessionName(sessionId: Int): String? = dao.getSessionById(sessionId)?.name

    suspend fun getCalendarHighlights(scopeType: String, scopeValue: String?): Map<LocalDate, Color> {
        val result = mutableMapOf<LocalDate, Color>()
        fun toDate(ts: Long) = Instant.ofEpochMilli(ts).atZone(ZoneId.systemDefault()).toLocalDate()
        when (scopeType) {
            "ALL_WORKOUTS" -> {
                dao.getAllSessionDates().forEach { row ->
                    val colorIndex = dao.getColorForSessionName(row.name) ?: 0
                    result[toDate(row.timestamp)] = sessionColorPalette[colorIndex % sessionColorPalette.size]
                }
            }
            "SESSION_NAME" -> {
                val colorIndex = dao.getColorForSessionName(scopeValue ?: "") ?: 0
                dao.getDatesForSessionName(scopeValue ?: "").forEach { row ->
                    result[toDate(row.timestamp)] = sessionColorPalette[colorIndex % sessionColorPalette.size]
                }
            }
            "EXERCISE_NAME" -> {
                dao.getDatesForExerciseName(scopeValue ?: "").forEach { row ->
                    result[toDate(row.timestamp)] = Color(0xFF03DAC5)
                }
            }
            "BODYWEIGHT" -> {
                dao.getBodyWeightDates().forEach { row ->
                    result[toDate(row.timestamp)] = Color(0xFFFF9800)
                }
            }
            "NOTE_TYPE" -> {
                dao.getNoteDatesForType(scopeValue ?: "").forEach { row ->
                    result[toDate(row.timestamp)] = Color(0xFFE91E63)
                }
            }
        }
        return result
    }



    private fun metricValue(metric: String, weight: Float, reps: Int): Float = when (metric) {
        "REPS" -> reps.toFloat()
        "VOLUME" -> weight * reps
        else -> weight
    }

    suspend fun getGraphData(scopeType: String, scopeValue: String?, metric: String): List<GraphCurve> {
        if (scopeType == "BODYWEIGHT") {
            val entries = dao.getBodyWeightEntriesOrdered()
            val points = entries.mapIndexed { index, entry -> index.toFloat() to entry.weight }
            return listOf(GraphCurve(setIndex = 0, points = points))
        }
        val rows = dao.getSetsHistoryForExercise(scopeValue ?: "")
        val grouped = LinkedHashMap<Int, MutableList<SetHistoryRow>>()
        rows.forEach { row ->
            grouped.getOrPut(row.workoutId) { mutableListOf() }.add(row)
        }
        val sessionGroups = grouped.values.toList()
        val curvesMap = mutableMapOf<Int, MutableList<Pair<Float, Float>>>()
        sessionGroups.forEachIndexed { sessionIndex, setsInSession ->
            setsInSession.forEachIndexed { idx, row ->
                val setIndex = idx + 1
                val value = metricValue(metric, row.weight, row.reps)
                curvesMap.getOrPut(setIndex) { mutableListOf() }.add(sessionIndex.toFloat() to value)
            }
        }
        return curvesMap.entries.sortedBy { it.key }.map { GraphCurve(it.key, it.value) }
    }

    suspend fun exportBackup(): String = BackupManager.exportToMarkdown(dao)

    suspend fun restoreBackup(content: String): Boolean {
        val parsed = BackupManager.parseMarkdown(content) ?: return false
        dao.deleteAllSessions()
        dao.deleteAllWorkouts()
        dao.deleteAllSets()
        dao.deleteAllBodyWeight()
        dao.deleteAllNoteEntries()
        dao.deleteAllWorkoutNotes()
        insertParsedBackup(parsed)
        return true
    }

    suspend fun mergeBackup(content: String): Boolean {
        val parsed = BackupManager.parseMarkdown(content) ?: return false
        insertParsedBackup(parsed)
        return true
    }

    private suspend fun insertParsedBackup(parsed: BackupManager.ParsedBackup) {
        parsed.sessions.forEach { ps ->
            val sessionId = dao.insertSession(ps.session).toInt()
            ps.workouts.forEach { pw ->
                val workoutId = dao.insertWorkout(pw.workout.copy(sessionId = sessionId)).toInt()
                pw.sets.forEach { set ->
                    dao.insertSet(set.copy(workoutId = workoutId))
                }
            }
        }
        parsed.bodyWeights.forEach { dao.insertBodyWeightEntry(it) }
        parsed.noteEntries.forEach { dao.insertNoteEntry(it) }
        parsed.workoutNotes.forEach { dao.upsertNote(it) }
    }


}
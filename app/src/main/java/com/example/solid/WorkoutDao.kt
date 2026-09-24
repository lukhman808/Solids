package com.example.solid

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface WorkoutDao {

    // Sessions
    @Insert
    suspend fun insertSession(session: Session): Long

    @Query("SELECT * FROM sessions ORDER BY id ASC, date ASC")
    fun getAllSessions(): Flow<List<Session>>

    @Delete
    suspend fun deleteSession(session: Session)


    @Update
    suspend fun updateSession(session: Session)

    // Workouts
    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Query("SELECT * FROM workouts WHERE sessionId = :sessionId")
    fun getWorkoutsForSession(sessionId: Int): Flow<List<Workout>>

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteSet(set: WorkoutSet)

    @Update
    suspend fun updateSet(set: WorkoutSet)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // Sets
    @Insert
    suspend fun insertSet(workoutSet: WorkoutSet)

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId")
    fun getSetsForWorkout(workoutId: Int): Flow<List<WorkoutSet>>

    // Progress tracking - get all sets for a specific exercise
    @Query("""
        SELECT workout_sets.* FROM workout_sets 
        INNER JOIN workouts ON workout_sets.workoutId = workouts.id 
        WHERE workouts.exerciseName = :exerciseName 
        ORDER BY workout_sets.id ASC
    """)
    fun getProgressForExercise(exerciseName: String): Flow<List<WorkoutSet>>

    // Get distinct session names for suggestions
    @Query("SELECT DISTINCT name FROM sessions ORDER BY name ASC")
    suspend fun getDistinctSessionNames(): List<String>

    // Get workouts for a specific session name (for copying exercises)
    @Query("SELECT * FROM workouts WHERE sessionId = (SELECT id FROM sessions WHERE name = :sessionName ORDER BY id DESC LIMIT 1)")
    suspend fun getWorkoutsForSessionName(sessionName: String): List<Workout>

    // Get the next color index to assign
    @Query("SELECT COUNT(DISTINCT name) FROM sessions")
    suspend fun getDistinctSessionCount(): Int

    @Query("SELECT color FROM sessions WHERE name = :name LIMIT 1")
    suspend fun getColorForSessionName(name: String): Int?

    @Query("SELECT DISTINCT exerciseName FROM workouts ORDER BY exerciseName ASC")
    suspend fun getDistinctExerciseNames(): List<String>

    @Query("SELECT SUM(weight * reps) FROM workout_sets")
    suspend fun getTotalWeightLifted(): Float?

    // Body weight entries
    @Insert
    suspend fun insertBodyWeightEntry(entry: BodyWeightEntry): Long

    @Query("SELECT * FROM body_weight_entries ORDER BY timestamp DESC")
    fun getAllBodyWeightEntries(): Flow<List<BodyWeightEntry>>

    @Update
    suspend fun updateBodyWeightEntry(entry: BodyWeightEntry)

    @Delete
    suspend fun deleteBodyWeightEntry(entry: BodyWeightEntry)

    // Workout notes
    @Query("SELECT * FROM workout_notes WHERE exerciseName = :exerciseName LIMIT 1")
    suspend fun getNoteForExercise(exerciseName: String): WorkoutNote?

    @Query("SELECT * FROM workout_notes WHERE exerciseName = :exerciseName LIMIT 1")
    fun getNoteForExerciseFlow(exerciseName: String): Flow<WorkoutNote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: WorkoutNote)

    @Query("SELECT * FROM sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Int): Session?

    @Query("""
    SELECT * FROM sessions 
    WHERE name = :name AND id != :excludeId 
    ORDER BY timestamp DESC 
    LIMIT 1
""")
    suspend fun getPreviousSessionByName(name: String, excludeId: Int): Session?

    @Insert
    suspend fun insertNoteEntry(entry: NoteEntry): Long

    @Query("SELECT * FROM note_entries ORDER BY timestamp DESC")
    fun getAllNoteEntries(): Flow<List<NoteEntry>>

    @Update
    suspend fun updateNoteEntry(entry: NoteEntry)

    @Delete
    suspend fun deleteNoteEntry(entry: NoteEntry)

    @Insert
    suspend fun insertDashboardCard(card: DashboardCard): Long

    @Query("SELECT * FROM dashboard_cards ORDER BY `order` ASC, timestamp ASC")
    fun getAllDashboardCards(): Flow<List<DashboardCard>>

    @Update
    suspend fun updateDashboardCard(card: DashboardCard)

    @Delete
    suspend fun deleteDashboardCard(card: DashboardCard)

    @Query("SELECT COUNT(*) FROM dashboard_cards")
    suspend fun getDashboardCardCount(): Int

    // For calendar: get all dates a given session name occurred on
    @Query("SELECT DISTINCT date, timestamp FROM sessions WHERE name = :sessionName ORDER BY timestamp ASC")
    suspend fun getDatesForSessionName(sessionName: String): List<SessionDateRow>

    // For calendar: all sessions (any name), for "All Workouts" scope
    @Query("SELECT DISTINCT name, date, timestamp FROM sessions ORDER BY timestamp ASC")
    suspend fun getAllSessionDates(): List<AllSessionDateRow>

    // For calendar: dates a specific exercise was performed on (via its workout instances)
    @Query("""
    SELECT DISTINCT sessions.date as date, sessions.timestamp as timestamp
    FROM workouts
    INNER JOIN sessions ON workouts.sessionId = sessions.id
    WHERE workouts.exerciseName = :exerciseName
    ORDER BY sessions.timestamp ASC
""")
    suspend fun getDatesForExerciseName(exerciseName: String): List<SessionDateRow>

    // For calendar: bodyweight / note-type dates
    @Query("SELECT date, timestamp FROM body_weight_entries ORDER BY timestamp ASC")
    suspend fun getBodyWeightDates(): List<SessionDateRow>

    @Query("SELECT date, timestamp FROM note_entries WHERE typeName = :typeName ORDER BY timestamp ASC")
    suspend fun getNoteDatesForType(typeName: String): List<SessionDateRow>

    // For graph: all sets for a given exercise, across all its workout instances, ordered by session recency
    @Query("""
    SELECT workout_sets.*, sessions.timestamp as sessionTimestamp
    FROM workout_sets
    INNER JOIN workouts ON workout_sets.workoutId = workouts.id
    INNER JOIN sessions ON workouts.sessionId = sessions.id
    WHERE workouts.exerciseName = :exerciseName AND workout_sets.isPartial = 0
    ORDER BY sessions.timestamp ASC, workouts.id ASC, workout_sets.id ASC
""")
    suspend fun getSetsHistoryForExercise(exerciseName: String): List<SetHistoryRow>

    @Query("SELECT DISTINCT typeName FROM note_entries ORDER BY typeName ASC")
    suspend fun getDistinctNoteTypes(): List<String>

    @Query("SELECT * FROM body_weight_entries ORDER BY timestamp ASC")
    suspend fun getBodyWeightEntriesOrdered(): List<BodyWeightEntry>

    // Export — one-shot full reads
    @Query("SELECT * FROM sessions ORDER BY timestamp ASC")
    suspend fun getAllSessionsOnce(): List<Session>

    @Query("SELECT * FROM workouts")
    suspend fun getAllWorkoutsOnce(): List<Workout>

    @Query("SELECT * FROM workout_sets")
    suspend fun getAllSetsOnce(): List<WorkoutSet>

    @Query("SELECT * FROM body_weight_entries ORDER BY timestamp ASC")
    suspend fun getAllBodyWeightOnce(): List<BodyWeightEntry>

    @Query("SELECT * FROM note_entries ORDER BY timestamp ASC")
    suspend fun getAllNoteEntriesOnce(): List<NoteEntry>

    @Query("SELECT * FROM workout_notes")
    suspend fun getAllWorkoutNotesOnce(): List<WorkoutNote>

    // Wipe (for Restore)
    @Query("DELETE FROM sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM workouts")
    suspend fun deleteAllWorkouts()

    @Query("DELETE FROM workout_sets")
    suspend fun deleteAllSets()

    @Query("DELETE FROM body_weight_entries")
    suspend fun deleteAllBodyWeight()

    @Query("DELETE FROM note_entries")
    suspend fun deleteAllNoteEntries()

    @Query("DELETE FROM workout_notes")
    suspend fun deleteAllWorkoutNotes()
}

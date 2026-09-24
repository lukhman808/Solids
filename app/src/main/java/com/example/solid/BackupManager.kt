package com.example.solid

import java.time.Instant
import java.time.ZoneId

object BackupManager {
    const val CURRENT_VERSION = 1

    suspend fun exportToMarkdown(dao: WorkoutDao): String {
        val sb = StringBuilder()
        sb.appendLine("---")
        sb.appendLine("solids_backup_version: $CURRENT_VERSION")
        sb.appendLine("exported_at: ${Instant.now()}")
        sb.appendLine("---")
        sb.appendLine()

        val sessions = dao.getAllSessionsOnce()
        val workouts = dao.getAllWorkoutsOnce()
        val sets = dao.getAllSetsOnce()

        sessions.forEach { session ->
            sb.appendLine("## SESSION")
            sb.appendLine("id: ${session.id}")
            sb.appendLine("name: ${session.name}")
            sb.appendLine("date: ${session.date}")
            sb.appendLine("color: ${session.color}")
            sb.appendLine("timestamp: ${session.timestamp}")
            sb.appendLine()

            workouts.filter { it.sessionId == session.id }.forEach { workout ->
                sb.appendLine("### WORKOUT")
                sb.appendLine("id: ${workout.id}")
                sb.appendLine("exerciseName: ${workout.exerciseName}")
                sb.appendLine()

                sets.filter { it.workoutId == workout.id }.forEach { set ->
                    sb.appendLine("#### SET")
                    sb.appendLine("reps: ${set.reps}")
                    sb.appendLine("weight: ${set.weight}")
                    sb.appendLine("isPartial: ${set.isPartial}")
                    sb.appendLine("difficulty: ${set.difficulty ?: "null"}")
                    sb.appendLine()
                }
            }
        }

        dao.getAllBodyWeightOnce().forEach { bw ->
            sb.appendLine("## BODYWEIGHT")
            sb.appendLine("weight: ${bw.weight}")
            sb.appendLine("date: ${bw.date}")
            sb.appendLine("timestamp: ${bw.timestamp}")
            sb.appendLine()
        }

        dao.getAllNoteEntriesOnce().forEach { note ->
            sb.appendLine("## NOTE_ENTRY")
            sb.appendLine("typeName: ${note.typeName}")
            sb.appendLine("note: ${note.note.replace("\n", "\\n")}")
            sb.appendLine("date: ${note.date}")
            sb.appendLine("timestamp: ${note.timestamp}")
            sb.appendLine()
        }

        dao.getAllWorkoutNotesOnce().forEach { wn ->
            sb.appendLine("## WORKOUT_NOTE")
            sb.appendLine("exerciseName: ${wn.exerciseName}")
            sb.appendLine("note: ${wn.note.replace("\n", "\\n")}")
            sb.appendLine()
        }

        return sb.toString()
    }

    data class ParsedBackup(
        val sessions: List<ParsedSession>,
        val bodyWeights: List<BodyWeightEntry>,
        val noteEntries: List<NoteEntry>,
        val workoutNotes: List<WorkoutNote>
    )
    data class ParsedSession(val session: Session, val workouts: List<ParsedWorkout>)
    data class ParsedWorkout(val workout: Workout, val sets: List<WorkoutSet>)

    fun parseMarkdown(content: String): ParsedBackup? {
        val lines = content.lines()
        var version = 0
        var i = 0
        while (i < lines.size && lines[i] != "---") i++
        i++
        while (i < lines.size && lines[i] != "---") {
            if (lines[i].startsWith("solids_backup_version:")) {
                version = lines[i].substringAfter(":").trim().toIntOrNull() ?: 0
            }
            i++
        }
        i++

        return when (version) {
            1 -> parseV1(lines, i)
            else -> null // unknown/unsupported format version
        }
    }

    private fun parseV1(lines: List<String>, startIndex: Int): ParsedBackup {
        val sessions = mutableListOf<ParsedSession>()
        val bodyWeights = mutableListOf<BodyWeightEntry>()
        val noteEntries = mutableListOf<NoteEntry>()
        val workoutNotes = mutableListOf<WorkoutNote>()

        var i = startIndex
        var currentSession: Session? = null
        var currentWorkouts = mutableListOf<ParsedWorkout>()
        var currentWorkout: Workout? = null
        var currentSets = mutableListOf<WorkoutSet>()

        fun flushWorkout() {
            currentWorkout?.let { currentWorkouts.add(ParsedWorkout(it, currentSets.toList())) }
            currentWorkout = null
            currentSets = mutableListOf()
        }
        fun flushSession() {
            flushWorkout()
            currentSession?.let { sessions.add(ParsedSession(it, currentWorkouts.toList())) }
            currentSession = null
            currentWorkouts = mutableListOf()
        }

        fun readFields(lines: List<String>, from: Int): Pair<Map<String, String>, Int> {
            val map = mutableMapOf<String, String>()
            var j = from
            while (j < lines.size && lines[j].isNotBlank() && !lines[j].startsWith("#")) {
                val line = lines[j]
                val key = line.substringBefore(":").trim()
                val value = line.substringAfter(":").trim()
                map[key] = value
                j++
            }
            return map to j
        }

        while (i < lines.size) {
            val line = lines[i]
            when {
                line == "## SESSION" -> {
                    flushSession()
                    val (fields, next) = readFields(lines, i + 1)
                    currentSession = Session(
                        name = fields["name"] ?: "",
                        date = fields["date"] ?: "",
                        color = fields["color"]?.toIntOrNull() ?: 0,
                        timestamp = fields["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                    )
                    i = next
                }
                line == "### WORKOUT" -> {
                    flushWorkout()
                    val (fields, next) = readFields(lines, i + 1)
                    currentWorkout = Workout(
                        sessionId = 0, // linked at insert time
                        exerciseName = fields["exerciseName"] ?: ""
                    )
                    i = next
                }
                line == "#### SET" -> {
                    val (fields, next) = readFields(lines, i + 1)
                    currentSets.add(
                        WorkoutSet(
                            workoutId = 0, // linked at insert time
                            reps = fields["reps"]?.toIntOrNull() ?: 0,
                            weight = fields["weight"]?.toFloatOrNull() ?: 0f,
                            isPartial = fields["isPartial"]?.toBooleanStrictOrNull() ?: false,
                            difficulty = fields["difficulty"]?.takeIf { it != "null" }
                        )
                    )
                    i = next
                }
                line == "## BODYWEIGHT" -> {
                    val (fields, next) = readFields(lines, i + 1)
                    bodyWeights.add(
                        BodyWeightEntry(
                            weight = fields["weight"]?.toFloatOrNull() ?: 0f,
                            date = fields["date"] ?: "",
                            timestamp = fields["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                        )
                    )
                    i = next
                }
                line == "## NOTE_ENTRY" -> {
                    val (fields, next) = readFields(lines, i + 1)
                    noteEntries.add(
                        NoteEntry(
                            typeName = fields["typeName"] ?: "",
                            note = (fields["note"] ?: "").replace("\\n", "\n"),
                            date = fields["date"] ?: "",
                            timestamp = fields["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
                        )
                    )
                    i = next
                }
                line == "## WORKOUT_NOTE" -> {
                    val (fields, next) = readFields(lines, i + 1)
                    workoutNotes.add(
                        WorkoutNote(
                            exerciseName = fields["exerciseName"] ?: "",
                            note = (fields["note"] ?: "").replace("\\n", "\n")
                        )
                    )
                    i = next
                }
                else -> i++
            }
        }
        flushSession()

        return ParsedBackup(sessions, bodyWeights, noteEntries, workoutNotes)
    }
}
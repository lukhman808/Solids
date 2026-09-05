package com.example.solid
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "workout_notes")
data class WorkoutNote(
    @PrimaryKey
    val exerciseName: String,
    val note: String
)
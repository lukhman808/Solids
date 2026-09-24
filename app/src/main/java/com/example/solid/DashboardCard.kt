package com.example.solid
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_cards")
data class DashboardCard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cardType: String,       // "CALENDAR" or "GRAPH"
    val scopeType: String,      // "ALL_WORKOUTS", "SESSION_NAME", "EXERCISE_NAME", "BODYWEIGHT", "NOTE_TYPE"
    val scopeValue: String?,    // e.g. "Push Day", "Bench Press", "CARDIO" — null when scopeType is ALL_WORKOUTS/BODYWEIGHT
    val metric: String = "WEIGHT", // GRAPH only: "WEIGHT", "REPS", "VOLUME" — ignored for CALENDAR
    val order: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
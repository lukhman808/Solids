package com.example.solid

data class SessionDateRow(val date: String, val timestamp: Long)
data class AllSessionDateRow(val name: String, val date: String, val timestamp: Long)
data class SetHistoryRow(
    val id: Int,
    val workoutId: Int,
    val reps: Int,
    val weight: Float,
    val isPartial: Boolean,
    val difficulty: String?,
    val sessionTimestamp: Long
)
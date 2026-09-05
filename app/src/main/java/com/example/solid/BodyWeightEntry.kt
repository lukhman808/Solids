package com.example.solid

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_weight_entries")
data class BodyWeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weight: Float,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
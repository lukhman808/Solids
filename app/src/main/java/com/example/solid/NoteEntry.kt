package com.example.solid
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_entries")
data class NoteEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val typeName: String,
    val note: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
package com.example.bodymindconnect.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")

data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val horizontalMood: Int, // 1-5 (negative to positive)
    val verticalMood: Int,   // 1-5 (low energy to high energy)
    val locationId: Long? = null,
    val calendarEventId: Long? = null
)
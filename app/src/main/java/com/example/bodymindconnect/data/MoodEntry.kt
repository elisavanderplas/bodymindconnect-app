package com.example.bodymindconnect.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val moodValue: Float,
    val energyValue: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
) {
    fun getFormattedDate(): String {
        return android.text.format.DateFormat.format("dd-MM-yyyy HH:mm", Date(timestamp)).toString()
    }

    fun getMoodDescription(): String {
        return when (moodValue) {
            in 0f..2f -> "Zeer Laag"
            in 3f..4f -> "Laag"
            in 5f..6f -> "Neutraal"
            in 7f..8f -> "Goed"
            else -> "Uitstekend"
        }
    }

    fun getEnergyDescription(): String {
        return when (energyValue) {
            in 0f..2f -> "Zeer Laag"
            in 3f..4f -> "Laag"
            in 5f..6f -> "Normaal"
            in 7f..8f -> "Hoog"
            else -> "Zeer Hoog"
        }
    }
}
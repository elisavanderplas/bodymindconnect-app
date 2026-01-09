// MoodDao.kt
package com.example.bodymindconnect.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bodymindconnect.model.MoodEntry

@Dao
interface MoodDao {
    @Insert
    suspend fun insert(moodEntry: MoodEntry)
    
    @Query("SELECT * FROM mood_entries")
    suspend fun getAll(): List<MoodEntry>
    
    @Query("SELECT * FROM mood_entries WHERE calendarEventId = :eventId")
    suspend fun getByEventId(eventId: Long): List<MoodEntry>
    
    @Query("SELECT * FROM mood_entries WHERE locationId = :locationId")
    suspend fun getByLocationId(locationId: Long): List<MoodEntry>
}
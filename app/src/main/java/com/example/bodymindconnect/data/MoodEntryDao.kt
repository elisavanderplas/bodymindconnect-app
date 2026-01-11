package com.example.bodymindconnect.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodEntryDao {

    @Insert
    suspend fun insert(moodEntry: MoodEntry)

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEntry(): MoodEntry?

    @Query("SELECT COUNT(*) FROM mood_entries")
    suspend fun getTotalEntriesCount(): Int

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()
}
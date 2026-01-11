package com.example.bodymindconnect.data

import kotlinx.coroutines.flow.Flow

class MoodRepository(private val moodEntryDao: MoodEntryDao) {

    val allEntries: Flow<List<MoodEntry>> = moodEntryDao.getAllEntries()

    suspend fun insert(moodEntry: MoodEntry) {
        moodEntryDao.insert(moodEntry)
    }

    suspend fun getLatestEntry(): MoodEntry? {
        return moodEntryDao.getLatestEntry()
    }

    suspend fun getTotalEntriesCount(): Int {
        return moodEntryDao.getTotalEntriesCount()
    }
}
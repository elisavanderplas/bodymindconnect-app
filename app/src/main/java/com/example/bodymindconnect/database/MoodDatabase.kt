// MoodDatabase.kt
package com.example.bodymindconnect.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.bodymindconnect.model.MoodEntry

@Database(entities = [MoodEntry::class], version = 1, exportSchema = false)
abstract class MoodDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    
    companion object {
        @Volatile
        private var INSTANCE: MoodDatabase? = null
        
        fun getInstance(context: Context): MoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodDatabase::class.java,
                    "mood_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
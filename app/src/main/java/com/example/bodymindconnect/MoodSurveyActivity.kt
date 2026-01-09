package com.example.bodymindconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bodymindconnect.database.MoodDatabase
import com.example.bodymindconnect.model.MoodEntry
import kotlinx.coroutines.launch

import com.example.bodymindconnect.CalendarReader
class MoodSurveyActivity : AppCompatActivity() {
    private lateinit var locationTracker: LocationTracker
    private lateinit var moodDatabase: MoodDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_survey)

        // Initialize components
        locationTracker = LocationTracker(this)
        moodDatabase = MoodDatabase.getInstance(this)

        // Start location tracking
        locationTracker.startTracking()

        // Set up your UI components and listeners here
        // For example, set up button click listeners that call saveMoodWithContext()
    }

    private fun saveMoodWithContext(horizontalMood: Int, verticalMood: Int) {
        // Get current location and event
        val currentLocation = locationTracker.getCurrentLocation()
        val currentEvent = CalendarReader.getCurrentEvent(this)

        // Create a MoodEntry
        val moodEntry = MoodEntry(
            timestamp = System.currentTimeMillis(),
            horizontalMood = horizontalMood,
            verticalMood = verticalMood,
            locationId = currentLocation?.id,
            calendarEventId = currentEvent?.id
        )

        // Save to database using coroutines
        lifecycleScope.launch {
            moodDatabase.moodDao().insert(moodEntry)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopTracking()
    }
}
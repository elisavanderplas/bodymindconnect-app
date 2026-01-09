package com.example.bodymindconnect

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.bodymindconnect.database.MoodDatabase
import com.example.bodymindconnect.model.MoodEntry
import kotlinx.coroutines.launch

class MoodSurveyActivity : AppCompatActivity() {
    private lateinit var locationTracker: LocationTracker
    private lateinit var moodDatabase: MoodDatabase

    // Add these variables for the UI elements
    private lateinit var horizontalMoodSlider: SeekBar
    private lateinit var verticalMoodSlider: SeekBar
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_survey)

        // Initialize UI components
        horizontalMoodSlider = findViewById(R.id.horizontalMoodSlider)
        verticalMoodSlider = findViewById(R.id.verticalMoodSlider)
        submitButton = findViewById(R.id.submitMoodButton)

        // MAKE SEEKBARS RED
        makeSeekBarsRed()

        // Initialize other components
        locationTracker = LocationTracker(this)
        moodDatabase = MoodDatabase.getInstance(this)

        // Start location tracking
        locationTracker.startTracking()

        // Set up button click listener
        submitButton.setOnClickListener {
            saveMoodWithContext()
        }

        // Optional: Add listeners to see values changing
        horizontalMoodSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // You could update a text view showing the value
                // Example: moodValueText.text = "Mood: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing
            }
        })
    }

    private fun makeSeekBarsRed() {
        // Get red color from resources
        val redColor = ContextCompat.getColor(this, R.color.red)

        // Create light red (20% opacity of your red color)
        // Your red is #F15A5E which is RGB(241, 90, 94)
        // Light red with 20% opacity: #33F15A5E
        val lightRedColor = Color.argb(51, 241, 90, 94) // 51 = 20% opacity

        // Apply to horizontal slider
        horizontalMoodSlider.progressTintList = android.content.res.ColorStateList.valueOf(redColor)
        horizontalMoodSlider.progressBackgroundTintList = android.content.res.ColorStateList.valueOf(lightRedColor)
        horizontalMoodSlider.thumbTintList = android.content.res.ColorStateList.valueOf(redColor)

        // Apply to vertical slider
        verticalMoodSlider.progressTintList = android.content.res.ColorStateList.valueOf(redColor)
        verticalMoodSlider.progressBackgroundTintList = android.content.res.ColorStateList.valueOf(lightRedColor)
        verticalMoodSlider.thumbTintList = android.content.res.ColorStateList.valueOf(redColor)

        // Also make the Submit button red
        submitButton.backgroundTintList = android.content.res.ColorStateList.valueOf(redColor)
    }

    private fun saveMoodWithContext() {
        // Get current values from sliders
        val horizontalMood = horizontalMoodSlider.progress
        val verticalMood = verticalMoodSlider.progress

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

            // Optional: Show confirmation or navigate back
            runOnUiThread {
                // Show a toast or message
                android.widget.Toast.makeText(
                    this@MoodSurveyActivity,
                    "Mood saved successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()

                // Optional: Reset sliders to middle
                horizontalMoodSlider.progress = 2
                verticalMoodSlider.progress = 2
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationTracker.stopTracking()
    }
}
package com.example.bodymindconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bodymindconnect.data.AppDatabase
import com.example.bodymindconnect.data.MoodEntry
import com.example.bodymindconnect.data.MoodRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoodSurveyActivity : AppCompatActivity() {

    private lateinit var moodSlider: Slider
    private lateinit var moodValueText: TextView
    private lateinit var submitButton: Button
    private lateinit var energyValueText: TextView
    private lateinit var energySlider: Slider
    private var currentMoodValue: Float = 5f
    private var currentEnergyValue: Float = 5f

    private lateinit var repository: MoodRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_survey)

        // Initialize database repository
        val database = AppDatabase.getDatabase(this)
        repository = MoodRepository(database.moodEntryDao())

        // Initialize views
        moodSlider = findViewById(R.id.mood_slider)
        moodValueText = findViewById(R.id.mood_value_text)
        submitButton = findViewById(R.id.submit_button)
        energyValueText = findViewById(R.id.energy_value_text)
        energySlider = findViewById(R.id.energy_slider)

        // Setup the mood slider
        setupMoodSlider()

        // Setup the energy slider
        setupEnergySlider()

        // Setup submit button
        setupSubmitButton()

        // Setup bottom navigation
        setupBottomNavigation()

        Toast.makeText(this, "Stemmingsonderzoek Geladen", Toast.LENGTH_SHORT).show()
    }

    private fun setupMoodSlider() {
        // Configure the mood slider
        moodSlider.apply {
            valueFrom = 0f
            valueTo = 10f
            stepSize = 1f
            value = 5f // Default middle value

            // Update the text when slider value changes
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    currentMoodValue = value
                    updateMoodText(value)
                }
            }

            // Also update on slide start
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    // Optional: You can add some feedback here
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    currentMoodValue = slider.value
                    updateMoodText(slider.value)
                }
            })
        }

        // Set initial text
        updateMoodText(moodSlider.value)
    }

    private fun updateMoodText(value: Float) {
        val moodText = when (value) {
            in 0f..2f -> "Zeer Laag ($value)"
            in 3f..4f -> "Laag ($value)"
            in 5f..6f -> "Neutraal ($value)"
            in 7f..8f -> "Goed ($value)"
            else -> "Uitstekend ($value)"
        }
        moodValueText.text = "Huidige Stemming: $moodText"
    }

    private fun setupEnergySlider() {
        // Configure the energy slider
        energySlider.apply {
            valueFrom = 0f
            valueTo = 10f
            stepSize = 1f
            value = 5f // Default middle value

            // Update the text when slider value changes
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    currentEnergyValue = value
                    updateEnergyText(value)
                }
            }

            // Also update on slide start
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                    // Optional: You can add some feedback here
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    currentEnergyValue = slider.value
                    updateEnergyText(slider.value)
                }
            })
        }

        // Set initial text
        updateEnergyText(energySlider.value)
    }

    private fun updateEnergyText(value: Float) {
        val energyText = when (value) {
            in 0f..2f -> "Zeer Laag ($value)"
            in 3f..4f -> "Laag ($value)"
            in 5f..6f -> "Normaal ($value)"
            in 7f..8f -> "Hoog ($value)"
            else -> "Zeer Hoog ($value)"
        }
        energyValueText.text = "Huidige Energie: $energyText"
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            // Save both mood and energy data to database
            saveMoodDataToDatabase(currentMoodValue, currentEnergyValue)

            // Show confirmation in Dutch
            val confirmationText = "Stemming: $currentMoodValue, Energie: $currentEnergyValue opgeslagen"
            Toast.makeText(this, confirmationText, Toast.LENGTH_SHORT).show()

            // Navigate back to home
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveMoodDataToDatabase(moodValue: Float, energyValue: Float) {
        // Create a new MoodEntry
        val moodEntry = MoodEntry(
            moodValue = moodValue,
            energyValue = energyValue,
            timestamp = System.currentTimeMillis()
        )

        // Save to database using coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.insert(moodEntry)
                // Optional: Show success message
                runOnUiThread {
                    Toast.makeText(this@MoodSurveyActivity, "Gegevens opgeslagen!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MoodSurveyActivity, "Fout bij opslaan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Set listener for navigation items
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Go to MainActivity (Home)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_dashboard -> {
                    // Go to MainActivity with dashboard selected
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("SELECTED_TAB", "dashboard")
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_mood_survey -> {
                    // Already on survey page - show message in Dutch
                    Toast.makeText(this, "Je bent al op de vragenlijst pagina", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_notifications -> {
                    // Go to MainActivity with notifications selected
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("SELECTED_TAB", "notifications")
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Highlight the current item (Survey)
        bottomNav.selectedItemId = R.id.navigation_mood_survey
    }
}
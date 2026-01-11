package com.example.bodymindconnect

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class MoodSurveyActivity : AppCompatActivity() {

    private lateinit var moodSlider: Slider
    private lateinit var moodValueText: TextView
    private lateinit var submitButton: Button
    private lateinit var energyValueText: TextView
    private lateinit var energySlider: Slider
    private var currentMoodValue: Float = 5f
    private var currentEnergyValue: Float = 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_survey)

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
            // Save both mood and energy data
            saveMoodData(currentMoodValue, currentEnergyValue)

            // Show confirmation in Dutch
            val confirmationText = "Stemming: $currentMoodValue, Energie: $currentEnergyValue opgeslagen"
            Toast.makeText(this, confirmationText, Toast.LENGTH_SHORT).show()

            // Navigate back to home
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveMoodData(moodValue: Float, energyValue: Float) {
        val sharedPref = getSharedPreferences("mood_data", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Get current entry count
        val entryCount = sharedPref.getInt("entry_count", 0)
        val newEntryNumber = entryCount + 1

        // Save this entry
        editor.putFloat("mood_$newEntryNumber", moodValue)
        editor.putFloat("energy_$newEntryNumber", energyValue)
        editor.putLong("timestamp_$newEntryNumber", System.currentTimeMillis())

        // Update entry count
        editor.putInt("entry_count", newEntryNumber)

        // Also save as latest for quick access
        editor.putFloat("latest_mood", moodValue)
        editor.putFloat("latest_energy", energyValue)
        editor.putLong("latest_timestamp", System.currentTimeMillis())

        editor.apply()
    }
}
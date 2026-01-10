package com.example.bodymindconnect

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoodSurveyService : Service() {
    private val AUTO_SURVEY_INTERVAL = 2 * 60 * 60 * 1000L // 2 hours for auto-popup
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private lateinit var prefs: SharedPreferences

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prefs = getSharedPreferences("survey_prefs", MODE_PRIVATE)

        // Start auto-survey timer (every 2 hours)
        startAutoSurveyTimer()

        return START_STICKY
    }

    private fun startAutoSurveyTimer() {
        serviceJob = serviceScope.launch {
            // Initial delay - don't show immediately on app start
            delay(10000) // 10 seconds

            while (true) {
                showAutoMoodSurvey()
                delay(AUTO_SURVEY_INTERVAL) // Wait 2 hours
            }
        }
    }

    private fun showAutoMoodSurvey() {
        // Get last AUTO survey time (not manual surveys)
        val lastAutoSurveyTime = prefs.getLong("last_auto_survey_time", 0)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAutoSurvey = currentTime - lastAutoSurveyTime

        // Get last navigation time
        val lastNavTime = prefs.getLong("last_nav_from_survey", 0)
        val timeSinceLastNav = currentTime - lastNavTime

        // Only show AUTO survey if:
        // 1. At least 2 hours since last AUTO survey AND
        // 2. At least 1 minute since user navigated away
        if (timeSinceLastAutoSurvey >= AUTO_SURVEY_INTERVAL && timeSinceLastNav >= 60000) {
            // Save this AUTO survey time
            prefs.edit().putLong("last_auto_survey_time", currentTime).apply()

            val intent = Intent(this, MoodSurveyActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("survey_type", "auto") // Mark as auto survey
            }
            startActivity(intent)
        }
    }

    // NEW METHOD: Manually trigger survey (for button clicks)
    fun triggerManualSurvey() {
        serviceScope.launch {
            val intent = Intent(this@MoodSurveyService, MoodSurveyActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("survey_type", "manual") // Mark as manual survey
            }
            startActivity(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel()
    }
}
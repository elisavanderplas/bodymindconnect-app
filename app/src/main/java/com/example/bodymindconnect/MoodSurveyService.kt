package com.example.bodymindconnect

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoodSurveyService : Service() {
    private val surveyInterval = 2 * 60 * 60 * 1000L // 2 hours
    private var serviceJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startSurveyTimer()
        return START_STICKY
    }

    private fun startSurveyTimer() {
        serviceJob = serviceScope.launch {
            while (true) {
                showMoodSurvey()
                delay(surveyInterval) // Wait for the interval
            }
        }
    }

    private fun showMoodSurvey() {
        val intent = Intent(this, MoodSurveyActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob?.cancel() // Stop the coroutine when service is destroyed
    }
}
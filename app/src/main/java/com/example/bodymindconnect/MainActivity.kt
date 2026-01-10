package com.example.bodymindconnect

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bodymindconnect.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

object PermissionUtils {
    const val PERMISSION_REQUEST_CODE = 1001

    fun checkAndRequestPermissions(activity: Activity): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle navigation from other activities
        val selectedTab = intent.getStringExtra("SELECTED_TAB")
        selectedTab?.let {
            Log.d("MainActivity", "Received SELECTED_TAB: $it")
            when (it) {
                "dashboard" -> {
                    // Navigate to dashboard fragment
                    val navController = findNavController(R.id.nav_host_fragment_activity_main)
                    binding.root.post {
                        navController.navigate(R.id.navigation_dashboard)
                    }
                }
                "notifications" -> {
                    // Navigate to notifications fragment
                    val navController = findNavController(R.id.nav_host_fragment_activity_main)
                    binding.root.post {
                        navController.navigate(R.id.navigation_notifications)
                    }
                }
            }
        }

        // Set up navigation
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define which fragments are top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
                // Note: R.id.navigation_mood_survey is NOT included because it's not a fragment
            )
        )

        // Setup ActionBar with navController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup BottomNavigationView with custom item selection listener
        // instead of setupWithNavController since we have custom behavior for mood survey
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mood_survey -> {
                    // Survey opens MoodSurveyActivity (not a fragment)
                    val intent = Intent(this, MoodSurveyActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_home -> {
                    // Navigate to home fragment
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_dashboard -> {
                    // Navigate to dashboard fragment
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_notifications -> {
                    // Navigate to notifications fragment
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                else -> false
            }
        }

        // Set initial selection
        navView.selectedItemId = R.id.navigation_home

        // Create notification channel for Android 8.0+
        createNotificationChannel()

        // Start the MoodSurveyService
        startMoodSurveyService()

        // Check and request permissions
        PermissionUtils.checkAndRequestPermissions(this)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mood Surveys"
            val descriptionText = "Channel for mood survey notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("mood_survey_channel", name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startMoodSurveyService() {
        val serviceIntent = Intent(this, MoodSurveyService::class.java)

        // Use ContextCompat.startForegroundService for Android 8.0+ to avoid crashes
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            // Handle permission results
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                // All permissions granted, you can proceed
                Log.d("MainActivity", "All permissions granted")
            } else {
                // Some permissions denied, handle accordingly
                Log.w("MainActivity", "Some permissions were denied")
            }
        }
    }

    // Optional: Handle up navigation
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
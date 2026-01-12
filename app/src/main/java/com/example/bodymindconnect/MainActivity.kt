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
            Manifest.permission.WRITE_CALENDAR,
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
    private lateinit var locationTracker: LocationTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize location tracker
        locationTracker = LocationTracker(this)

        // Hide the default ActionBar
        supportActionBar?.hide()

        // Handle navigation from other activities
        val selectedTab = intent.getStringExtra("SELECTED_TAB")
        selectedTab?.let {
            Log.d("MainActivity", "Received SELECTED_TAB: $it")
            when (it) {
                "dashboard" -> {
                    // Navigate to dashboard fragment
                    val navController = findNavController(R.id.nav_host_fragment_activity_main)
                    // Use post to ensure navigation happens after setup
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
            )
        )

        // Setup ActionBar with navController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup BottomNavigationView with custom item selection listener
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
        checkAndRequestPermissions()

        // Start location tracking if permission granted
        if (hasLocationPermission()) {
            startLocationTracking()
        }
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

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PermissionUtils.PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d("MainActivity", "All permissions already granted")
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationTracking() {
        Log.d("MainActivity", "Starting location tracking")
        locationTracker.startTracking()

        // Check if tracking started successfully
        if (locationTracker.isTracking()) {
            Log.i("MainActivity", "Location tracking started successfully")
        } else {
            Log.w("MainActivity", "Location tracking failed to start")
        }
    }

    private fun stopLocationTracking() {
        Log.d("MainActivity", "Stopping location tracking")
        locationTracker.stopTracking()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                Log.d("MainActivity", "All permissions granted")
                // Start location tracking now that permission is granted
                startLocationTracking()
            } else {
                Log.w("MainActivity", "Some permissions were denied")
                // Check if location permission was granted
                permissions.forEachIndexed { index, permission ->
                    if (permission == Manifest.permission.ACCESS_FINE_LOCATION ||
                        permission == Manifest.permission.ACCESS_COARSE_LOCATION) {
                        if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                            Log.d("MainActivity", "Location permission granted")
                            startLocationTracking()
                        } else {
                            Log.w("MainActivity", "Location permission denied")
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Restart location tracking if app comes to foreground
        Log.d("MainActivity", "App resumed, checking location tracking")
        if (hasLocationPermission()) {
            startLocationTracking()
        }
    }

    override fun onPause() {
        super.onPause()
        // Optionally stop tracking when app goes to background to save battery
        // Uncomment if you want to stop tracking in background
        // stopLocationTracking()
        Log.d("MainActivity", "App paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop tracking when app is destroyed
        stopLocationTracking()
        Log.d("MainActivity", "App destroyed, location tracking stopped")
    }

    // Optional: Handle up navigation
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
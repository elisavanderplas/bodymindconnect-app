package com.example.bodymindconnect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bodymindconnect.model.LocationEntry

class LocationTracker(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var currentLocation: LocationEntry? = null
    private var locationStartTime: Long = 0
    private var isTracking = false

    // Helper method to update location
    private fun updateCurrentLocation(location: Location) {
        Log.d("LocationTracker", "Updating location: ${location.latitude}, ${location.longitude}")

        if (currentLocation == null) {
            // Starting a new location session
            locationStartTime = System.currentTimeMillis()
            currentLocation = LocationEntry(
                id = 0,
                latitude = location.latitude,
                longitude = location.longitude,
                startTime = locationStartTime,
                endTime = locationStartTime,
                address = null,
                accuracy = location.accuracy
            )
        } else {
            // Update existing location session
            currentLocation = currentLocation!!.copy(
                endTime = System.currentTimeMillis(),
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy
            )
        }

        // Save to database if significant time has passed
        if (shouldSaveLocation()) {
            saveCurrentLocation()
        }
    }

    // Location listener for continuous updates
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateCurrentLocation(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d("LocationTracker", "Provider $provider status changed: $status")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d("LocationTracker", "Provider $provider enabled")
        }

        override fun onProviderDisabled(provider: String) {
            Log.d("LocationTracker", "Provider $provider disabled")
        }
    }

    fun startTracking() {
        if (isTracking) {
            Log.w("LocationTracker", "Already tracking location")
            return
        }

        if (!hasLocationPermission()) {
            Log.e("LocationTracker", "Location permission not granted")
            return
        }

        try {
            isTracking = true
            Log.d("LocationTracker", "Starting location tracking")

            // First, try to get last known location immediately
            val lastKnownLocation = getLastKnownLocation()
            lastKnownLocation?.let { location ->
                updateCurrentLocation(location)
            }

            // Request continuous updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,  // 5 seconds - more frequent updates
                5f,    // 5 meters - minimum distance change
                locationListener,
                Looper.getMainLooper()
            )

            // Also request network updates as backup
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                10000,  // 10 seconds
                10f,    // 10 meters
                locationListener,
                Looper.getMainLooper()
            )

            Log.i("LocationTracker", "Location tracking started successfully")

        } catch (e: SecurityException) {
            Log.e("LocationTracker", "Security exception: ${e.message}")
            isTracking = false
        } catch (e: Exception) {
            Log.e("LocationTracker", "Error starting location tracking: ${e.message}")
            isTracking = false
        }
    }

    fun stopTracking() {
        if (!isTracking) return

        try {
            locationManager.removeUpdates(locationListener)
            saveCurrentLocation()
            isTracking = false
            Log.i("LocationTracker", "Location tracking stopped")
        } catch (e: Exception) {
            Log.e("LocationTracker", "Error stopping location tracking: ${e.message}")
        }
    }

    private fun getLastKnownLocation(): Location? {
        return try {
            if (hasLocationPermission()) {
                // Try GPS first
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                null
            }
        } catch (e: SecurityException) {
            Log.e("LocationTracker", "Security exception getting last known location: ${e.message}")
            null
        }
    }

    private fun shouldSaveLocation(): Boolean {
        return currentLocation != null &&
                (System.currentTimeMillis() - currentLocation!!.startTime) > MIN_LOCATION_DURATION
    }

    private fun saveCurrentLocation() {
        currentLocation?.let {
            // Save to SharedPreferences for now (you can change to Room DB later)
            saveLocationToPrefs(it)
            Log.d("LocationTracker", "Location saved: ${it.latitude}, ${it.longitude}")
            currentLocation = null
        }
    }

    private fun saveLocationToPrefs(location: LocationEntry) {
        val sharedPref = context.getSharedPreferences("location_data", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putFloat("last_latitude", location.latitude.toFloat())
            putFloat("last_longitude", location.longitude.toFloat())
            putLong("last_location_time", location.endTime)
            putFloat("last_accuracy", location.accuracy ?: 0f)
            apply()
        }
    }

    fun getCurrentLocation(): LocationEntry? {
        // First check if we have a current tracking session
        if (currentLocation != null) {
            return currentLocation
        }

        // Otherwise, get from saved preferences
        return getLastSavedLocation()
    }

    private fun getLastSavedLocation(): LocationEntry? {
        val sharedPref = context.getSharedPreferences("location_data", Context.MODE_PRIVATE)
        val latitude = sharedPref.getFloat("last_latitude", 0f).toDouble()
        val longitude = sharedPref.getFloat("last_longitude", 0f).toDouble()
        val timestamp = sharedPref.getLong("last_location_time", 0L)
        val accuracy = sharedPref.getFloat("last_accuracy", 0f)

        return if (latitude != 0.0 && longitude != 0.0 && timestamp > 0) {
            LocationEntry(
                id = 0,
                latitude = latitude,
                longitude = longitude,
                startTime = timestamp,
                endTime = timestamp,
                address = null,
                accuracy = accuracy
            )
        } else {
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun isTracking(): Boolean {
        return isTracking
    }

    companion object {
        private const val MIN_LOCATION_DURATION = 60000 // 1 minute
        private const val TAG = "LocationTracker"
    }
}
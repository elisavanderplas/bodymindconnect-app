package com.example.bodymindconnect

import android.content.Context
import android.os.Looper
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

import com.example.bodymindconnect.model.LocationEntry

class LocationTracker(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var currentLocation: LocationEntry? = null
    private var locationStartTime: Long = 0

    // Location listener for updates
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (currentLocation == null) {
                // Starting a new location session
                locationStartTime = System.currentTimeMillis()
                currentLocation = LocationEntry(
                    id = 0,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    startTime = locationStartTime,
                    endTime = locationStartTime,
                    address = null
                )
            } else {
                // Update existing location session
                currentLocation = currentLocation!!.copy(
                    endTime = System.currentTimeMillis()
                )
            }

            // Save to database if significant time has passed
            if (shouldSaveLocation()) {
                saveCurrentLocation()
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun startTracking() {
        try {
            // Request location updates
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000,  // 10 seconds
                10f,    // 10 meters
                locationListener,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            // Handle permission exception
            e.printStackTrace()
        }
    }

    fun stopTracking() {
        locationManager.removeUpdates(locationListener)
        saveCurrentLocation()
    }

    private fun shouldSaveLocation(): Boolean {
        return currentLocation != null &&
                (System.currentTimeMillis() - currentLocation!!.startTime) > MIN_LOCATION_DURATION
    }

    private fun saveCurrentLocation() {
        currentLocation?.let {
            // Save to your database implementation
            // database.locationDao().insert(it)
            currentLocation = null
        }
    }

    fun getCurrentLocation(): LocationEntry? {
        return currentLocation
    }

    companion object {
        private const val MIN_LOCATION_DURATION = 300000 // 5 minutes
    }
}
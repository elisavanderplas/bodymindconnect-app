package com.example.bodymindconnect.model

data class LocationEntry(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val startTime: Long,
    val endTime: Long,
    val address: String?,
    val accuracy: Float? = null
) {
    fun getFormattedCoordinates(): String {
        return String.format("%.6f, %.6f", latitude, longitude)
    }

    fun getAccuracyInMeters(): String {
        return if (accuracy != null) {
            String.format("%.1f m", accuracy)
        } else {
            "Onbekend"
        }
    }

    fun getDurationMinutes(): Long {
        return (endTime - startTime) / (60 * 1000)
    }
}
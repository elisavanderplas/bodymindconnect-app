package com.example.bodymindconnect.model

data class LocationEntry(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val startTime: Long,
    val endTime: Long,
    val address: String?
)
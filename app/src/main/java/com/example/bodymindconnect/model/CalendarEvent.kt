package com.example.bodymindconnect.model

data class CalendarEvent(
    val id: Long = 0,
    val title: String,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val participants: List<String>,
    val location: String?
)
package com.example.bodymindconnect

import android.content.Context
import android.provider.CalendarContract
import com.example.bodymindconnect.model.CalendarEvent

object CalendarReader {

    fun getCalendarEvents(context: Context, startTime: Long, endTime: Long): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION
        )

        val cursor = context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTEND} <= ?",
            arrayOf(startTime.toString(), endTime.toString()),
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val eventId = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events._ID))
                val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                val description = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION))
                val dtStart = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART))
                val dtEnd = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Events.DTEND))
                val location = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION))

                val participants = getEventParticipants(context, eventId)

                events.add(CalendarEvent(eventId, title, description, dtStart, dtEnd, participants, location))
            }
        }

        return events
    }

    private fun getEventParticipants(context: Context, eventId: Long): List<String> {
        val participants = mutableListOf<String>()
        val attendeeProjection: Array<String> = arrayOf(CalendarContract.Attendees.ATTENDEE_EMAIL)

        val cursor = context.contentResolver.query(
            CalendarContract.Attendees.CONTENT_URI,
            attendeeProjection,
            "${CalendarContract.Attendees.EVENT_ID} = ?",
            arrayOf(eventId.toString()),
            null
        )

        cursor?.use {
            val emailColumnIndex = it.getColumnIndex(CalendarContract.Attendees.ATTENDEE_EMAIL)
            while (it.moveToNext()) {
                if (emailColumnIndex != -1) {
                    val email = it.getString(emailColumnIndex)
                    if (!email.isNullOrEmpty()) {
                        participants.add(email)
                    }
                }
            }
        }

        return participants
    }

    // Add this method to get the current event
    fun getCurrentEvent(context: Context): CalendarEvent? {
        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000

        // Get events from one hour ago to one hour from now
        val events = getCalendarEvents(context, now - oneHourMs, now + oneHourMs)

        // Find the event that is currently happening
        return events.firstOrNull { event ->
            event.startTime <= now && event.endTime >= now
        }
    }
}
package com.example.bodymindconnect.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.bodymindconnect.CalendarReader
import com.example.bodymindconnect.LocationTracker
import com.example.bodymindconnect.model.ActivityType
import com.example.bodymindconnect.model.CalendarEvent
import com.example.bodymindconnect.model.LocationEntry
import java.text.SimpleDateFormat
import java.util.*

class DataCombiner(private val context: Context) {

    private val sharedPref: SharedPreferences = context.getSharedPreferences("mood_data", Context.MODE_PRIVATE)
    private val locationTracker = LocationTracker(context)

    data class CombinedData(
        val moodData: MoodSummary,
        val calendarEvents: List<CalendarEvent>,
        val currentEvent: CalendarEvent?,
        val locationData: LocationEntry?,
        val insights: List<String>
    )

    data class MoodSummary(
        val totalEntries: Int,
        val latestMood: Float,
        val latestEnergy: Float,
        val latestTimestamp: Long,
        val averageMood: Float,
        val averageEnergy: Float,
        val allEntries: List<MoodEntry>
    )

    data class MoodEntry(
        val moodValue: Float,
        val energyValue: Float,
        val timestamp: Long
    )

    fun getCombinedData(): CombinedData {
        val moodSummary = getMoodSummary()
        val calendarEvents = getTodaysCalendarEvents()
        val currentEvent = getCurrentEvent()
        val locationData = getCurrentLocation()
        val insights = generateInsights(moodSummary, calendarEvents, currentEvent, locationData)

        return CombinedData(
            moodData = moodSummary,
            calendarEvents = calendarEvents,
            currentEvent = currentEvent,
            locationData = locationData,
            insights = insights
        )
    }

    private fun getMoodSummary(): MoodSummary {
        val entryCount = sharedPref.getInt("entry_count", 0)

        if (entryCount == 0) {
            return MoodSummary(
                totalEntries = 0,
                latestMood = 0f,
                latestEnergy = 0f,
                latestTimestamp = 0L,
                averageMood = 0f,
                averageEnergy = 0f,
                allEntries = emptyList()
            )
        }

        val latestMood = sharedPref.getFloat("latest_mood", 0f)
        val latestEnergy = sharedPref.getFloat("latest_energy", 0f)
        val latestTimestamp = sharedPref.getLong("latest_timestamp", 0L)

        val allEntries = mutableListOf<MoodEntry>()
        var totalMood = 0f
        var totalEnergy = 0f

        for (i in 1..entryCount) {
            val mood = sharedPref.getFloat("mood_$i", 0f)
            val energy = sharedPref.getFloat("energy_$i", 0f)
            val timestamp = sharedPref.getLong("timestamp_$i", 0L)

            totalMood += mood
            totalEnergy += energy

            allEntries.add(MoodEntry(mood, energy, timestamp))
        }

        val avgMood = if (entryCount > 0) totalMood / entryCount else 0f
        val avgEnergy = if (entryCount > 0) totalEnergy / entryCount else 0f

        return MoodSummary(
            totalEntries = entryCount,
            latestMood = latestMood,
            latestEnergy = latestEnergy,
            latestTimestamp = latestTimestamp,
            averageMood = avgMood,
            averageEnergy = avgEnergy,
            allEntries = allEntries
        )
    }

    private fun getTodaysCalendarEvents(): List<CalendarEvent> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis - 1

            CalendarReader.getCalendarEvents(context, startOfDay, endOfDay)
        } catch (e: SecurityException) {
            // Permission not granted
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getCurrentEvent(): CalendarEvent? {
        return try {
            CalendarReader.getCurrentEvent(context)
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentLocation(): LocationEntry? {
        return try {
            locationTracker.getCurrentLocation()
        } catch (e: Exception) {
            null
        }
    }

    private fun generateInsights(
        moodSummary: MoodSummary,
        calendarEvents: List<CalendarEvent>,
        currentEvent: CalendarEvent?,
        locationData: LocationEntry?
    ): List<String> {
        val insights = mutableListOf<String>()

        // Mood-based insights
        if (moodSummary.totalEntries > 0) {
            if (moodSummary.latestMood < 3) {
                insights.add("💭 Je stemming is laag. Overweeg een pauze of iets leuks te doen.")
            } else if (moodSummary.latestMood > 7) {
                insights.add("🎉 Je voelt je geweldig! Perfect moment om productief te zijn.")
            }

            if (moodSummary.latestEnergy < 3) {
                insights.add("⚡ Lage energie gedetecteerd. Zorg dat je gehydrateerd en uitgerust bent.")
            }
        }

        // Calendar-based insights
        if (calendarEvents.isNotEmpty()) {
            insights.add("📅 Je hebt ${calendarEvents.size} afspraak(en) vandaag.")

            // Analyze event types
            val eventTypes = calendarEvents.groupBy { it.activityType }
            eventTypes.forEach { (type, events) ->
                if (events.isNotEmpty()) {
                    val typeName = when (type) {
                        ActivityType.MEETING -> "vergaderingen"
                        ActivityType.WORK -> "werkafspraken"
                        ActivityType.SOCIAL -> "sociale activiteiten"
                        ActivityType.EXERCISE -> "sportmomenten"
                        ActivityType.MEAL -> "maaltijden"
                        ActivityType.TRAVEL -> "reistijd"
                        ActivityType.PERSONAL -> "persoonlijke afspraken"
                        else -> "andere activiteiten"
                    }
                    insights.add("📊 ${events.size} $typeName vandaag")
                }
            }

            // Current event insight
            currentEvent?.let { event ->
                insights.add("⏰ Nu bezig: ${event.title ?: "Afspraak"} (${event.getFormattedDuration()})")
                insights.add("👥 ${event.getFormattedParticipants()}")
            }
        }

        // Location-based insights
        locationData?.let {
            insights.add("📍 Laatste locatie: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.startTime))}")

            // If at work location during work hours
            val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (hourOfDay in 9..17) {
                insights.add("🏢 Waarschijnlijk op werk (werkuren)")
            }
        }

        // Combined mood-calendar insights
        if (moodSummary.totalEntries > 0) {
            val busyDay = calendarEvents.size > 3
            val lowMood = moodSummary.latestMood < 4

            if (busyDay && lowMood) {
                insights.add("📊 Drukke dag en lage stemming - overweeg prioriteiten te stellen")
            }

            if (!busyDay && moodSummary.latestEnergy > 7) {
                insights.add("🌟 Veel energie en weinig afspraken - perfect voor sport of creatief werk!")
            }

            // Check for back-to-back meetings
            val backToBackMeetings = hasBackToBackMeetings(calendarEvents)
            if (backToBackMeetings) {
                insights.add("⏱️ Veel aaneengesloten afspraken - plan pauzes in")
            }
        }

        return insights
    }

    private fun hasBackToBackMeetings(events: List<CalendarEvent>): Boolean {
        if (events.size < 2) return false

        val sortedEvents = events.sortedBy { it.startTime }
        for (i in 0 until sortedEvents.size - 1) {
            val current = sortedEvents[i]
            val next = sortedEvents[i + 1]

            // If next event starts within 15 minutes of current event ending
            if (next.startTime - current.endTime < 15 * 60 * 1000) {
                return true
            }
        }
        return false
    }

    fun formatMoodValue(value: Float): String {
        return when {
            value == 0f -> "0 (Geen data)"
            value <= 2f -> String.format("%.1f (Zeer laag)", value)
            value <= 4f -> String.format("%.1f (Laag)", value)
            value <= 6f -> String.format("%.1f (Neutraal)", value)
            value <= 8f -> String.format("%.1f (Goed)", value)
            else -> String.format("%.1f (Uitstekend)", value)
        }
    }

    fun formatEnergyValue(value: Float): String {
        return when {
            value == 0f -> "0 (Geen data)"
            value <= 2f -> String.format("%.1f (Zeer laag)", value)
            value <= 4f -> String.format("%.1f (Laag)", value)
            value <= 6f -> String.format("%.1f (Normaal)", value)
            value <= 8f -> String.format("%.1f (Hoog)", value)
            else -> String.format("%.1f (Zeer hoog)", value)
        }
    }

    // Helper function to get activity type icon
    fun getActivityIcon(activityType: ActivityType): String {
        return when (activityType) {
            ActivityType.MEETING -> "👥"
            ActivityType.WORK -> "💼"
            ActivityType.SOCIAL -> "🎉"
            ActivityType.EXERCISE -> "🏃"
            ActivityType.MEAL -> "🍽️"
            ActivityType.TRAVEL -> "🚗"
            ActivityType.PERSONAL -> "👨‍👩‍👧"
            else -> "📅"
        }
    }
}
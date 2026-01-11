package com.example.bodymindconnect.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.bodymindconnect.R
import com.example.bodymindconnect.model.ActivityType
import com.example.bodymindconnect.utils.DataCombiner
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private lateinit var tvTotalEntries: TextView
    private lateinit var tvLatestMood: TextView
    private lateinit var tvLatestEnergy: TextView
    private lateinit var tvLatestTime: TextView
    private lateinit var tvAverageMood: TextView
    private lateinit var tvAverageEnergy: TextView
    private lateinit var tvCalendarEvents: TextView
    private lateinit var tvLocationData: TextView
    private lateinit var tvInsights: TextView
    private lateinit var tvAllEntries: TextView
    private lateinit var btnRefreshData: Button
    private lateinit var cardCurrentEvent: CardView
    private lateinit var tvCurrentEvent: TextView

    private lateinit var dataCombiner: DataCombiner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize views
        tvTotalEntries = view.findViewById(R.id.tv_total_entries)
        tvLatestMood = view.findViewById(R.id.tv_latest_mood)
        tvLatestEnergy = view.findViewById(R.id.tv_latest_energy)
        tvLatestTime = view.findViewById(R.id.tv_latest_time)
        tvAverageMood = view.findViewById(R.id.tv_average_mood)
        tvAverageEnergy = view.findViewById(R.id.tv_average_energy)
        tvCalendarEvents = view.findViewById(R.id.tv_calendar_events)
        tvLocationData = view.findViewById(R.id.tv_location_data)
        tvInsights = view.findViewById(R.id.tv_insights)
        tvAllEntries = view.findViewById(R.id.tv_all_entries)
        btnRefreshData = view.findViewById(R.id.btn_refresh_data)
        cardCurrentEvent = view.findViewById(R.id.card_current_event)
        tvCurrentEvent = view.findViewById(R.id.tv_current_event)

        // Initialize data combiner
        dataCombiner = DataCombiner(requireContext())

        // Setup refresh button
        btnRefreshData.setOnClickListener {
            loadCombinedData()
            Toast.makeText(requireContext(), "Data refreshed", Toast.LENGTH_SHORT).show()
        }

        // Load and display data
        loadCombinedData()

        return view
    }

    private fun loadCombinedData() {
        val combinedData = dataCombiner.getCombinedData()
        val moodData = combinedData.moodData

        // Display mood data
        tvTotalEntries.text = "Totaal mood invoeren: ${moodData.totalEntries}"

        if (moodData.totalEntries > 0) {
            tvLatestMood.text = "Laatste stemming: ${dataCombiner.formatMoodValue(moodData.latestMood)}"
            tvLatestEnergy.text = "Laatste energie: ${dataCombiner.formatEnergyValue(moodData.latestEnergy)}"

            val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val timeString = dateFormat.format(Date(moodData.latestTimestamp))
            tvLatestTime.text = "Laatste invoer: $timeString"

            tvAverageMood.text = "Gemiddelde stemming: ${dataCombiner.formatMoodValue(moodData.averageMood)}"
            tvAverageEnergy.text = "Gemiddelde energie: ${dataCombiner.formatEnergyValue(moodData.averageEnergy)}"

            // Display all mood entries
            val allEntriesText = StringBuilder("Mood geschiedenis:\n\n")
            moodData.allEntries.sortedByDescending { it.timestamp }.forEach { entry ->
                val entryTime = dateFormat.format(Date(entry.timestamp))
                allEntriesText.append("• $entryTime\n")
                allEntriesText.append("  Stemming: ${dataCombiner.formatMoodValue(entry.moodValue)}\n")
                allEntriesText.append("  Energie: ${dataCombiner.formatEnergyValue(entry.energyValue)}\n\n")
            }
            tvAllEntries.text = allEntriesText.toString()
        } else {
            tvLatestMood.text = "Laatste stemming: Geen data"
            tvLatestEnergy.text = "Laatste energie: Geen data"
            tvLatestTime.text = "Laatste invoer: Geen data"
            tvAverageMood.text = "Gem. stemming: Geen data"
            tvAverageEnergy.text = "Gem. energie: Geen data"
            tvAllEntries.text = "Alle invoeren:\nGeen data beschikbaar"
        }

        // Display calendar events with enhanced information
        val calendarEvents = combinedData.calendarEvents
        if (calendarEvents.isNotEmpty()) {
            val calendarText = StringBuilder("📅 Vandaag's Agenda:\n\n")
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            calendarEvents.sortedBy { it.startTime }.forEach { event ->
                val icon = dataCombiner.getActivityIcon(event.activityType)
                val startTime = timeFormat.format(Date(event.startTime))
                val endTime = timeFormat.format(Date(event.endTime))

                calendarText.append("$icon $startTime - $endTime (${event.getFormattedDuration()})\n")
                calendarText.append("   ${event.title ?: "Geen titel"}\n")

                // Show activity type
                val activityTypeText = getActivityTypeName(event.activityType)
                calendarText.append("   $activityTypeText\n")

                // Show participants
                calendarText.append("   ${event.getFormattedParticipants()}\n")

                event.location?.let {
                    calendarText.append("   📍 $it\n")
                }
                calendarText.append("\n")
            }
            tvCalendarEvents.text = calendarText.toString()
        } else {
            tvCalendarEvents.text = "Geen agenda items vandaag"
        }

        // Display current event if any
        combinedData.currentEvent?.let { currentEvent ->
            cardCurrentEvent.visibility = View.VISIBLE

            val currentEventText = StringBuilder()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            currentEventText.append("${currentEvent.title ?: "Afspraak"}\n\n")
            currentEventText.append("⏰ ${timeFormat.format(Date(currentEvent.startTime))} - ${timeFormat.format(Date(currentEvent.endTime))}\n")
            currentEventText.append("⏱️ Duur: ${currentEvent.getFormattedDuration()}\n")
            currentEventText.append("📋 Type: ${getActivityTypeName(currentEvent.activityType)}\n")
            currentEventText.append("👥 ${currentEvent.getFormattedParticipants()}\n")
            currentEvent.location?.let {
                currentEventText.append("📍 Locatie: $it\n")
            }

            tvCurrentEvent.text = currentEventText.toString()
        } ?: run {
            cardCurrentEvent.visibility = View.GONE
        }

        // Display location data
        combinedData.locationData?.let { location ->
            val locationText = StringBuilder("Locatie data:\n\n")
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            locationText.append("Laatste locatie:\n")
            locationText.append("Tijd: ${timeFormat.format(Date(location.startTime))}\n")
            locationText.append("Coördinaten: ${String.format("%.4f, %.4f", location.latitude, location.longitude)}\n")
            location.address?.let {
                locationText.append("Adres: $it\n")
            }

            tvLocationData.text = locationText.toString()
        } ?: run {
            tvLocationData.text = "Geen locatie data beschikbaar"
        }

        // Display insights
        val insights = combinedData.insights
        if (insights.isNotEmpty()) {
            val insightsText = StringBuilder("Inzichten:\n\n")
            insights.forEach { insight ->
                insightsText.append("• $insight\n\n")
            }
            tvInsights.text = insightsText.toString()
        } else {
            tvInsights.text = "Geen inzichten beschikbaar\n(meer data nodig)"
        }
    }

    private fun getActivityTypeName(type: ActivityType): String {
        return when (type) {
            ActivityType.MEETING -> "👥 Vergadering"
            ActivityType.WORK -> "💼 Werk"
            ActivityType.SOCIAL -> "🎉 Sociaal"
            ActivityType.EXERCISE -> "🏃 Sport"
            ActivityType.MEAL -> "🍽️ Maaltijd"
            ActivityType.TRAVEL -> "🚗 Reis"
            ActivityType.PERSONAL -> "👨‍👩‍👧 Persoonlijk"
            else -> "📅 Overig"
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadCombinedData()
    }
}
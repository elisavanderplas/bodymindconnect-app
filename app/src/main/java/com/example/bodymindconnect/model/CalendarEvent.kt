package com.example.bodymindconnect.model

data class CalendarEvent(
    val id: Long,
    val title: String?,
    val description: String?,
    val startTime: Long,
    val endTime: Long,
    val participants: List<String>,
    val location: String?
) {
    // Calculate duration in minutes
    val durationMinutes: Long
        get() = (endTime - startTime) / (60 * 1000)

    // Get activity type based on title, description, and location
    val activityType: ActivityType
        get() = detectActivityType()

    // Format participants for display
    fun getFormattedParticipants(): String {
        return if (participants.isEmpty()) {
            "Alleen"
        } else if (participants.size == 1) {
            "Met: ${participants.first()}"
        } else {
            "Met ${participants.size} personen"
        }
    }

    // Format duration for display
    fun getFormattedDuration(): String {
        return when {
            durationMinutes < 60 -> "${durationMinutes} min"
            durationMinutes % 60 == 0L -> "${durationMinutes / 60} uur"
            else -> "${durationMinutes / 60}u ${durationMinutes % 60}min"
        }
    }

    private fun detectActivityType(): ActivityType {
        val combinedText = "${title ?: ""} ${description ?: ""} ${location ?: ""}".lowercase()

        return when {
            containsMeetingKeywords(combinedText) -> ActivityType.MEETING
            containsWorkKeywords(combinedText) -> ActivityType.WORK
            containsSocialKeywords(combinedText) -> ActivityType.SOCIAL
            containsExerciseKeywords(combinedText) -> ActivityType.EXERCISE
            containsMealKeywords(combinedText) -> ActivityType.MEAL
            containsTravelKeywords(combinedText) -> ActivityType.TRAVEL
            containsPersonalKeywords(combinedText) -> ActivityType.PERSONAL
            else -> ActivityType.OTHER
        }
    }

    private fun containsMeetingKeywords(text: String): Boolean {
        val keywords = listOf("meeting", "vergadering", "call", "overleg", "conference", "presentation")
        return keywords.any { text.contains(it) }
    }

    private fun containsWorkKeywords(text: String): Boolean {
        val keywords = listOf("work", "werk", "office", "kantoor", "project", "deadline", "report")
        return keywords.any { text.contains(it) }
    }

    private fun containsSocialKeywords(text: String): Boolean {
        val keywords = listOf("lunch", "dinner", "drinks", "borrel", "party", "feest", "friend", "vriend")
        return keywords.any { text.contains(it) }
    }

    private fun containsExerciseKeywords(text: String): Boolean {
        val keywords = listOf("gym", "workout", "run", "hardlopen", "yoga", "sport", "fitness", "training")
        return keywords.any { text.contains(it) }
    }

    private fun containsMealKeywords(text: String): Boolean {
        val keywords = listOf("breakfast", "ontbijt", "lunch", "dinner", "avondeten", "eat", "eten", "restaurant")
        return keywords.any { text.contains(it) }
    }

    private fun containsTravelKeywords(text: String): Boolean {
        val keywords = listOf("travel", "reis", "flight", "vlucht", "train", "trein", "commute", "wonen-werk")
        return keywords.any { text.contains(it) }
    }

    private fun containsPersonalKeywords(text: String): Boolean {
        val keywords = listOf("doctor", "arts", "appointment", "afspraak", "family", "familie", "home", "thuis")
        return keywords.any { text.contains(it) }
    }
}

enum class ActivityType {
    MEETING, WORK, SOCIAL, EXERCISE, MEAL, TRAVEL, PERSONAL, OTHER
}
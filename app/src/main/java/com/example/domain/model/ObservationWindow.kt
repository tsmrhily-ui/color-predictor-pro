package com.example.domain.model

enum class ObservationWindow(val label: String, val limit: Int?) {
    ALL("All", null),
    LAST_10("Last 10", 10),
    LAST_25("Last 25", 25),
    LAST_50("Last 50", 50),
    LAST_100("Last 100", 100);

    companion object {
        fun fromString(value: String?): ObservationWindow {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

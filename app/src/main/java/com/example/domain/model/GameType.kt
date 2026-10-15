package com.example.domain.model

enum class GameType(val displayName: String, val description: String) {
    RED_GREEN(
        displayName = "Red / Green",
        description = "Binary 2-color outcome tracking (Red vs Green)"
    ),
    RED_GREEN_VIOLET(
        displayName = "Red / Green / Violet",
        description = "Standard 3-color outcome tracking (Red, Green, Violet)"
    ),
    NUMBER(
        displayName = "Number (0-9)",
        description = "Single digit integer outcome tracking (0 through 9)"
    );

    companion object {
        fun fromString(value: String?): GameType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: RED_GREEN
        }
    }
}

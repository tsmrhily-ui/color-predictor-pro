package com.example.domain.model

enum class ColorResult(val displayName: String, val hexCode: Long) {
    RED("RED", 0xFFFF385C),
    GREEN("GREEN", 0xFF00E676),
    VIOLET("VIOLET", 0xFFB026FF);

    companion object {
        fun fromString(value: String?): ColorResult? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
        }

        /**
         * Returns standard color mapping for a given digit 0-9:
         * 0 -> Violet & Red
         * 5 -> Violet & Green
         * 1, 3, 7, 9 -> Green
         * 2, 4, 6, 8 -> Red
         */
        fun fromNumber(number: Int): ColorResult? {
            return when (number) {
                1, 3, 7, 9 -> GREEN
                2, 4, 6, 8 -> RED
                0, 5 -> VIOLET
                else -> null
            }
        }
    }
}

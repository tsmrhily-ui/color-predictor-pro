package com.example.util

import com.example.data.local.PredictionRecord
import com.example.domain.model.ColorResult
import com.example.domain.model.GameType

data class CsvImportResult(
    val validRecords: List<PredictionRecord>,
    val errorMessages: List<String>
)

object CsvHandler {

    private const val CSV_HEADER = "id,game_type,round_id,color_result,number_result,timestamp,notes"

    /**
     * Converts a list of PredictionRecords into a standard CSV string.
     */
    fun exportToCsv(records: List<PredictionRecord>): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\n")

        for (record in records) {
            val escapedNotes = record.notes?.replace("\"", "\"\"") ?: ""
            val notesValue = if (escapedNotes.contains(",") || escapedNotes.contains("\n")) {
                "\"$escapedNotes\""
            } else {
                escapedNotes
            }

            sb.append(record.id).append(",")
                .append(record.gameType).append(",")
                .append(escapeSpecialChars(record.roundId)).append(",")
                .append(record.colorResult ?: "").append(",")
                .append(record.numberResult?.toString() ?: "").append(",")
                .append(record.timestamp).append(",")
                .append(notesValue)
                .append("\n")
        }
        return sb.toString()
    }

    private fun escapeSpecialChars(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    /**
     * Parses and strictly validates CSV content row by row.
     */
    fun importFromCsv(csvContent: String): CsvImportResult {
        val lines = csvContent.lines()
        val validRecords = mutableListOf<PredictionRecord>()
        val errors = mutableListOf<String>()

        if (lines.isEmpty()) {
            errors.add("The CSV file is empty.")
            return CsvImportResult(emptyList(), errors)
        }

        var headerSkipped = false
        var lineIndex = 0

        for (line in lines) {
            lineIndex++
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // Check if this is the header line
            if (!headerSkipped && (trimmed.startsWith("id,") || trimmed.contains("game_type") || trimmed.contains("round_id"))) {
                headerSkipped = true
                continue
            }

            val tokens = parseCsvLine(trimmed)
            if (tokens.size < 3) {
                errors.add("Line $lineIndex: Insufficient columns (expected at least game_type, round_id, color/number).")
                continue
            }

            // Columns layout:
            // Either [id, game_type, round_id, color_result, number_result, timestamp, notes] (7 columns)
            // Or [game_type, round_id, color_result, number_result, notes] (5 columns)
            val gameTypeStr: String
            val roundIdStr: String
            val colorStr: String?
            val numberStr: String?
            val timestampVal: Long
            val notesStr: String?

            if (tokens.size >= 6 && tokens[0].toLongOrNull() != null) {
                // 7 columns format
                gameTypeStr = tokens.getOrNull(1)?.trim() ?: ""
                roundIdStr = tokens.getOrNull(2)?.trim() ?: ""
                colorStr = tokens.getOrNull(3)?.trim()?.ifBlank { null }
                numberStr = tokens.getOrNull(4)?.trim()?.ifBlank { null }
                timestampVal = tokens.getOrNull(5)?.trim()?.toLongOrNull() ?: System.currentTimeMillis()
                notesStr = tokens.getOrNull(6)?.trim()?.ifBlank { null }
            } else {
                // Simplified 4-5 columns format: [game_type, round_id, color_result, number_result, notes]
                gameTypeStr = tokens.getOrNull(0)?.trim() ?: ""
                roundIdStr = tokens.getOrNull(1)?.trim() ?: ""
                colorStr = tokens.getOrNull(2)?.trim()?.ifBlank { null }
                numberStr = tokens.getOrNull(3)?.trim()?.ifBlank { null }
                timestampVal = System.currentTimeMillis()
                notesStr = tokens.getOrNull(4)?.trim()?.ifBlank { null }
            }

            // Validation 1: Round ID must not be blank
            if (roundIdStr.isBlank()) {
                errors.add("Line $lineIndex: Round identifier cannot be blank.")
                continue
            }

            // Validation 2: Game Type must be valid
            val gameType = try {
                GameType.valueOf(gameTypeStr.uppercase())
            } catch (e: Exception) {
                errors.add("Line $lineIndex: Unknown game type '$gameTypeStr'. Must be RED_GREEN, RED_GREEN_VIOLET, or NUMBER.")
                continue
            }

            // Validation 3: Result validation
            var parsedColor: String? = null
            var parsedNumber: Int? = null

            if (!numberStr.isNullOrBlank()) {
                val num = numberStr.toIntOrNull()
                if (num == null || num !in 0..9) {
                    errors.add("Line $lineIndex: Invalid number '$numberStr'. Number must be an integer from 0 to 9.")
                    continue
                }
                parsedNumber = num
            }

            if (!colorStr.isNullOrBlank()) {
                val colorEnum = ColorResult.fromString(colorStr)
                if (colorEnum == null) {
                    errors.add("Line $lineIndex: Invalid color '$colorStr'. Allowed: RED, GREEN, VIOLET.")
                    continue
                }
                parsedColor = colorEnum.name
            }

            // Cross-check: If gameType is RED_GREEN, cannot be VIOLET
            if (gameType == GameType.RED_GREEN && parsedColor == ColorResult.VIOLET.name) {
                errors.add("Line $lineIndex: VIOLET is not permitted for RED_GREEN game type.")
                continue
            }

            // Cross-check: Must have at least a color or a number
            if (parsedColor == null && parsedNumber == null) {
                errors.add("Line $lineIndex: Missing outcome result (color or number required).")
                continue
            }

            validRecords.add(
                PredictionRecord(
                    id = 0, // Auto-generated upon insertion
                    gameType = gameType.name,
                    roundId = roundIdStr,
                    colorResult = parsedColor,
                    numberResult = parsedNumber,
                    notes = notesStr,
                    timestamp = timestampVal
                )
            )
        }

        return CsvImportResult(validRecords, errors)
    }

    /**
     * Splits a single CSV row taking double-quoted values into account.
     */
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (insideQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    // Escaped quote
                    current.append('\"')
                    i++
                } else {
                    insideQuotes = !insideQuotes
                }
            } else if (c == ',' && !insideQuotes) {
                result.add(current.toString())
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

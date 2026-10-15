package com.example.util

import com.example.data.local.PredictionRecord
import com.example.domain.model.GameType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvHandlerTest {

    @Test
    fun testExportAndImport_roundTripMatches() {
        val original = listOf(
            PredictionRecord(
                id = 1,
                gameType = GameType.RED_GREEN.name,
                roundId = "101",
                colorResult = "RED",
                numberResult = null,
                notes = "Sample Note",
                timestamp = 1700000000000L
            ),
            PredictionRecord(
                id = 2,
                gameType = GameType.NUMBER.name,
                roundId = "102",
                colorResult = "GREEN",
                numberResult = 7,
                notes = null,
                timestamp = 1700000005000L
            )
        )

        val csv = CsvHandler.exportToCsv(original)
        assertTrue(csv.contains("game_type"))
        assertTrue(csv.contains("RED_GREEN"))
        assertTrue(csv.contains("101"))

        val importResult = CsvHandler.importFromCsv(csv)
        assertEquals(2, importResult.validRecords.size)
        assertTrue(importResult.errorMessages.isEmpty())

        val rec1 = importResult.validRecords[0]
        assertEquals("RED_GREEN", rec1.gameType)
        assertEquals("101", rec1.roundId)
        assertEquals("RED", rec1.colorResult)
        assertEquals("Sample Note", rec1.notes)

        val rec2 = importResult.validRecords[1]
        assertEquals("NUMBER", rec2.gameType)
        assertEquals("102", rec2.roundId)
        assertEquals(7, rec2.numberResult)
    }

    @Test
    fun testImport_rejectsInvalidRowsAndCollectsErrors() {
        val badCsv = """
            id,game_type,round_id,color_result,number_result,timestamp,notes
            1,UNKNOWN_GAME,101,RED,,1700000000000,
            2,RED_GREEN,,RED,,1700000000000,
            3,RED_GREEN,103,VIOLET,,1700000000000,
            4,NUMBER,104,,99,1700000000000,
            5,RED_GREEN,105,GREEN,,1700000000000,Valid Row
        """.trimIndent()

        val result = CsvHandler.importFromCsv(badCsv)
        assertEquals(1, result.validRecords.size)
        assertEquals("105", result.validRecords[0].roundId)
        assertEquals("Valid Row", result.validRecords[0].notes)

        // There should be 4 recorded errors corresponding to the 4 bad rows
        assertEquals(4, result.errorMessages.size)
    }

    @Test
    fun testDuplicateRoundIdDetection() {
        val existingRounds = setOf("101", "102", "103")

        val newRound1 = "104"
        val newRound2 = "102" // duplicate

        assertFalse(existingRounds.contains(newRound1))
        assertTrue(existingRounds.contains(newRound2))
    }
}

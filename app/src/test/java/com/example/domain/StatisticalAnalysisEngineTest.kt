package com.example.domain

import com.example.data.local.PredictionRecord
import com.example.domain.engine.StatisticalAnalysisEngine
import com.example.domain.model.ColorResult
import com.example.domain.model.GameType
import com.example.domain.model.ObservationWindow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StatisticalAnalysisEngineTest {

    @Test
    fun testEmptyDataset_doesNotCrashAndReportsSafeDefaults() {
        val emptyRecords = emptyList<PredictionRecord>()

        val colorStats = StatisticalAnalysisEngine.analyzeColors(emptyRecords)
        assertEquals(0, colorStats.totalCount)
        assertEquals(0, colorStats.redCount)
        assertEquals(0, colorStats.greenCount)
        assertEquals(0, colorStats.violetCount)
        assertEquals(0.0, colorStats.redPercentage, 0.001)
        assertEquals(0.0, colorStats.greenPercentage, 0.001)
        assertNull(colorStats.currentStreak)
        assertTrue(colorStats.isSmallDataset)

        val numberStats = StatisticalAnalysisEngine.analyzeNumbers(emptyRecords)
        assertEquals(0, numberStats.totalCount)
        assertEquals(0, numberStats.sampleSize)
        assertTrue(numberStats.isSmallDataset)
        assertTrue(numberStats.mostFrequentNumbers.isEmpty())
        assertTrue(numberStats.leastFrequentNumbers.isEmpty())

        val estimate = StatisticalAnalysisEngine.calculateExperimentalEstimate(colorStats)
        assertNull(estimate)
    }

    @Test
    fun testPercentageCalculations_handlesRoundingAndDivisionByZero() {
        assertEquals(0.0, StatisticalAnalysisEngine.calculatePercentage(0, 0), 0.001)
        assertEquals(0.0, StatisticalAnalysisEngine.calculatePercentage(5, 0), 0.001)
        assertEquals(50.0, StatisticalAnalysisEngine.calculatePercentage(1, 2), 0.001)
        assertEquals(33.3, StatisticalAnalysisEngine.calculatePercentage(1, 3), 0.001)
        assertEquals(66.7, StatisticalAnalysisEngine.calculatePercentage(2, 3), 0.001)
        assertEquals(100.0, StatisticalAnalysisEngine.calculatePercentage(10, 10), 0.001)
    }

    @Test
    fun testNumberValidation_restrictsToSingleDigitZeroToNine() {
        assertTrue(StatisticalAnalysisEngine.isValidNumber(0))
        assertTrue(StatisticalAnalysisEngine.isValidNumber(5))
        assertTrue(StatisticalAnalysisEngine.isValidNumber(9))

        assertFalse(StatisticalAnalysisEngine.isValidNumber(-1))
        assertFalse(StatisticalAnalysisEngine.isValidNumber(10))
        assertFalse(StatisticalAnalysisEngine.isValidNumber(null))
    }

    @Test
    fun testFrequencyCalculations_colorCountsAndPercentages() {
        // Build list in newest-first order
        val records = listOf(
            PredictionRecord(id = 1, gameType = GameType.RED_GREEN.name, roundId = "104", colorResult = "RED", numberResult = null),
            PredictionRecord(id = 2, gameType = GameType.RED_GREEN.name, roundId = "103", colorResult = "RED", numberResult = null),
            PredictionRecord(id = 3, gameType = GameType.RED_GREEN.name, roundId = "102", colorResult = "GREEN", numberResult = null),
            PredictionRecord(id = 4, gameType = GameType.RED_GREEN.name, roundId = "101", colorResult = "RED", numberResult = null)
        )

        val stats = StatisticalAnalysisEngine.analyzeColors(records)
        assertEquals(4, stats.totalCount)
        assertEquals(3, stats.redCount)
        assertEquals(1, stats.greenCount)
        assertEquals(0, stats.violetCount)
        assertEquals(75.0, stats.redPercentage, 0.001)
        assertEquals(25.0, stats.greenPercentage, 0.001)
        assertTrue(stats.isSmallDataset) // 4 < 10
    }

    @Test
    fun testHistoricalStreakCalculations_currentAndMaxStreaks() {
        // Out comes chronological: GREEN, RED, RED, RED, GREEN, GREEN
        // In reverse chronological (newest first):
        // 1: GREEN (newest)
        // 2: GREEN
        // 3: RED
        // 4: RED
        // 5: RED
        // 6: GREEN (oldest)
        val records = listOf(
            PredictionRecord(id = 1, gameType = GameType.RED_GREEN.name, roundId = "6", colorResult = "GREEN", numberResult = null),
            PredictionRecord(id = 2, gameType = GameType.RED_GREEN.name, roundId = "5", colorResult = "GREEN", numberResult = null),
            PredictionRecord(id = 3, gameType = GameType.RED_GREEN.name, roundId = "4", colorResult = "RED", numberResult = null),
            PredictionRecord(id = 4, gameType = GameType.RED_GREEN.name, roundId = "3", colorResult = "RED", numberResult = null),
            PredictionRecord(id = 5, gameType = GameType.RED_GREEN.name, roundId = "2", colorResult = "RED", numberResult = null),
            PredictionRecord(id = 6, gameType = GameType.RED_GREEN.name, roundId = "1", colorResult = "GREEN", numberResult = null)
        )

        val stats = StatisticalAnalysisEngine.analyzeColors(records)
        assertNotNull(stats.currentStreak)
        assertEquals("GREEN", stats.currentStreak!!.outcome)
        assertEquals(2, stats.currentStreak!!.length) // newest 2 are GREEN
        assertEquals(3, stats.maxRedStreak) // 3 REDs in a row
        assertEquals(2, stats.maxGreenStreak)
    }

    @Test
    fun testNumberFrequencies_hotAndColdDetection() {
        val records = listOf(
            PredictionRecord(id = 1, gameType = GameType.NUMBER.name, roundId = "1", colorResult = null, numberResult = 7),
            PredictionRecord(id = 2, gameType = GameType.NUMBER.name, roundId = "2", colorResult = null, numberResult = 7),
            PredictionRecord(id = 3, gameType = GameType.NUMBER.name, roundId = "3", colorResult = null, numberResult = 3),
            PredictionRecord(id = 4, gameType = GameType.NUMBER.name, roundId = "4", colorResult = null, numberResult = 0)
        )

        val stats = StatisticalAnalysisEngine.analyzeNumbers(records)
        assertEquals(4, stats.totalCount)
        assertEquals(2, stats.numberFrequencies[7])
        assertEquals(1, stats.numberFrequencies[3])
        assertEquals(1, stats.numberFrequencies[0])
        assertEquals(0, stats.numberFrequencies[1])

        assertTrue(stats.mostFrequentNumbers.contains(7))
        // 0 count numbers like 1, 2, 4, 5, 6, 8, 9 are cold
        assertTrue(stats.leastFrequentNumbers.contains(1))
    }

    @Test
    fun testObservationWindow_slicing() {
        val records = (1..50).map { i ->
            PredictionRecord(
                id = i.toLong(),
                gameType = GameType.RED_GREEN.name,
                roundId = "$i",
                colorResult = if (i % 2 == 0) "RED" else "GREEN",
                numberResult = null
            )
        }

        val allWindow = StatisticalAnalysisEngine.applyWindow(records, ObservationWindow.ALL)
        assertEquals(50, allWindow.size)

        val tenWindow = StatisticalAnalysisEngine.applyWindow(records, ObservationWindow.LAST_10)
        assertEquals(10, tenWindow.size)

        val twentyFive = StatisticalAnalysisEngine.applyWindow(records, ObservationWindow.LAST_25)
        assertEquals(25, twentyFive.size)
    }
}

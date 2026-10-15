package com.example.domain.engine

import com.example.data.local.PredictionRecord
import com.example.domain.model.ColorResult
import com.example.domain.model.ColorStatsSummary
import com.example.domain.model.ExperimentalStatisticalEstimate
import com.example.domain.model.NumberStatsSummary
import com.example.domain.model.ObservationWindow
import com.example.domain.model.StreakInfo
import java.util.Locale
import kotlin.math.round

object StatisticalAnalysisEngine {

    const val SMALL_DATASET_THRESHOLD = 10

    /**
     * Filters list of records (expected in newest-first order) by observation window.
     */
    fun applyWindow(
        records: List<PredictionRecord>,
        window: ObservationWindow
    ): List<PredictionRecord> {
        val limit = window.limit ?: return records
        return records.take(limit)
    }

    /**
     * Validates whether a number string or integer is a valid 0-9 single digit.
     */
    fun isValidNumber(number: Int?): Boolean {
        return number != null && number in 0..9
    }

    /**
     * Safe percentage calculation with 1 decimal place precision.
     */
    fun calculatePercentage(count: Int, total: Int): Double {
        if (total <= 0 || count <= 0) return 0.0
        val raw = (count.toDouble() / total.toDouble()) * 100.0
        return round(raw * 10.0) / 10.0
    }

    /**
     * Formats percentage with sample size, e.g. "45.0% (N=20)"
     */
    fun formatPercentageWithSample(percentage: Double, sampleSize: Int): String {
        return String.format(Locale.US, "%.1f%% (N=%d)", percentage, sampleSize)
    }

    /**
     * Calculates color statistics for binary (RED/GREEN) or 3-color (RED/GREEN/VIOLET).
     * @param records List of records, ordered newest-first.
     */
    fun analyzeColors(records: List<PredictionRecord>): ColorStatsSummary {
        val total = records.size
        if (total == 0) {
            return ColorStatsSummary(
                totalCount = 0,
                redCount = 0,
                greenCount = 0,
                violetCount = 0,
                redPercentage = 0.0,
                greenPercentage = 0.0,
                violetPercentage = 0.0,
                currentStreak = null,
                maxRedStreak = 0,
                maxGreenStreak = 0,
                maxVioletStreak = 0,
                sampleSize = 0,
                isSmallDataset = true
            )
        }

        var red = 0
        var green = 0
        var violet = 0

        // In reverse chronological order (newest first), list of color names
        val colorOutcomes = mutableListOf<String>()

        for (record in records) {
            val color = record.colorResult?.uppercase(Locale.ROOT)
            when (color) {
                ColorResult.RED.name -> {
                    red++
                    colorOutcomes.add(ColorResult.RED.name)
                }
                ColorResult.GREEN.name -> {
                    green++
                    colorOutcomes.add(ColorResult.GREEN.name)
                }
                ColorResult.VIOLET.name -> {
                    violet++
                    colorOutcomes.add(ColorResult.VIOLET.name)
                }
                else -> {
                    // Try to infer from number if color was not explicitly set
                    if (record.numberResult != null) {
                        val mapped = ColorResult.fromNumber(record.numberResult)
                        if (mapped != null) {
                            when (mapped) {
                                ColorResult.RED -> red++
                                ColorResult.GREEN -> green++
                                ColorResult.VIOLET -> violet++
                            }
                            colorOutcomes.add(mapped.name)
                        }
                    }
                }
            }
        }

        val observedCount = colorOutcomes.size
        val redPct = calculatePercentage(red, observedCount)
        val greenPct = calculatePercentage(green, observedCount)
        val violetPct = calculatePercentage(violet, observedCount)

        // Calculate current streak (from newest item backwards)
        var currentStreak: StreakInfo? = null
        if (colorOutcomes.isNotEmpty()) {
            val currentOutcome = colorOutcomes[0]
            var streakLen = 0
            for (outcome in colorOutcomes) {
                if (outcome == currentOutcome) {
                    streakLen++
                } else {
                    break
                }
            }
            currentStreak = StreakInfo(
                outcome = currentOutcome,
                length = streakLen,
                isCurrent = true
            )
        }

        // Calculate max streak for each color (chronological order is easiest for scanning)
        val chronological = colorOutcomes.asReversed()
        val maxRed = calculateMaxStreak(chronological, ColorResult.RED.name)
        val maxGreen = calculateMaxStreak(chronological, ColorResult.GREEN.name)
        val maxViolet = calculateMaxStreak(chronological, ColorResult.VIOLET.name)

        return ColorStatsSummary(
            totalCount = total,
            redCount = red,
            greenCount = green,
            violetCount = violet,
            redPercentage = redPct,
            greenPercentage = greenPct,
            violetPercentage = violetPct,
            currentStreak = currentStreak,
            maxRedStreak = maxRed,
            maxGreenStreak = maxGreen,
            maxVioletStreak = maxViolet,
            sampleSize = observedCount,
            isSmallDataset = observedCount < SMALL_DATASET_THRESHOLD
        )
    }

    /**
     * Calculates number statistics for 0 through 9.
     * @param records List of records ordered newest-first.
     */
    fun analyzeNumbers(records: List<PredictionRecord>): NumberStatsSummary {
        val numbersOnly = records.mapNotNull { it.numberResult }.filter { it in 0..9 }
        val total = numbersOnly.size

        if (total == 0) {
            val emptyMap = (0..9).associateWith { 0 }
            val emptyPctMap = (0..9).associateWith { 0.0 }
            return NumberStatsSummary(
                totalCount = 0,
                numberFrequencies = emptyMap,
                numberPercentages = emptyPctMap,
                mostFrequentNumbers = emptyList(),
                leastFrequentNumbers = emptyList(),
                recentNumbers = emptyList(),
                sampleSize = 0,
                isSmallDataset = true
            )
        }

        val freqMap = mutableMapOf<Int, Int>()
        for (i in 0..9) freqMap[i] = 0
        for (num in numbersOnly) {
            freqMap[num] = (freqMap[num] ?: 0) + 1
        }

        val pctMap = freqMap.mapValues { (_, count) ->
            calculatePercentage(count, total)
        }

        val maxFreq = freqMap.values.maxOrNull() ?: 0
        val minFreq = freqMap.values.minOrNull() ?: 0

        val mostFrequent = if (maxFreq > 0) {
            freqMap.filter { it.value == maxFreq }.keys.sorted()
        } else emptyList()

        val leastFrequent = if (numbersOnly.isNotEmpty()) {
            freqMap.filter { it.value == minFreq }.keys.sorted()
        } else emptyList()

        return NumberStatsSummary(
            totalCount = total,
            numberFrequencies = freqMap,
            numberPercentages = pctMap,
            mostFrequentNumbers = mostFrequent,
            leastFrequentNumbers = leastFrequent,
            recentNumbers = numbersOnly.take(20),
            sampleSize = total,
            isSmallDataset = total < SMALL_DATASET_THRESHOLD
        )
    }

    /**
     * Calculates the longest consecutive streak of a given target outcome in a chronological list.
     */
    fun calculateMaxStreak(chronologicalOutcomes: List<String>, target: String): Int {
        var maxStreak = 0
        var currentStreak = 0
        for (outcome in chronologicalOutcomes) {
            if (outcome.equals(target, ignoreCase = true)) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 0
            }
        }
        return maxStreak
    }

    /**
     * Produces an experimental statistical estimate based on empirical frequency.
     * Explicitly documents assumptions and disclaimers.
     */
    fun calculateExperimentalEstimate(
        summary: ColorStatsSummary
    ): ExperimentalStatisticalEstimate? {
        if (summary.sampleSize < SMALL_DATASET_THRESHOLD) return null

        val counts = listOf(
            ColorResult.RED.name to summary.redCount,
            ColorResult.GREEN.name to summary.greenCount,
            ColorResult.VIOLET.name to summary.violetCount
        )

        val highest = counts.maxByOrNull { it.second } ?: return null
        val pct = calculatePercentage(highest.second, summary.sampleSize)

        return ExperimentalStatisticalEstimate(
            dominantHistoricalOutcome = highest.first,
            historicalFrequencyPct = pct,
            observedSampleSize = summary.sampleSize,
            modelAssumptions = "Descriptive empirical mode calculated over the past ${summary.sampleSize} rounds. Assumes stationarity across recent recorded observations."
        )
    }

    /**
     * Compares two periods of records (e.g. First Half vs Second Half) to detect variance.
     */
    fun comparePeriods(
        period1: List<PredictionRecord>,
        period2: List<PredictionRecord>
    ): Pair<ColorStatsSummary, ColorStatsSummary> {
        return analyzeColors(period1) to analyzeColors(period2)
    }
}

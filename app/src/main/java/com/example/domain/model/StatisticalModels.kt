package com.example.domain.model

data class StreakInfo(
    val outcome: String,
    val length: Int,
    val isCurrent: Boolean = false
)

data class FrequencyItem(
    val key: String,
    val displayName: String,
    val count: Int,
    val percentage: Double,
    val sampleSize: Int
)

data class ColorStatsSummary(
    val totalCount: Int,
    val redCount: Int,
    val greenCount: Int,
    val violetCount: Int,
    val redPercentage: Double,
    val greenPercentage: Double,
    val violetPercentage: Double,
    val currentStreak: StreakInfo?,
    val maxRedStreak: Int,
    val maxGreenStreak: Int,
    val maxVioletStreak: Int,
    val sampleSize: Int,
    val isSmallDataset: Boolean,
    val disclaimerMessage: String = "Historical frequency describes past recordings only. Independent random trials cannot be predicted from previous outcomes."
)

data class NumberStatsSummary(
    val totalCount: Int,
    val numberFrequencies: Map<Int, Int>, // 0..9 -> count
    val numberPercentages: Map<Int, Double>, // 0..9 -> percentage
    val mostFrequentNumbers: List<Int>,
    val leastFrequentNumbers: List<Int>,
    val recentNumbers: List<Int>,
    val sampleSize: Int,
    val isSmallDataset: Boolean,
    val disclaimerMessage: String = "Next result probability cannot be reliably determined from historical results alone."
)

data class ExperimentalStatisticalEstimate(
    val dominantHistoricalOutcome: String,
    val historicalFrequencyPct: Double,
    val observedSampleSize: Int,
    val modelAssumptions: String,
    val warning: String = "UNVERIFIED STATISTICAL ESTIMATE: Past occurrences have zero causal effect on truly independent random draws. Not a guaranteed outcome."
)

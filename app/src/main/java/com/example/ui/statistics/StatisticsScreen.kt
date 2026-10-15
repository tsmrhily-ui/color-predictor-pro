package com.example.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ColorResult
import com.example.ui.components.CanvasFrequencyBarChart
import com.example.ui.components.CanvasRatioBar
import com.example.ui.components.ChartBarData
import com.example.ui.components.GamblerDisclaimerCard
import com.example.ui.components.NeonCard
import com.example.ui.components.ObservationWindowSelector
import com.example.ui.components.SmallDatasetWarningBanner
import com.example.ui.components.StatCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "STATISTICAL TELEMETRY",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Descriptive analysis of local manual records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Observation Window Selector
        item {
            ObservationWindowSelector(
                selectedWindow = uiState.selectedWindow,
                onWindowSelected = { viewModel.selectWindow(it) }
            )
        }

        // Small dataset warning
        item {
            SmallDatasetWarningBanner(sampleSize = uiState.colorSummary.sampleSize)
        }

        // Overview Cards: Sample Size, Red/Green/Violet ratios
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Sample Size",
                    value = "${uiState.colorSummary.sampleSize}",
                    subtitle = "Window: ${uiState.selectedWindow.label}",
                    badgeText = if (uiState.isSmallDataset) "Low N" else "Valid",
                    accentColor = NeonCyan,
                    icon = Icons.Default.QueryStats,
                    modifier = Modifier.weight(1f).testTag("stats_sample_size_card")
                )

                val topNum = uiState.numberSummary.mostFrequentNumbers.firstOrNull()?.toString() ?: "-"
                StatCard(
                    title = "Mode Digit",
                    value = topNum,
                    subtitle = "Highest frequency",
                    badgeText = "0-9",
                    accentColor = NeonGold,
                    modifier = Modifier.weight(1f).testTag("stats_mode_digit_card")
                )
            }
        }

        // Color Breakdown Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Red",
                    value = "${uiState.colorSummary.redCount}",
                    subtitle = "${String.format(Locale.US, "%.1f", uiState.colorSummary.redPercentage)}%",
                    accentColor = NeonRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Green",
                    value = "${uiState.colorSummary.greenCount}",
                    subtitle = "${String.format(Locale.US, "%.1f", uiState.colorSummary.greenPercentage)}%",
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Violet",
                    value = "${uiState.colorSummary.violetCount}",
                    subtitle = "${String.format(Locale.US, "%.1f", uiState.colorSummary.violetPercentage)}%",
                    accentColor = NeonViolet,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tri-Color Distribution Visuals
        item {
            NeonCard(modifier = Modifier.fillMaxWidth().testTag("stats_color_distribution_card")) {
                Text(
                    text = "TRI-COLOR RATIO & FREQUENCY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                CanvasRatioBar(summary = uiState.colorSummary)

                Spacer(modifier = Modifier.height(14.dp))

                val colorBars = listOf(
                    ChartBarData("RED", uiState.colorSummary.redCount, uiState.colorSummary.redPercentage, NeonRed),
                    ChartBarData("GREEN", uiState.colorSummary.greenCount, uiState.colorSummary.greenPercentage, NeonGreen),
                    ChartBarData("VIOLET", uiState.colorSummary.violetCount, uiState.colorSummary.violetPercentage, NeonViolet)
                )
                CanvasFrequencyBarChart(bars = colorBars, heightDp = 150)
            }
        }

        // Streak Metrics
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "HISTORICAL RUNS & STREAKS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = NeonCyan
                    )
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val currentStreakText = if (uiState.colorSummary.currentStreak != null && uiState.colorSummary.currentStreak!!.length > 0) {
                    "${uiState.colorSummary.currentStreak!!.length}x ${uiState.colorSummary.currentStreak!!.outcome}"
                } else {
                    "None"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Current Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = currentStreakText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = NeonCyan)
                    }
                    Column {
                        Text(text = "Max Red Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${uiState.colorSummary.maxRedStreak}x", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NeonRed)
                    }
                    Column {
                        Text(text = "Max Green Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${uiState.colorSummary.maxGreenStreak}x", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NeonGreen)
                    }
                    Column {
                        Text(text = "Max Violet Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "${uiState.colorSummary.maxVioletStreak}x", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NeonViolet)
                    }
                }
            }
        }

        // Full 0–9 Digit Histogram
        item {
            NeonCard(modifier = Modifier.fillMaxWidth().testTag("stats_number_distribution_card")) {
                Text(
                    text = "DIGIT HISTOGRAM (0–9)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                val numberBars = (0..9).map { digit ->
                    val count = uiState.numberSummary.numberFrequencies[digit] ?: 0
                    val pct = uiState.numberSummary.numberPercentages[digit] ?: 0.0
                    val col = when (ColorResult.fromNumber(digit)) {
                        ColorResult.RED -> NeonRed
                        ColorResult.GREEN -> NeonGreen
                        ColorResult.VIOLET -> NeonViolet
                        null -> NeonCyan
                    }
                    ChartBarData(label = digit.toString(), count = count, percentage = pct, color = col)
                }

                CanvasFrequencyBarChart(bars = numberBars, heightDp = 180)
            }
        }

        // Gambler's Fallacy Educational Card
        item {
            GamblerDisclaimerCard()
        }
    }
}

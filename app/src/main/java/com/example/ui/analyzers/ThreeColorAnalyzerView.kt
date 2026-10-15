package com.example.ui.analyzers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PredictionRecord
import com.example.domain.model.ColorStatsSummary
import com.example.domain.model.ObservationWindow
import com.example.ui.components.CanvasFrequencyBarChart
import com.example.ui.components.CanvasRatioBar
import com.example.ui.components.ChartBarData
import com.example.ui.components.NeonCard
import com.example.ui.components.ObservationWindowSelector
import com.example.ui.components.ResultColorBadge
import com.example.ui.components.SmallDatasetWarningBanner
import com.example.ui.components.StatCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import java.util.Locale

@Composable
fun ThreeColorAnalyzerView(
    summary: ColorStatsSummary,
    records: List<PredictionRecord>,
    selectedWindow: ObservationWindow,
    onWindowSelected: (ObservationWindow) -> Unit,
    comparisonPeriodA: ColorStatsSummary?,
    comparisonPeriodB: ColorStatsSummary?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("three_color_analyzer_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Window selector
        ObservationWindowSelector(
            selectedWindow = selectedWindow,
            onWindowSelected = onWindowSelected
        )

        // Small dataset banner
        SmallDatasetWarningBanner(sampleSize = summary.sampleSize)

        // 3-Color Count Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "Red",
                value = "${summary.redCount}",
                subtitle = "${String.format(Locale.US, "%.1f", summary.redPercentage)}%",
                accentColor = NeonRed,
                modifier = Modifier.weight(1f).testTag("rgv_red_stat_card")
            )
            StatCard(
                title = "Green",
                value = "${summary.greenCount}",
                subtitle = "${String.format(Locale.US, "%.1f", summary.greenPercentage)}%",
                accentColor = NeonGreen,
                modifier = Modifier.weight(1f).testTag("rgv_green_stat_card")
            )
            StatCard(
                title = "Violet",
                value = "${summary.violetCount}",
                subtitle = "${String.format(Locale.US, "%.1f", summary.violetPercentage)}%",
                accentColor = NeonViolet,
                modifier = Modifier.weight(1f).testTag("rgv_violet_stat_card")
            )
        }

        // Streak Analysis Card
        NeonCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonCyan.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STREAK ANALYSIS (3-COLOR)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = NeonCyan
                )
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = NeonCyan
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            val currentStreakText = if (summary.currentStreak != null && summary.currentStreak.length > 0) {
                "${summary.currentStreak.length}x ${summary.currentStreak.outcome}"
            } else {
                "None"
            }

            val currentStreakColor = when (summary.currentStreak?.outcome) {
                "RED" -> NeonRed
                "GREEN" -> NeonGreen
                "VIOLET" -> NeonViolet
                else -> NeonCyan
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Current Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = currentStreakText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = currentStreakColor)
                }
                Column {
                    Text(text = "Max Red Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${summary.maxRedStreak}x", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NeonRed)
                }
                Column {
                    Text(text = "Max Green Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${summary.maxGreenStreak}x", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NeonGreen)
                }
                Column {
                    Text(text = "Max Violet Run", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${summary.maxVioletStreak}x", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = NeonViolet)
                }
            }
        }

        // Distribution Charts
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "TRI-COLOR DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            CanvasRatioBar(summary = summary)

            Spacer(modifier = Modifier.height(14.dp))

            val bars = listOf(
                ChartBarData("RED", summary.redCount, summary.redPercentage, NeonRed),
                ChartBarData("GREEN", summary.greenCount, summary.greenPercentage, NeonGreen),
                ChartBarData("VIOLET", summary.violetCount, summary.violetPercentage, NeonViolet)
            )
            CanvasFrequencyBarChart(bars = bars, heightDp = 150)
        }

        // Recent sequence chips
        if (records.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RECENT OUTCOME SEQUENCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(records.take(25)) { record ->
                        ResultColorBadge(
                            colorName = record.colorResult,
                            number = record.numberResult,
                            size = 34
                        )
                    }
                }
            }
        }

        // Historical Comparison Between Recent Half and Older Half
        if (comparisonPeriodA != null && comparisonPeriodB != null) {
            NeonCard(
                modifier = Modifier.fillMaxWidth().testTag("time_range_comparison_card"),
                borderColor = NeonCyan.copy(alpha = 0.35f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CompareArrows, contentDescription = null, tint = NeonCyan)
                    Text(
                        text = "HISTORICAL SUBSET COMPARISON",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Comparing the recent half (${comparisonPeriodA.sampleSize} rounds) vs earlier half (${comparisonPeriodB.sampleSize} rounds) to inspect temporal frequency variance:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recent Half (N=${comparisonPeriodA.sampleSize})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Red: ${String.format(Locale.US, "%.1f", comparisonPeriodA.redPercentage)}%", style = MaterialTheme.typography.bodySmall, color = NeonRed)
                        Text(text = "Green: ${String.format(Locale.US, "%.1f", comparisonPeriodA.greenPercentage)}%", style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                        Text(text = "Violet: ${String.format(Locale.US, "%.1f", comparisonPeriodA.violetPercentage)}%", style = MaterialTheme.typography.bodySmall, color = NeonViolet)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Earlier Half (N=${comparisonPeriodB.sampleSize})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = "Red: ${String.format(Locale.US, "%.1f", comparisonPeriodB.redPercentage)}%", style = MaterialTheme.typography.bodySmall, color = NeonRed)
                        Text(text = "Green: ${String.format(Locale.US, "%.1f", comparisonPeriodB.greenPercentage)}%", style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                        Text(text = "Violet: ${String.format(Locale.US, "%.1f", comparisonPeriodB.violetPercentage)}%", style = MaterialTheme.typography.bodySmall, color = NeonViolet)
                    }
                }
            }
        }

        // Probability Distinction Notice
        NeonCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonGold.copy(alpha = 0.35f),
            backgroundColor = NeonGold.copy(alpha = 0.04f)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = NeonGold)
                Column {
                    Text(
                        text = "Historical Frequency ≠ Next Probability",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Historical frequencies document recorded events in your local dataset. They do not dictate or influence the outcome of the next independent game round.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

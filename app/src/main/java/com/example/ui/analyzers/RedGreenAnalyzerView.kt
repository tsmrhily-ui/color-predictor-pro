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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PredictionRecord
import com.example.domain.model.ColorStatsSummary
import com.example.domain.model.ExperimentalStatisticalEstimate
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
import java.util.Locale

@Composable
fun RedGreenAnalyzerView(
    summary: ColorStatsSummary,
    records: List<PredictionRecord>,
    selectedWindow: ObservationWindow,
    onWindowSelected: (ObservationWindow) -> Unit,
    estimate: ExperimentalStatisticalEstimate?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("red_green_analyzer_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Window selector
        ObservationWindowSelector(
            selectedWindow = selectedWindow,
            onWindowSelected = onWindowSelected
        )

        // Small dataset banner
        SmallDatasetWarningBanner(sampleSize = summary.sampleSize)

        // Frequency Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Red Results",
                value = "${summary.redCount}",
                subtitle = "${String.format(Locale.US, "%.1f", summary.redPercentage)}%",
                badgeText = "N=${summary.sampleSize}",
                accentColor = NeonRed,
                modifier = Modifier.weight(1f).testTag("rg_red_stat_card")
            )
            StatCard(
                title = "Green Results",
                value = "${summary.greenCount}",
                subtitle = "${String.format(Locale.US, "%.1f", summary.greenPercentage)}%",
                badgeText = "N=${summary.sampleSize}",
                accentColor = NeonGreen,
                modifier = Modifier.weight(1f).testTag("rg_green_stat_card")
            )
        }

        // Streak Analysis Card
        NeonCard(
            modifier = Modifier.fillMaxWidth().testTag("streak_analysis_card"),
            borderColor = NeonCyan.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTORICAL STREAK TELEMETRY",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current Streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentStreakText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (summary.currentStreak?.outcome == "RED") NeonRed else NeonGreen
                    )
                }

                Column {
                    Text(
                        text = "Max Red Run",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.maxRedStreak} in a row",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = NeonRed
                    )
                }

                Column {
                    Text(
                        text = "Max Green Run",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${summary.maxGreenStreak} in a row",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = NeonGreen
                    )
                }
            }
        }

        // Visual Distribution Chart
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "HISTORICAL FREQUENCY DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            CanvasRatioBar(summary = summary)

            Spacer(modifier = Modifier.height(14.dp))

            val bars = listOf(
                ChartBarData("RED", summary.redCount, summary.redPercentage, NeonRed),
                ChartBarData("GREEN", summary.greenCount, summary.greenPercentage, NeonGreen)
            )
            CanvasFrequencyBarChart(bars = bars, heightDp = 140)
        }

        // Recent sequence chips
        if (records.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RECORDED SEQUENCE (NEWEST FIRST)",
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
                    items(records.take(20)) { record ->
                        ResultColorBadge(
                            colorName = record.colorResult,
                            number = record.numberResult,
                            size = 32
                        )
                    }
                }
            }
        }

        // Statistical Summary (Historical Fact Notice)
        NeonCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ) {
            Text(
                text = "STATISTICAL SUMMARY & LIMITATIONS",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Based exclusively on the ${summary.sampleSize} locally recorded rounds: RED has appeared ${summary.redCount} times (${String.format(Locale.US, "%.1f", summary.redPercentage)}%) and GREEN has appeared ${summary.greenCount} times (${String.format(Locale.US, "%.1f", summary.greenPercentage)}%).\n\nIndependent Random Notice: Historical frequency does not cause future outcomes. A color is never 'due' to appear because it appeared less frequently in past rounds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Experimental Statistical Estimate (labeled clearly as uncertain)
        if (estimate != null) {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonGold.copy(alpha = 0.4f),
                backgroundColor = NeonGold.copy(alpha = 0.04f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonGold
                    )
                    Text(
                        text = "EXPERIMENTAL STATISTICAL ESTIMATE (UNCERTAIN)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonGold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Observed Empirical Mode: ${estimate.dominantHistoricalOutcome} (${String.format(Locale.US, "%.1f", estimate.historicalFrequencyPct)}% over last ${estimate.observedSampleSize} rounds)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Assumption: ${estimate.modelAssumptions}\n${estimate.warning}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

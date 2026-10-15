package com.example.ui.analyzers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PredictionRecord
import com.example.domain.model.ColorResult
import com.example.domain.model.NumberStatsSummary
import com.example.domain.model.ObservationWindow
import com.example.ui.components.CanvasFrequencyBarChart
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NumberAnalyzerView(
    summary: NumberStatsSummary,
    records: List<PredictionRecord>,
    selectedWindow: ObservationWindow,
    onWindowSelected: (ObservationWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("number_analyzer_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Window selector
        ObservationWindowSelector(
            selectedWindow = selectedWindow,
            onWindowSelected = onWindowSelected
        )

        // Small dataset banner
        SmallDatasetWarningBanner(sampleSize = summary.sampleSize)

        // Hot / Cold Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val mostFreqStr = if (summary.mostFrequentNumbers.isNotEmpty()) {
                summary.mostFrequentNumbers.joinToString(", ")
            } else "-"

            val leastFreqStr = if (summary.leastFrequentNumbers.isNotEmpty()) {
                summary.leastFrequentNumbers.joinToString(", ")
            } else "-"

            StatCard(
                title = "Most Frequent",
                value = mostFreqStr,
                subtitle = "Empirical Mode",
                badgeText = "Hot",
                accentColor = NeonRed,
                icon = Icons.Default.Whatshot,
                modifier = Modifier.weight(1f).testTag("stat_hot_numbers")
            )

            StatCard(
                title = "Least Frequent",
                value = leastFreqStr,
                subtitle = "Lowest Count",
                badgeText = "Cold",
                accentColor = NeonCyan,
                icon = Icons.Default.AcUnit,
                modifier = Modifier.weight(1f).testTag("stat_cold_numbers")
            )
        }

        // Full 10-Digit Bar Chart (0 - 9)
        NeonCard(modifier = Modifier.fillMaxWidth().testTag("number_chart_card")) {
            Text(
                text = "NUMBER FREQUENCY HISTOGRAM (0–9)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            val chartBars = (0..9).map { digit ->
                val count = summary.numberFrequencies[digit] ?: 0
                val pct = summary.numberPercentages[digit] ?: 0.0
                val digitColor = when (ColorResult.fromNumber(digit)) {
                    ColorResult.RED -> NeonRed
                    ColorResult.GREEN -> NeonGreen
                    ColorResult.VIOLET -> NeonViolet
                    null -> NeonCyan
                }
                ChartBarData(
                    label = digit.toString(),
                    count = count,
                    percentage = pct,
                    color = digitColor
                )
            }

            CanvasFrequencyBarChart(bars = chartBars, heightDp = 180)
        }

        // Complete 10-Digit Frequency Grid Table
        NeonCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "ALL DIGITS FREQUENCY TABLE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (0..9).forEach { digit ->
                    val count = summary.numberFrequencies[digit] ?: 0
                    val pct = summary.numberPercentages[digit] ?: 0.0
                    val digitColor = when (ColorResult.fromNumber(digit)) {
                        ColorResult.RED -> NeonRed
                        ColorResult.GREEN -> NeonGreen
                        ColorResult.VIOLET -> NeonViolet
                        null -> NeonCyan
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, digitColor.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = digit.toString(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = digitColor
                            )
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", pct)}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Recent Number Sequence
        if (summary.recentNumbers.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "RECENT DIGIT SEQUENCE (NEWEST FIRST)",
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
                    items(summary.recentNumbers) { digit ->
                        val color = ColorResult.fromNumber(digit)
                        ResultColorBadge(
                            colorName = color?.name,
                            number = digit,
                            size = 36
                        )
                    }
                }
            }
        }

        // Independent Random Disclaimer
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
                        text = "Independence of Number Draws",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = NeonGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary.disclaimerMessage + "\nIn a fair 10-digit system, each number 0–9 has an equal 10% theoretical probability on every independent draw, regardless of past streak lengths or 'cold' frequencies.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ColorStatsSummary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import java.util.Locale

data class ChartBarData(
    val label: String,
    val count: Int,
    val percentage: Double,
    val color: Color
)

@Composable
fun CanvasFrequencyBarChart(
    bars: List<ChartBarData>,
    modifier: Modifier = Modifier,
    heightDp: Int = 180
) {
    val totalCount = bars.sumOf { it.count }
    val maxCount = (bars.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)

    // Build accessibility description
    val a11ySummary = bars.joinToString(separator = ", ") {
        "${it.label}: ${it.count} (${String.format(Locale.US, "%.1f", it.percentage)}%)"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Frequency distribution chart. $a11ySummary"
            }
    ) {
        if (totalCount == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightDp.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No records recorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height((heightDp - 32).dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = bars.size
                if (barCount == 0) return@Canvas

                val slotWidth = canvasWidth / barCount
                val barWidth = (slotWidth * 0.65f).coerceAtMost(48.dp.toPx())
                val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())

                bars.forEachIndexed { index, bar ->
                    val barHeightFraction = (bar.count.toFloat() / maxCount.toFloat()).coerceIn(0.04f, 1f)
                    val barHeight = canvasHeight * barHeightFraction
                    val left = index * slotWidth + (slotWidth - barWidth) / 2f
                    val top = canvasHeight - barHeight

                    // Draw bar gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(bar.color, bar.color.copy(alpha = 0.4f)),
                            startY = top,
                            endY = canvasHeight
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = cornerRadius
                    )

                    // Draw subtle top glow cap
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.8f),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, 3.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // Bottom Labels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                bars.forEach { bar ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bar.count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = bar.color,
                            fontSize = 11.sp
                        )
                        Text(
                            text = bar.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Accessible text alternative table
        Text(
            text = "Data summary: $a11ySummary",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun CanvasRatioBar(
    summary: ColorStatsSummary,
    modifier: Modifier = Modifier
) {
    val total = summary.redCount + summary.greenCount + summary.violetCount
    if (total == 0) return

    val redFrac = (summary.redCount.toFloat() / total.toFloat()).coerceAtLeast(0f)
    val greenFrac = (summary.greenCount.toFloat() / total.toFloat()).coerceAtLeast(0f)
    val violetFrac = (summary.violetCount.toFloat() / total.toFloat()).coerceAtLeast(0f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Stacked visual bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                if (redFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(redFrac.coerceAtLeast(0.001f))
                            .fillMaxHeight()
                            .background(NeonRed)
                    )
                }
                if (greenFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(greenFrac.coerceAtLeast(0.001f))
                            .fillMaxHeight()
                            .background(NeonGreen)
                    )
                }
                if (violetFrac > 0) {
                    Box(
                        modifier = Modifier
                            .weight(violetFrac.coerceAtLeast(0.001f))
                            .fillMaxHeight()
                            .background(NeonViolet)
                    )
                }
            }
        }

        // Legends below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RatioLegendItem("RED", summary.redCount, summary.redPercentage, NeonRed)
            RatioLegendItem("GREEN", summary.greenCount, summary.greenPercentage, NeonGreen)
            if (summary.violetCount > 0 || violetFrac > 0) {
                RatioLegendItem("VIOLET", summary.violetCount, summary.violetPercentage, NeonViolet)
            }
        }
    }
}

@Composable
private fun RatioLegendItem(
    label: String,
    count: Int,
    percentage: Double,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = "$label: $count (${String.format(Locale.US, "%.1f", percentage)}%)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

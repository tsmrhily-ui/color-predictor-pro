package com.example.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.PredictionRecord
import com.example.domain.model.ColorResult
import com.example.domain.model.GameType
import com.example.ui.components.GamblerDisclaimerCard
import com.example.ui.components.NeonCard
import com.example.ui.components.ResultColorBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.util.DateFormatter
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAddResult: (GameType?) -> Unit,
    onNavigateToAnalyzerModule: (Int) -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayFormatted = DateFormatter.formatDate(System.currentTimeMillis())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Header & Branding
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hero_analytics_banner_1792077757224),
                    contentDescription = "Analytics Dashboard Banner",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.45f
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Logo",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "COLOR PREDICTOR PRO",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Offline Statistical Telemetry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, NeonCyan.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = todayFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Prominent Add Result Action Button
                    Button(
                        onClick = { onNavigateToAddResult(GameType.RED_GREEN) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("home_add_result_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECORD NEW RESULT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // Metrics Quick Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "LIFETIME OVERVIEW",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Row 1: Total Records & Top Number
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Recorded",
                        value = "${uiState.totalRecords}",
                        subtitle = "Manual entries",
                        badgeText = "Offline",
                        accentColor = NeonCyan,
                        icon = Icons.Default.QueryStats,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_records")
                    )

                    val topNum = uiState.numberSummary.mostFrequentNumbers.firstOrNull()?.toString() ?: "-"
                    StatCard(
                        title = "Top Number",
                        value = topNum,
                        subtitle = "Most recorded",
                        badgeText = if (topNum != "-") "N=${uiState.numberSummary.sampleSize}" else "Empty",
                        accentColor = NeonGold,
                        icon = Icons.Default.Numbers,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_top_number")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Red, Green, Violet Counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Red",
                        value = "${uiState.colorSummary.redCount}",
                        subtitle = "${String.format(Locale.US, "%.1f", uiState.colorSummary.redPercentage)}%",
                        accentColor = NeonRed,
                        modifier = Modifier.weight(1f).testTag("stat_red_count")
                    )
                    StatCard(
                        title = "Green",
                        value = "${uiState.colorSummary.greenCount}",
                        subtitle = "${String.format(Locale.US, "%.1f", uiState.colorSummary.greenPercentage)}%",
                        accentColor = NeonGreen,
                        modifier = Modifier.weight(1f).testTag("stat_green_count")
                    )
                    StatCard(
                        title = "Violet",
                        value = "${uiState.colorSummary.violetCount}",
                        subtitle = "${String.format(Locale.US, "%.1f", uiState.colorSummary.violetPercentage)}%",
                        accentColor = NeonViolet,
                        modifier = Modifier.weight(1f).testTag("stat_violet_count")
                    )
                }
            }
        }

        // Recent Results Timeline Section
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT OUTCOMES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.recentRecords.isNotEmpty()) {
                        Text(
                            text = "View all (${uiState.totalRecords})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = NeonCyan,
                            modifier = Modifier
                                .clickable { onNavigateToHistory() }
                                .padding(4.dp)
                                .testTag("home_view_all_history")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.recentRecords.isEmpty()) {
                    NeonCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No recorded results yet. Tap 'RECORD NEW RESULT' to log your first outcome.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        items(uiState.recentRecords) { item ->
                            RecentOutcomeBadgeItem(item)
                        }
                    }
                }
            }
        }

        // Navigation to 3 Analyzer Modules
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "ANALYZER MODULES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                AnalyzerModuleCard(
                    title = "Module A: Red / Green Analyzer",
                    description = "Binary frequency tracking, streaks, 10-100 round observation windows, and distribution charts.",
                    accentColor = NeonGreen,
                    icon = Icons.Default.FiberManualRecord,
                    onClick = { onNavigateToAnalyzerModule(0) },
                    testTag = "nav_module_a_card"
                )

                Spacer(modifier = Modifier.height(10.dp))

                AnalyzerModuleCard(
                    title = "Module B: Red / Green / Violet Analyzer",
                    description = "3-Color distribution analyzer, sequence tracking, and time range comparative statistics.",
                    accentColor = NeonViolet,
                    icon = Icons.Default.Casino,
                    onClick = { onNavigateToAnalyzerModule(1) },
                    testTag = "nav_module_b_card"
                )

                Spacer(modifier = Modifier.height(10.dp))

                AnalyzerModuleCard(
                    title = "Module C: Number Analyzer (0–9)",
                    description = "Full 10-digit frequency histograms, hot & cold numbers, and observation window slicing.",
                    accentColor = NeonCyan,
                    icon = Icons.Default.Numbers,
                    onClick = { onNavigateToAnalyzerModule(2) },
                    testTag = "nav_module_c_card"
                )
            }
        }

        // Mandatory Statistical Notice / Gambler's Fallacy
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                GamblerDisclaimerCard()
            }
        }
    }
}

@Composable
private fun RecentOutcomeBadgeItem(record: PredictionRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ResultColorBadge(
                colorName = record.colorResult,
                number = record.numberResult,
                size = 36
            )
            Text(
                text = "#${record.roundId}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun AnalyzerModuleCard(
    title: String,
    description: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open",
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

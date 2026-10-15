package com.example.ui.analyzers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.GameType
import com.example.ui.theme.NeonCyan

@Composable
fun AnalyzersScreen(
    viewModel: AnalyzersViewModel,
    onNavigateToAddResult: (GameType) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Red / Green", "Red / Green / Violet", "Numbers (0–9)")

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = NeonCyan,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                        color = NeonCyan
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("analyzers_tab_row")
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = uiState.selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("analyzer_tab_$index")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("analyzers_content_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
            ) {
                item {
                    when (uiState.selectedTab) {
                        0 -> RedGreenAnalyzerView(
                            summary = uiState.redGreenSummary,
                            records = uiState.redGreenRecords,
                            selectedWindow = uiState.selectedWindow,
                            onWindowSelected = { viewModel.selectWindow(it) },
                            estimate = uiState.redGreenEstimate
                        )
                        1 -> ThreeColorAnalyzerView(
                            summary = uiState.threeColorSummary,
                            records = uiState.threeColorRecords,
                            selectedWindow = uiState.selectedWindow,
                            onWindowSelected = { viewModel.selectWindow(it) },
                            comparisonPeriodA = uiState.comparisonPeriodA,
                            comparisonPeriodB = uiState.comparisonPeriodB
                        )
                        2 -> NumberAnalyzerView(
                            summary = uiState.numberSummary,
                            records = uiState.numberRecords,
                            selectedWindow = uiState.selectedWindow,
                            onWindowSelected = { viewModel.selectWindow(it) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val gameType = when (uiState.selectedTab) {
                    0 -> GameType.RED_GREEN
                    1 -> GameType.RED_GREEN_VIOLET
                    else -> GameType.NUMBER
                }
                onNavigateToAddResult(gameType)
            },
            containerColor = NeonCyan,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 96.dp, end = 20.dp)
                .testTag("analyzers_fab_add")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add result for this module")
        }
    }
}

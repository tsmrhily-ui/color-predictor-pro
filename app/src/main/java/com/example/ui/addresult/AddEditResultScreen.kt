package com.example.ui.addresult

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ColorResult
import com.example.domain.model.GameType
import com.example.ui.components.NeonCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.util.HapticHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditResultScreen(
    viewModel: AddEditResultViewModel,
    hapticEnabled: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AddEditEvent.SaveSuccess -> {
                    HapticHelper.performSuccessHaptic(context, hapticEnabled)
                    snackbarHostState.showSnackbar(event.message)
                    onNavigateBack()
                }
                is AddEditEvent.SaveError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) "Edit Round #${uiState.roundId}" else "Record Game Outcome",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("add_result_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner if any
            if (uiState.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Game Type selector
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SELECT GAME MODE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameType.entries.forEach { type ->
                        val isSelected = uiState.gameType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                HapticHelper.performClickHaptic(context, hapticEnabled)
                                viewModel.setGameType(type)
                            },
                            label = {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                selectedBorderColor = NeonCyan
                            ),
                            modifier = Modifier.weight(1f).testTag("chip_game_type_${type.name}")
                        )
                    }
                }
            }

            // Round Identifier Input
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ROUND IDENTIFIER *",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.roundId,
                    onValueChange = { viewModel.setRoundId(it) },
                    placeholder = { Text("e.g. 20261015001 or 104") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_round_id"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Dynamic Result Selection Based on Game Type
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RECORDED OUTCOME RESULT *",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (uiState.gameType) {
                    GameType.RED_GREEN -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ColorSelectButton(
                                label = "RED",
                                color = NeonRed,
                                isSelected = uiState.selectedColor == ColorResult.RED,
                                onClick = {
                                    HapticHelper.performClickHaptic(context, hapticEnabled)
                                    viewModel.selectColor(ColorResult.RED)
                                },
                                modifier = Modifier.weight(1f).testTag("btn_select_red")
                            )
                            ColorSelectButton(
                                label = "GREEN",
                                color = NeonGreen,
                                isSelected = uiState.selectedColor == ColorResult.GREEN,
                                onClick = {
                                    HapticHelper.performClickHaptic(context, hapticEnabled)
                                    viewModel.selectColor(ColorResult.GREEN)
                                },
                                modifier = Modifier.weight(1f).testTag("btn_select_green")
                            )
                        }
                    }

                    GameType.RED_GREEN_VIOLET -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ColorSelectButton(
                                label = "RED",
                                color = NeonRed,
                                isSelected = uiState.selectedColor == ColorResult.RED,
                                onClick = {
                                    HapticHelper.performClickHaptic(context, hapticEnabled)
                                    viewModel.selectColor(ColorResult.RED)
                                },
                                modifier = Modifier.weight(1f).testTag("btn_select_red")
                            )
                            ColorSelectButton(
                                label = "GREEN",
                                color = NeonGreen,
                                isSelected = uiState.selectedColor == ColorResult.GREEN,
                                onClick = {
                                    HapticHelper.performClickHaptic(context, hapticEnabled)
                                    viewModel.selectColor(ColorResult.GREEN)
                                },
                                modifier = Modifier.weight(1f).testTag("btn_select_green")
                            )
                            ColorSelectButton(
                                label = "VIOLET",
                                color = NeonViolet,
                                isSelected = uiState.selectedColor == ColorResult.VIOLET,
                                onClick = {
                                    HapticHelper.performClickHaptic(context, hapticEnabled)
                                    viewModel.selectColor(ColorResult.VIOLET)
                                },
                                modifier = Modifier.weight(1f).testTag("btn_select_violet")
                            )
                        }
                    }

                    GameType.NUMBER -> {
                        Text(
                            text = "Tap a number (0–9) to record the outcome:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            (0..9).forEach { digit ->
                                val isSelected = uiState.selectedNumber == digit
                                val digitColor = when (ColorResult.fromNumber(digit)) {
                                    ColorResult.RED -> NeonRed
                                    ColorResult.GREEN -> NeonGreen
                                    ColorResult.VIOLET -> NeonViolet
                                    null -> NeonCyan
                                }

                                NumberSelectTile(
                                    digit = digit,
                                    accentColor = digitColor,
                                    isSelected = isSelected,
                                    onClick = {
                                        HapticHelper.performClickHaptic(context, hapticEnabled)
                                        viewModel.selectNumber(digit)
                                    },
                                    modifier = Modifier.weight(1f).testTag("btn_digit_$digit")
                                )
                            }
                        }
                    }
                }
            }

            // Optional Notes
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "OPTIONAL NOTES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.setNotes(it) },
                    placeholder = { Text("e.g. Session #1, tested after 5 reds...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save / Update Button
            Button(
                onClick = { viewModel.saveResult() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_save_result"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isEditMode) "UPDATE RECORD" else "SAVE OUTCOME",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSelectButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) color else color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun NumberSelectTile(
    digit: Int,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accentColor else accentColor.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = digit.toString(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

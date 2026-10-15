package com.example.ui.settings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsSystemDaydream
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.AppThemeMode
import com.example.ui.components.GamblerDisclaimerCard
import com.example.ui.components.NeonCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonRed

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingExportCsv by remember { mutableStateOf<String?>(null) }

    // SAF Create Document launcher for CSV export
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null && pendingExportCsv != null) {
            viewModel.writeExportDataToUri(context, uri, pendingExportCsv!!)
            pendingExportCsv = null
        }
    }

    // SAF Open Document launcher for CSV import
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importCsvFromUri(context, uri)
        }
    }

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SettingsEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SettingsEvent.ExportReady -> {
                    pendingExportCsv = event.csvData
                    exportFileLauncher.launch(event.suggestedFileName)
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "SETTINGS & DATA",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Application preferences and offline data management",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Appearance Theme Selector
        item {
            NeonCard(modifier = Modifier.fillMaxWidth().testTag("theme_setting_card")) {
                Text(
                    text = "APPEARANCE THEME",
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
                    ThemeOptionChip(
                        label = "System",
                        icon = Icons.Default.SettingsSystemDaydream,
                        isSelected = uiState.themeMode == AppThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                        modifier = Modifier.weight(1f).testTag("theme_system")
                    )
                    ThemeOptionChip(
                        label = "Dark",
                        icon = Icons.Default.DarkMode,
                        isSelected = uiState.themeMode == AppThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f).testTag("theme_dark")
                    )
                    ThemeOptionChip(
                        label = "Light",
                        icon = Icons.Default.LightMode,
                        isSelected = uiState.themeMode == AppThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f).testTag("theme_light")
                    )
                }
            }
        }

        // Haptic Feedback Toggle
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = NeonCyan
                        )
                        Column {
                            Text(
                                text = "Haptic Tactile Feedback",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Vibrate on button clicks and outcomes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = uiState.hapticEnabled,
                        onCheckedChange = { viewModel.setHapticEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }
            }
        }

        // CSV Import / Export Section
        item {
            NeonCard(modifier = Modifier.fillMaxWidth().testTag("csv_data_card")) {
                Text(
                    text = "DATA PORTABILITY & BACKUP (CSV)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Export your local dataset to standard CSV format or restore records from a backup file without needing internet access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.triggerExport() },
                        enabled = !uiState.isExporting && uiState.totalRecords > 0,
                        modifier = Modifier.weight(1f).testTag("btn_export_csv"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", color = NeonCyan)
                    }

                    OutlinedButton(
                        onClick = { importFileLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "text/plain")) },
                        enabled = !uiState.isImporting,
                        modifier = Modifier.weight(1f).testTag("btn_import_csv"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, NeonCyan)
                    ) {
                        if (uiState.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import CSV", color = NeonCyan)
                        }
                    }
                }
            }
        }

        // Clear All Data
        item {
            NeonCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonRed.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset Local Database",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonRed
                        )
                        Text(
                            text = "Permanently wipe all ${uiState.totalRecords} saved records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.promptClearAll() },
                        enabled = uiState.totalRecords > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_settings_clear_all")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wipe", color = Color.White)
                    }
                }
            }
        }

        // Privacy & Offline Notice
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                    Column {
                        Text(
                            text = "100% Offline & Private",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Color Predictor Pro operates strictly on-device using local SQLite storage (Room). No accounts, no external tracking, no cloud telemetry, and zero third-party platform scraping.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Gambler's Fallacy & Mathematical Philosophy
        item {
            GamblerDisclaimerCard()
        }

        // About Application Card
        item {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ABOUT COLOR PREDICTOR PRO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Version: 1.0.0 (Production Build)\nArchitecture: Jetpack Compose + MVVM + Room + Coroutines\nOffline Database: Room SQLite with indices\nDesign: Material 3 Cybernetic Gaming Dashboard",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }

    // CSV Import Report Dialog
    if (uiState.importReport != null) {
        val report = uiState.importReport!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportReport() },
            title = { Text("CSV Import Summary") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Successfully imported: ${report.validRecords.size} valid record(s).",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (report.errorMessages.isNotEmpty()) {
                        Text(
                            text = "Skipped invalid rows (${report.errorMessages.size}):",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = NeonRed
                        )
                        LazyColumn(modifier = Modifier.height(150.dp)) {
                            items(report.errorMessages) { err ->
                                Text(
                                    text = "• $err",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissImportReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("OK", color = Color.Black)
                }
            }
        )
    }

    // Double Confirmation Dialog for Clear All
    if (uiState.showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissClearAllDialog() },
            title = { Text("Permanently Erase All Data?") },
            text = { Text("This will delete all historical records from local SQLite. This action cannot be reversed.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmClearAll() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                    modifier = Modifier.testTag("btn_confirm_wipe")
                ) {
                    Text("Permanently Erase", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissClearAllDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeOptionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        label = {
            Text(
                text = label,
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
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = NeonCyan
        ),
        modifier = modifier
    )
}

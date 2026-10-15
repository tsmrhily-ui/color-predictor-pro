package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecord
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.PredictionRepository
import com.example.util.CsvHandler
import com.example.util.CsvImportResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val hapticEnabled: Boolean = true,
    val totalRecords: Int = 0,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val importReport: CsvImportResult? = null,
    val showClearAllDialog: Boolean = false
)

sealed class SettingsEvent {
    data class ShowToast(val message: String) : SettingsEvent()
    data class ExportReady(val csvData: String, val suggestedFileName: String) : SettingsEvent()
}

class SettingsViewModel(
    private val repository: PredictionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    private val _isImporting = MutableStateFlow(false)
    private val _importReport = MutableStateFlow<CsvImportResult?>(null)
    private val _showClearAllDialog = MutableStateFlow(false)

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow: SharedFlow<SettingsEvent> = _eventFlow.asSharedFlow()

    private data class OperationState(
        val isExporting: Boolean,
        val isImporting: Boolean,
        val importReport: CsvImportResult?,
        val showClearAllDialog: Boolean
    )

    private val _opState = combine(
        _isExporting,
        _isImporting,
        _importReport,
        _showClearAllDialog
    ) { exporting, importing, report, showClear ->
        OperationState(exporting, importing, report, showClear)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.themeMode,
        preferencesRepository.hapticEnabled,
        repository.totalCount,
        _opState
    ) { theme, haptic, count, op ->
        SettingsUiState(
            themeMode = theme,
            hapticEnabled = haptic,
            totalRecords = count,
            isExporting = op.isExporting,
            isImporting = op.isImporting,
            importReport = op.importReport,
            showClearAllDialog = op.showClearAllDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticEnabled(enabled)
        }
    }

    fun triggerExport() {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val records = repository.allRecords.first()
                if (records.isEmpty()) {
                    _eventFlow.emit(SettingsEvent.ShowToast("No records available to export."))
                    _isExporting.value = false
                    return@launch
                }
                val csv = CsvHandler.exportToCsv(records)
                val fileName = "ColorPredictorPro_${System.currentTimeMillis()}.csv"
                _eventFlow.emit(SettingsEvent.ExportReady(csv, fileName))
            } catch (e: Exception) {
                _eventFlow.emit(SettingsEvent.ShowToast("Export failed: ${e.localizedMessage}"))
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun writeExportDataToUri(context: Context, uri: Uri, csvData: String) {
        viewModelScope.launch {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(csvData)
                    }
                }
                _eventFlow.emit(SettingsEvent.ShowToast("Records successfully exported to CSV."))
            } catch (e: Exception) {
                _eventFlow.emit(SettingsEvent.ShowToast("Failed to write CSV file: ${e.localizedMessage}"))
            }
        }
    }

    fun importCsvFromUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val stringBuilder = java.lang.StringBuilder()
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line).append("\n")
                            line = reader.readLine()
                        }
                    }
                }

                val result = CsvHandler.importFromCsv(stringBuilder.toString())
                if (result.validRecords.isNotEmpty()) {
                    repository.insertAll(result.validRecords)
                }
                _importReport.value = result
            } catch (e: Exception) {
                _eventFlow.emit(SettingsEvent.ShowToast("Import failed: ${e.localizedMessage}"))
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun dismissImportReport() {
        _importReport.value = null
    }

    fun promptClearAll() {
        _showClearAllDialog.value = true
    }

    fun dismissClearAllDialog() {
        _showClearAllDialog.value = false
    }

    fun confirmClearAll() {
        viewModelScope.launch {
            repository.deleteAll()
            _showClearAllDialog.value = false
            _eventFlow.emit(SettingsEvent.ShowToast("All historical records successfully erased."))
        }
    }

    companion object {
        fun provideFactory(
            repository: PredictionRepository,
            preferencesRepository: UserPreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(repository, preferencesRepository) as T
            }
        }
    }
}

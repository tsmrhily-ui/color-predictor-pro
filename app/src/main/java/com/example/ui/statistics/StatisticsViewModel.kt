package com.example.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecord
import com.example.data.repository.PredictionRepository
import com.example.domain.engine.StatisticalAnalysisEngine
import com.example.domain.model.ColorStatsSummary
import com.example.domain.model.NumberStatsSummary
import com.example.domain.model.ObservationWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val selectedWindow: ObservationWindow = ObservationWindow.ALL,
    val totalCount: Int = 0,
    val windowedRecords: List<PredictionRecord> = emptyList(),
    val colorSummary: ColorStatsSummary = StatisticalAnalysisEngine.analyzeColors(emptyList()),
    val numberSummary: NumberStatsSummary = StatisticalAnalysisEngine.analyzeNumbers(emptyList()),
    val isSmallDataset: Boolean = true
)

class StatisticsViewModel(
    private val repository: PredictionRepository
) : ViewModel() {

    private val _selectedWindow = MutableStateFlow(ObservationWindow.ALL)
    val selectedWindow: StateFlow<ObservationWindow> = _selectedWindow

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.allRecords,
        _selectedWindow
    ) { allRecords, window ->
        val windowed = StatisticalAnalysisEngine.applyWindow(allRecords, window)
        val colorStats = StatisticalAnalysisEngine.analyzeColors(windowed)
        val numberStats = StatisticalAnalysisEngine.analyzeNumbers(windowed)

        StatisticsUiState(
            selectedWindow = window,
            totalCount = allRecords.size,
            windowedRecords = windowed,
            colorSummary = colorStats,
            numberSummary = numberStats,
            isSmallDataset = windowed.size < StatisticalAnalysisEngine.SMALL_DATASET_THRESHOLD
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState()
    )

    fun selectWindow(window: ObservationWindow) {
        _selectedWindow.value = window
    }

    companion object {
        fun provideFactory(repository: PredictionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatisticsViewModel(repository) as T
                }
            }
    }
}

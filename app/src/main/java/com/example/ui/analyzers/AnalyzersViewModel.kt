package com.example.ui.analyzers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecord
import com.example.data.repository.PredictionRepository
import com.example.domain.engine.StatisticalAnalysisEngine
import com.example.domain.model.ColorStatsSummary
import com.example.domain.model.ExperimentalStatisticalEstimate
import com.example.domain.model.GameType
import com.example.domain.model.NumberStatsSummary
import com.example.domain.model.ObservationWindow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AnalyzersUiState(
    val selectedTab: Int = 0, // 0: Red/Green, 1: Red/Green/Violet, 2: Number
    val selectedWindow: ObservationWindow = ObservationWindow.ALL,
    val allRecords: List<PredictionRecord> = emptyList(),
    val redGreenRecords: List<PredictionRecord> = emptyList(),
    val threeColorRecords: List<PredictionRecord> = emptyList(),
    val numberRecords: List<PredictionRecord> = emptyList(),
    val redGreenSummary: ColorStatsSummary = StatisticalAnalysisEngine.analyzeColors(emptyList()),
    val threeColorSummary: ColorStatsSummary = StatisticalAnalysisEngine.analyzeColors(emptyList()),
    val numberSummary: NumberStatsSummary = StatisticalAnalysisEngine.analyzeNumbers(emptyList()),
    val redGreenEstimate: ExperimentalStatisticalEstimate? = null,
    val threeColorEstimate: ExperimentalStatisticalEstimate? = null,
    val comparisonPeriodA: ColorStatsSummary? = null,
    val comparisonPeriodB: ColorStatsSummary? = null
)

class AnalyzersViewModel(
    private val repository: PredictionRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    private val _selectedWindow = MutableStateFlow(ObservationWindow.ALL)
    val selectedWindow: StateFlow<ObservationWindow> = _selectedWindow

    val uiState: StateFlow<AnalyzersUiState> = combine(
        repository.allRecords,
        _selectedTab,
        _selectedWindow
    ) { records, tab, window ->
        // Partition records
        val rgAll = records.filter { it.gameType == GameType.RED_GREEN.name }
        val rgvAll = records.filter { it.gameType == GameType.RED_GREEN_VIOLET.name }
        val numAll = records.filter { it.gameType == GameType.NUMBER.name || it.numberResult != null }

        // Windowed subsets
        val rgWindowed = StatisticalAnalysisEngine.applyWindow(rgAll, window)
        val rgvWindowed = StatisticalAnalysisEngine.applyWindow(rgvAll, window)
        val numWindowed = StatisticalAnalysisEngine.applyWindow(numAll, window)

        val rgSummary = StatisticalAnalysisEngine.analyzeColors(rgWindowed)
        val rgvSummary = StatisticalAnalysisEngine.analyzeColors(rgvWindowed)
        val numSummary = StatisticalAnalysisEngine.analyzeNumbers(numWindowed)

        val rgEstimate = StatisticalAnalysisEngine.calculateExperimentalEstimate(rgSummary)
        val rgvEstimate = StatisticalAnalysisEngine.calculateExperimentalEstimate(rgvSummary)

        // Historical split comparison for 3-color module
        var compA: ColorStatsSummary? = null
        var compB: ColorStatsSummary? = null
        if (rgvWindowed.size >= 6) {
            val midpoint = rgvWindowed.size / 2
            val recentHalf = rgvWindowed.subList(0, midpoint)
            val olderHalf = rgvWindowed.subList(midpoint, rgvWindowed.size)
            val (summaryA, summaryB) = StatisticalAnalysisEngine.comparePeriods(recentHalf, olderHalf)
            compA = summaryA
            compB = summaryB
        }

        AnalyzersUiState(
            selectedTab = tab,
            selectedWindow = window,
            allRecords = records,
            redGreenRecords = rgWindowed,
            threeColorRecords = rgvWindowed,
            numberRecords = numWindowed,
            redGreenSummary = rgSummary,
            threeColorSummary = rgvSummary,
            numberSummary = numSummary,
            redGreenEstimate = rgEstimate,
            threeColorEstimate = rgvEstimate,
            comparisonPeriodA = compA,
            comparisonPeriodB = compB
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyzersUiState()
    )

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectWindow(window: ObservationWindow) {
        _selectedWindow.value = window
    }

    companion object {
        fun provideFactory(repository: PredictionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AnalyzersViewModel(repository) as T
                }
            }
    }
}

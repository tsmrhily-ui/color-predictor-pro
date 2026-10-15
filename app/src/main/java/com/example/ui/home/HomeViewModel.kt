package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecord
import com.example.data.repository.PredictionRepository
import com.example.domain.engine.StatisticalAnalysisEngine
import com.example.domain.model.ColorStatsSummary
import com.example.domain.model.NumberStatsSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalRecords: Int = 0,
    val colorSummary: ColorStatsSummary = StatisticalAnalysisEngine.analyzeColors(emptyList()),
    val numberSummary: NumberStatsSummary = StatisticalAnalysisEngine.analyzeNumbers(emptyList()),
    val recentRecords: List<PredictionRecord> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: PredictionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.allRecords
        .map { records ->
            HomeUiState(
                totalRecords = records.size,
                colorSummary = StatisticalAnalysisEngine.analyzeColors(records),
                numberSummary = StatisticalAnalysisEngine.analyzeNumbers(records),
                recentRecords = records.take(15),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState(isLoading = true)
        )

    companion object {
        fun provideFactory(repository: PredictionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
    }
}

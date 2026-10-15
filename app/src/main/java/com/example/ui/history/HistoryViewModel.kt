package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecord
import com.example.data.repository.PredictionRepository
import com.example.domain.model.GameType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val records: List<PredictionRecord> = emptyList(),
    val searchQuery: String = "",
    val selectedGameTypeFilter: GameType? = null,
    val sortNewestFirst: Boolean = true,
    val totalCount: Int = 0,
    val recordToDelete: PredictionRecord? = null,
    val showClearAllDialog: Boolean = false
)

class HistoryViewModel(
    private val repository: PredictionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedGameTypeFilter = MutableStateFlow<GameType?>(null)
    private val _sortNewestFirst = MutableStateFlow(true)
    private val _recordToDelete = MutableStateFlow<PredictionRecord?>(null)
    private val _showClearAllDialog = MutableStateFlow(false)

    private data class FilterConfig(
        val search: String,
        val typeFilter: GameType?,
        val newestFirst: Boolean
    )

    private val _filterConfig = combine(_searchQuery, _selectedGameTypeFilter, _sortNewestFirst) { search, typeFilter, newestFirst ->
        FilterConfig(search, typeFilter, newestFirst)
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.allRecords,
        _filterConfig,
        _recordToDelete,
        _showClearAllDialog
    ) { allRecords, filter, toDelete, clearAll ->
        var filtered = allRecords

        // Filter by game type
        if (filter.typeFilter != null) {
            filtered = filtered.filter { it.gameType == filter.typeFilter.name }
        }

        // Filter by search query (round ID or notes)
        if (filter.search.isNotBlank()) {
            val q = filter.search.trim().lowercase()
            filtered = filtered.filter {
                it.roundId.lowercase().contains(q) || (it.notes?.lowercase()?.contains(q) == true)
            }
        }

        // Sort
        filtered = if (filter.newestFirst) {
            filtered.sortedWith(compareByDescending<PredictionRecord> { it.timestamp }.thenByDescending { it.id })
        } else {
            filtered.sortedWith(compareBy<PredictionRecord> { it.timestamp }.thenBy { it.id })
        }

        HistoryUiState(
            records = filtered,
            searchQuery = filter.search,
            selectedGameTypeFilter = filter.typeFilter,
            sortNewestFirst = filter.newestFirst,
            totalCount = allRecords.size,
            recordToDelete = toDelete,
            showClearAllDialog = clearAll
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGameTypeFilter(filter: GameType?) {
        _selectedGameTypeFilter.value = filter
    }

    fun toggleSortOrder() {
        _sortNewestFirst.value = !_sortNewestFirst.value
    }

    fun promptDeleteRecord(record: PredictionRecord) {
        _recordToDelete.value = record
    }

    fun dismissDeleteDialog() {
        _recordToDelete.value = null
    }

    fun confirmDeleteRecord() {
        val record = _recordToDelete.value ?: return
        viewModelScope.launch {
            repository.delete(record)
            _recordToDelete.value = null
        }
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
        }
    }

    companion object {
        fun provideFactory(repository: PredictionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(repository) as T
                }
            }
    }
}

package com.example.ui.addresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.PredictionRecord
import com.example.data.repository.PredictionRepository
import com.example.domain.model.ColorResult
import com.example.domain.model.GameType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AddEditUiState(
    val recordId: Long? = null,
    val isEditMode: Boolean = false,
    val gameType: GameType = GameType.RED_GREEN,
    val roundId: String = "",
    val selectedColor: ColorResult? = null,
    val selectedNumber: Int? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val duplicateWarning: String? = null
)

sealed class AddEditEvent {
    data class SaveSuccess(val message: String) : AddEditEvent()
    data class SaveError(val message: String) : AddEditEvent()
}

class AddEditResultViewModel(
    private val repository: PredictionRepository,
    initialRecordId: Long? = null,
    initialGameType: GameType? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddEditUiState(
            recordId = if (initialRecordId != null && initialRecordId > 0) initialRecordId else null,
            isEditMode = initialRecordId != null && initialRecordId > 0,
            gameType = initialGameType ?: GameType.RED_GREEN
        )
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddEditEvent>()
    val eventFlow: SharedFlow<AddEditEvent> = _eventFlow.asSharedFlow()

    init {
        if (initialRecordId != null && initialRecordId > 0) {
            loadExistingRecord(initialRecordId)
        } else {
            autoSuggestRoundId()
        }
    }

    private fun loadExistingRecord(id: Long) {
        viewModelScope.launch {
            val record = repository.getById(id)
            if (record != null) {
                _uiState.value = _uiState.value.copy(
                    recordId = record.id,
                    isEditMode = true,
                    gameType = GameType.fromString(record.gameType),
                    roundId = record.roundId,
                    selectedColor = ColorResult.fromString(record.colorResult),
                    selectedNumber = record.numberResult,
                    notes = record.notes ?: ""
                )
            }
        }
    }

    private fun autoSuggestRoundId() {
        viewModelScope.launch {
            val recent = repository.getRecentRecords(1).first()
            if (recent.isNotEmpty()) {
                val lastRound = recent.first().roundId
                // Try to parse trailing integer and increment
                val lastNum = lastRound.toLongOrNull()
                if (lastNum != null) {
                    _uiState.value = _uiState.value.copy(roundId = (lastNum + 1).toString())
                }
            }
        }
    }

    fun setGameType(type: GameType) {
        _uiState.value = _uiState.value.copy(
            gameType = type,
            // Clear incompatible selections
            selectedColor = if (type == GameType.RED_GREEN && _uiState.value.selectedColor == ColorResult.VIOLET) {
                null
            } else _uiState.value.selectedColor
        )
    }

    fun setRoundId(roundId: String) {
        _uiState.value = _uiState.value.copy(
            roundId = roundId.trim(),
            errorMessage = null,
            duplicateWarning = null
        )
    }

    fun selectColor(color: ColorResult) {
        _uiState.value = _uiState.value.copy(selectedColor = color, errorMessage = null)
    }

    fun selectNumber(number: Int) {
        val mappedColor = ColorResult.fromNumber(number)
        _uiState.value = _uiState.value.copy(
            selectedNumber = number,
            selectedColor = mappedColor ?: _uiState.value.selectedColor,
            errorMessage = null
        )
    }

    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveResult() {
        val state = _uiState.value
        val roundId = state.roundId.trim()

        if (roundId.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Round identifier is required.")
            return
        }

        // Validate outcome selection based on game type
        when (state.gameType) {
            GameType.RED_GREEN -> {
                if (state.selectedColor == null || state.selectedColor == ColorResult.VIOLET) {
                    _uiState.value = state.copy(errorMessage = "Please select RED or GREEN.")
                    return
                }
            }
            GameType.RED_GREEN_VIOLET -> {
                if (state.selectedColor == null) {
                    _uiState.value = state.copy(errorMessage = "Please select RED, GREEN, or VIOLET.")
                    return
                }
            }
            GameType.NUMBER -> {
                if (state.selectedNumber == null || state.selectedNumber !in 0..9) {
                    _uiState.value = state.copy(errorMessage = "Please select a valid number (0–9).")
                    return
                }
            }
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                // Check duplicate round ID if new record
                if (!state.isEditMode) {
                    val existing = repository.getByRoundId(roundId)
                    if (existing != null) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = "Round #$roundId already exists. Please choose a different Round ID or edit the existing record in History."
                        )
                        return@launch
                    }
                }

                val record = PredictionRecord(
                    id = state.recordId ?: 0,
                    gameType = state.gameType.name,
                    roundId = roundId,
                    colorResult = state.selectedColor?.name,
                    numberResult = state.selectedNumber,
                    notes = state.notes.trim().ifBlank { null },
                    timestamp = System.currentTimeMillis()
                )

                if (state.isEditMode) {
                    repository.update(record)
                    _eventFlow.emit(AddEditEvent.SaveSuccess("Round #$roundId successfully updated."))
                } else {
                    repository.insert(record)
                    _eventFlow.emit(AddEditEvent.SaveSuccess("Round #$roundId successfully saved."))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Error saving: ${e.localizedMessage}")
                _eventFlow.emit(AddEditEvent.SaveError("Failed to save: ${e.localizedMessage}"))
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: PredictionRepository,
            recordId: Long?,
            gameType: GameType?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddEditResultViewModel(repository, recordId, gameType) as T
            }
        }
    }
}

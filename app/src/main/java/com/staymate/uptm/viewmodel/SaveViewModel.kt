// SaveViewModel - the brain that remembers which posts wear magnets
package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.SaveRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// stream machine: which post ids wear a filled bookmark right now
sealed interface SaveUiState {
    data object Loading : SaveUiState
    data class Success(val savedIds: Set<String>) : SaveUiState
    data class Error(val message: String) : SaveUiState
}

// action machine: what the toggle hands are doing right now
sealed interface SaveToggleState {
    data object Idle : SaveToggleState
    data object Saving : SaveToggleState
    data class Error(val message: String) : SaveToggleState
}

@OptIn(ExperimentalCoroutinesApi::class)
class SaveViewModel(
    private val saveRepository: SaveRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _saveUiState = MutableStateFlow<SaveUiState>(SaveUiState.Loading)
    val saveUiState = _saveUiState.asStateFlow()

    private val _toggleState = MutableStateFlow<SaveToggleState>(SaveToggleState.Idle)
    val toggleState = _toggleState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.observeAuthUid()
                .flatMapLatest { uid ->
                    if (uid == null) flowOf(emptySet())
                    else saveRepository.observeSavedPostIds(uid)
                }
                .catch { e -> _saveUiState.value = SaveUiState.Error(e.message ?: "Could not load saved posts.") }
                .collect { ids -> _saveUiState.value = SaveUiState.Success(ids) }
        }
    }

    // stick or peel one magnet; the icon flips when the stream confirms, not on the tap
    fun toggleSave(postId: String) {
        viewModelScope.launch {
            if (_toggleState.value == SaveToggleState.Saving) return@launch
            val uid = authRepository.currentUid()
            if (uid == null) {
                _toggleState.value = SaveToggleState.Error("You are not logged in.")
                return@launch
            }
            val currentlySaved = (_saveUiState.value as? SaveUiState.Success)?.savedIds?.contains(postId) == true
            _toggleState.value = SaveToggleState.Saving
            val result = if (currentlySaved) {
                saveRepository.unsavePost(uid, postId)
            } else {
                saveRepository.savePost(uid, postId)
            }
            result.onSuccess { _toggleState.value = SaveToggleState.Idle }
                .onFailure { e -> _toggleState.value = SaveToggleState.Error(e.message ?: "Could not save. Try again.") }
        }
    }

    // wipe an old error so the next visit starts clean
    fun resetToggle() {
        _toggleState.value = SaveToggleState.Idle
    }
}

// two-waiter factory, same spelling on every screen that acquires it
class SaveViewModelFactory(
    private val saveRepository: SaveRepository,
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SaveViewModel::class.java)) {
            return SaveViewModel(saveRepository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
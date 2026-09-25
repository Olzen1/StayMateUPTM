package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.UserProfile
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
// function holds the small yes/no memory for the SAVE button only (separate from the load state)
sealed class ProfileSaveState {
    object Idle : ProfileSaveState()      // function nothing happening, button normal
    object Saving : ProfileSaveState()    // function write in flight, button locked
    object Success : ProfileSaveState()   // function write landed, dialog may close
    data class Error(val message: String) : ProfileSaveState() // function write failed, show red words
}
class ProfileViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAuthUid()
                .flatMapLatest { uid ->
                    if (uid == null) flowOf(null)
                    else repository.observeUserProfile(uid)
                }
                .catch { e -> _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile") }
                .collect { profile ->
                    _uiState.value = if (profile != null) {
                        ProfileUiState.Success(profile)
                    } else {
                        ProfileUiState.Loading
                    }
                }
        }
    }
    // function the save button's own memory, same muscle as _uiState
    private val _saveState = MutableStateFlow<ProfileSaveState>(ProfileSaveState.Idle)
    val saveState: StateFlow<ProfileSaveState> = _saveState.asStateFlow() // function read‑only window for the screen

    // function called when the user taps Save in the edit sheet
    fun saveProfile(fullName: String, course: String, semester: String) {
        // function lock the door while a write is already running (stops double‑tap spam)
        if (_saveState.value == ProfileSaveState.Saving) return

        viewModelScope.launch { // function do the slow network work off the UI thread
            _saveState.value = ProfileSaveState.Saving // function button goes to "Saving…"

            // function grab the signed‑in user's id straight from auth (the truth), not from the screen
            val uid = repository.currentUid()
            if (uid == null) { // function nobody signed in -> cannot write
                _saveState.value = ProfileSaveState.Error("Not signed in")
                return@launch // function stop here, do not try the write
            }

            // function ask the repository to fix ONLY the 3 edited lines (the correction‑pen tool from Piece 1)
            repository.updateUserProfile(uid, fullName, course, semester)
                .onSuccess { _saveState.value = ProfileSaveState.Success } // function write landed
                .onFailure { e -> // function write bounced (no network, rule denied, etc.)
                    _saveState.value = ProfileSaveState.Error(e.message ?: "Could not save profile")
                }
        }
    }

    // function wipe the save memory back to normal (used when the sheet closes, so old errors don't haunt the next open)
    fun resetSave() {
        _saveState.value = ProfileSaveState.Idle
    }
    }

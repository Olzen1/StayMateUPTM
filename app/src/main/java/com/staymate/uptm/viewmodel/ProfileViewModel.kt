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
    }

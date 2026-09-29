package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.UserProfile
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileSaveState {
    data object Idle : ProfileSaveState()
    data object Saving : ProfileSaveState()
    data object Success : ProfileSaveState()
    data class Error(val message: String) : ProfileSaveState()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val saveRepository = SaveRepository()
    private val postRepository = PostRepository()

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val userPostCount: StateFlow<Int> = repository.observeAuthUid()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(0)
            else postRepository.observePosts().map { posts -> posts.count { it.authorUid == uid } }
        }
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val savedPostCount: StateFlow<Int> = repository.observeAuthUid()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(0)
            else saveRepository.observeSavedPostIds(uid).map { it.size }
        }
        .catch { emit(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    private val _saveState = MutableStateFlow<ProfileSaveState>(ProfileSaveState.Idle)
    val saveState: StateFlow<ProfileSaveState> = _saveState.asStateFlow()

    fun saveProfile(fullName: String, course: String, semester: String) {
        if (_saveState.value == ProfileSaveState.Saving) return

        viewModelScope.launch {
            _saveState.value = ProfileSaveState.Saving

            val uid = repository.currentUid()
            if (uid == null) {
                _saveState.value = ProfileSaveState.Error("Not signed in")
                return@launch
            }

            repository.updateUserProfile(uid, fullName, course, semester)
                .onSuccess { _saveState.value = ProfileSaveState.Success }
                .onFailure { e ->
                    _saveState.value = ProfileSaveState.Error(e.message ?: "Could not save profile")
                }
        }
    }

    fun resetSave() {
        _saveState.value = ProfileSaveState.Idle
    }
}

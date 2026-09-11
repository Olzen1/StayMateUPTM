package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// The complete list of states the Login UI can ever be in.
sealed class AuthUiState {
    object Idle : AuthUiState()                      // nothing happened yet
    object Loading : AuthUiState()                   // network call in flight
    data class Success(val uid: String) : AuthUiState()  // logged in; carry the uid upward
    data class Error(val message: String) : AuthUiState() // failed; carry a human message
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()   // ViewModel talks to Repository, NEVER to Firebase

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle) // writable, private
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()           // readable, public

    fun signInWithEmail(email: String, password: String) {
        if (!repository.isUptmEmail(email)) {          // fail fast, no network needed
            _uiState.value = AuthUiState.Error("Please use your UPTM student email.")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            repository.signInOrCreateWithEmail(email, password)
                .onSuccess { uid -> _uiState.value = AuthUiState.Success(uid) }
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Login failed") }
        }
    }

    // ✅ THE EXERCISE ANSWER:
    fun signInWithGoogle(idToken: String) {
        _uiState.value = AuthUiState.Loading           // 1. tell UI: show spinner
        viewModelScope.launch {                        // 2. hop onto a lifecycle-safe thread
            repository.signInWithGoogle(idToken)       // 3. delegate; suspend until Firebase answers
                .onSuccess { uid -> _uiState.value = AuthUiState.Success(uid) }  // 4a. happy path
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Google sign-in failed") } // 4b. sad path
        }
    }
}
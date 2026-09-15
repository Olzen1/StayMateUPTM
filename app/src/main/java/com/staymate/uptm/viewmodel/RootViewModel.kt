package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth // <-- NEW
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StartupState {
    object Loading : StartupState()
    object NotSignedIn : StartupState()
    object NeedsOnboarding : StartupState()
    object Ready : StartupState()
}

class RootViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    // NEW: Listen to Auth changes (login/logout) in real-time
    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            _startupState.value = when {
                uid == null -> StartupState.NotSignedIn
                repository.doesUserProfileExist(uid) -> StartupState.Ready
                else -> StartupState.NeedsOnboarding
            }
        }
    }

    init {
        // Start listening when the app opens
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        // Stop listening when the app is destroyed to prevent memory leaks
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }

    // NEW: Clean logout function
    fun logout() {
        repository.signOut()
        // The authListener above will instantly catch this and flip the UI to LoginScreen!
    }
    fun checkStartup() {
        viewModelScope.launch {
            val uid = repository.currentUid()
            _startupState.value = when {
                uid == null -> StartupState.NotSignedIn
                repository.doesUserProfileExist(uid) -> StartupState.Ready
                else -> StartupState.NeedsOnboarding
            }
        }
    }
}
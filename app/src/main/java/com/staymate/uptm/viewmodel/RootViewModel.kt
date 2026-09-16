package com.staymate.uptm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

class RootViewModel(app: Application) : AndroidViewModel(app) {
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

    // logout function
    fun logout() {
        repository.signOut(getApplication()) // getApplication() hands over the stored Application Context
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
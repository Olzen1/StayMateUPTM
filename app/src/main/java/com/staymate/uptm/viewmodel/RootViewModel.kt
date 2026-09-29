package com.staymate.uptm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class StartupState {
    data object Loading : StartupState()
    data object NotSignedIn : StartupState()
    data object NeedsOnboarding : StartupState()
    data object Ready : StartupState()
    data object AdminReady : StartupState()
}

class RootViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = AuthRepository()

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { auth ->
        viewModelScope.launch {
            val user = auth.currentUser
            val uid = user?.uid
            val email = user?.email.orEmpty()
            _startupState.value = when {
                uid == null || !repository.isUptmEmail(email) -> {
                    if (uid != null) repository.signOut(getApplication())
                    StartupState.NotSignedIn
                }
                repository.isAdminEmail(email) -> StartupState.AdminReady
                repository.doesUserProfileExist(uid) -> StartupState.Ready
                else -> StartupState.NeedsOnboarding
            }
        }
    }

    init {
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
    }

    fun logout() {
        repository.signOut(getApplication())
    }

    fun checkStartup() {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid
            val email = user?.email.orEmpty()
            _startupState.value = when {
                uid == null || !repository.isUptmEmail(email) -> {
                    if (uid != null) repository.signOut(getApplication())
                    StartupState.NotSignedIn
                }
                repository.isAdminEmail(email) -> StartupState.AdminReady
                repository.doesUserProfileExist(uid) -> StartupState.Ready
                else -> StartupState.NeedsOnboarding
            }
        }
    }
}

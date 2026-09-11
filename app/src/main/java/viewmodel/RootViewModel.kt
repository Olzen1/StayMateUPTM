package com.staymate.uptm.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

        init { checkStartup() }   // runs automatically when the ViewModel is created

        fun checkStartup() {
            _startupState.value = StartupState.Loading
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

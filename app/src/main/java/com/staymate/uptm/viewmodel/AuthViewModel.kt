package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class AuthUiState {
    object Idle : AuthUiState()                      // nothing happened yet
    object Loading : AuthUiState()                   // network call in flight
    data class Success(val uid: String) : AuthUiState()  // logged in; carry the uid upward
    data class Error(val message: String) : AuthUiState() // failed; carry a human message
}
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

    class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

        private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle) //updating the state inside  functions
        val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

        // loginWithEmail function handles email + password login
        fun loginWithEmail(email: String, password: String) {

            // viewModelScope.launch starts background work
            // it is like asking Android to do this without freezing the screen
            viewModelScope.launch {
                val cleanEmail = email.trim()// trim removes extra spaces before/after email
                if (cleanEmail.isEmpty() || password.isEmpty()) { // check if user forgot email or password
                    _loginUiState.value = LoginUiState.Error("Email and password cannot be empty")
                    return@launch // stop here, no Firebase call
                }
                // check UPTM email domain before calling Firebase
                // this is a UX check so user gets fast error
                if (!authRepository.isUptmEmail(cleanEmail)) {
                    _loginUiState.value = LoginUiState.Error("Please use your UPTM student email")
                    return@launch // stop here, wrong domain
                }
                _loginUiState.value = LoginUiState.Loading // tell UI a request is running


                authRepository.signInWithEmail(cleanEmail, password) // sign-in only now; the Google door is the only registration path
                    .onSuccess { _loginUiState.value = LoginUiState.Success } // unchanged wiring
                    .onFailure { e -> _loginUiState.value = LoginUiState.Error(e.message ?: "Login failed") } // unchanged wiringS
            }
        }
        // loginWithGoogle function handles Google login after UI gives idToken
        fun loginWithGoogle(idToken: String) {

            // start background work
            viewModelScope.launch {
                _loginUiState.value = LoginUiState.Loading
                authRepository.signInWithGoogle(idToken) // repository returns Result and does NOT throw, so we read the Result object
                    .onSuccess { _loginUiState.value = LoginUiState.Success }// onSuccess lambda runs only when Result is success
                    .onFailure { e -> _loginUiState.value = LoginUiState.Error(e.message ?: "Google login failed") } // onFailure lambda hands us the wrapped exception
                try {
                    // repository sends Google idToken to Firebase
                    // repository also checks UPTM domain after Google sign-in
                    authRepository.signInWithGoogle(idToken)

                    // if no error happened, tell UI login success
                    _loginUiState.value = LoginUiState.Success

                } catch (e: Exception) {
                    // if Google/Firebase login fails, show error message
                    _loginUiState.value = LoginUiState.Error(e.message ?: "Google login failed")
                }
            }
        }
    }

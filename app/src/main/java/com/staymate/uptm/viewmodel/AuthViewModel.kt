package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope // import lets ViewModel use coroutine helper called viewModelScope
import kotlinx.coroutines.launch // import lets us use launch to run background work

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

    fun signInWithGoogle(idToken: String) {
        _uiState.value = AuthUiState.Loading           // 1. tell UI: show spinner
        viewModelScope.launch {                        // 2. hop onto a lifecycle-safe thread
            repository.signInWithGoogle(idToken)       // 3. delegate; suspend until Firebase answers
                .onSuccess { uid -> _uiState.value = AuthUiState.Success(uid) }  // 4a. happy path
                .onFailure { e -> _uiState.value = AuthUiState.Error(e.message ?: "Google sign-in failed") } // 4b. sad path
        }
    }
    class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

        private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle) //updating the state inside  functions
        val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

        // loginWithEmail function handles email + password login
        fun loginWithEmail(email: String, password: String) {

            // viewModelScope.launch starts background work
            // it is like asking Android to do this without freezing the screen
            viewModelScope.launch {

                // trim removes extra spaces before/after email
                val cleanEmail = email.trim()

                // check if user forgot email or password
                if (cleanEmail.isEmpty() || password.isEmpty()) {
                    _loginUiState.value = LoginUiState.Error("Email and password cannot be empty")
                    return@launch // stop here, no Firebase call
                }

                // check UPTM email domain before calling Firebase
                // this is a UX check so user gets fast error
                if (!authRepository.isUptmEmail(cleanEmail)) {
                    _loginUiState.value = LoginUiState.Error("Please use your UPTM student email")
                    return@launch // stop here, wrong domain
                }

                // set UI state to Loading so UI can show spinner/progress
                _loginUiState.value = LoginUiState.Loading

                // try means: attempt this action
                try {
                    // repository talks to Firebase for us
                    // this function signs in or creates account if user not found
                    authRepository.signInOrCreateWithEmail(cleanEmail, password)

                    // if no error happened, tell UI login success
                    _loginUiState.value = LoginUiState.Success

                } catch (e: Exception) {
                    // catch means: if error happened, handle it here
                    // e.message gives the error text from Firebase/Exception
                    _loginUiState.value = LoginUiState.Error(e.message ?: "Login failed")
                }
            }
        }

        // loginWithGoogle function handles Google login after UI gives idToken
        fun loginWithGoogle(idToken: String) {

            // start background work
            viewModelScope.launch {

                // set UI state to Loading so UI can show spinner/progress
                _loginUiState.value = LoginUiState.Loading

                // try means: attempt this action
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
}
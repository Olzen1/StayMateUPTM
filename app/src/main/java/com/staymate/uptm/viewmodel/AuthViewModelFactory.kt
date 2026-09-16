package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.staymate.uptm.repository.AuthRepository

class AuthViewModelFactory(
    // this is the repository that AuthViewModel needs
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {

    // Android calls this create function when it needs the ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // check if Android is asking for AuthViewModel
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

            // UNCHECKED_CAST warning is safe because we checked the class above
            @Suppress("UNCHECKED_CAST")

            // create AuthViewModel with repository and give it back
            return AuthViewModel(authRepository) as T
        }

        // if Android asks for wrong ViewModel, crash early with clear message
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
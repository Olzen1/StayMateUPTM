package com.staymate.uptm.viewmodel // function tells Android where this file lives

import androidx.lifecycle.ViewModel // function imports ViewModel base class
import androidx.lifecycle.ViewModelProvider // function imports the factory tool
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository // function imports Repository

class AddPostViewModelFactory(
    private val postRepository: PostRepository, // function receives the posts waiter
    private val authRepository: AuthRepository // function receives the login waiter
) : ViewModelProvider.Factory { // function implements the factory interface

    override fun <T : ViewModel> create(modelClass: Class<T>): T { // function is the required builder recipe
        if (modelClass.isAssignableFrom(AddPostViewModel::class.java)) { // function checks we are building the AddPostViewModel
            @Suppress("UNCHECKED_CAST") // function silences the known-safe cast warning
            return AddPostViewModel(postRepository, authRepository) as T // function builds the ViewModel with BOTH waiters
        }
        throw IllegalArgumentException("Unknown ViewModel class") // function crashes on purpose for wrong classes
    }
}
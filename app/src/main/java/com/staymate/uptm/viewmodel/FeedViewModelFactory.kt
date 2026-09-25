package com.staymate.uptm.viewmodel // function tells Android where this file lives

import androidx.lifecycle.ViewModel // function imports the base ViewModel class
import androidx.lifecycle.ViewModelProvider // function imports the factory tool
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository // function imports our Waiter

// function creates the Factory to build the FeedViewModel
@Suppress("UNCHECKED_CAST")
class FeedViewModelFactory(private val postRepository: PostRepository, private val authRepository: AuthRepository) : ViewModelProvider.Factory { // factory backpack now holds both waiters
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FeedViewModel(postRepository, authRepository) as T // hand both waiters to the ViewModel it builds
}
package com.staymate.uptm.viewmodel // function tells Android where this file lives

import androidx.lifecycle.ViewModel // function imports the base ViewModel class
import androidx.lifecycle.ViewModelProvider // function imports the factory tool
import com.staymate.uptm.repository.PostRepository // function imports our Waiter

// function creates the Factory to build the FeedViewModel
class FeedViewModelFactory(private val postRepository: PostRepository) : ViewModelProvider.Factory { // function makes a class that knows how to build ViewModels

    override fun <T : ViewModel> create(modelClass: Class<T>): T { // function is the required recipe for building ViewModels
        if (modelClass.isAssignableFrom(FeedViewModel::class.java)) { // function checks if we are trying to build a FeedViewModel
            @Suppress("UNCHECKED_CAST") // function tells Android to ignore a warning because we know what we are doing
            return FeedViewModel(postRepository) as T // function builds the FeedViewModel and gives it the Waiter
        }
        throw IllegalArgumentException("Unknown ViewModel class") // function crashes on purpose if we ask for the wrong ViewModel
    }
}
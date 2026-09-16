package com.staymate.uptm.viewmodel // function tells Android where this file lives

import androidx.lifecycle.ViewModel // function imports the base ViewModel class
import androidx.lifecycle.viewModelScope // function imports the tool to run background tasks
import com.staymate.uptm.model.Post // function imports our Post blueprint
import com.staymate.uptm.repository.PostRepository // function imports our Waiter
import kotlinx.coroutines.flow.MutableStateFlow // function imports the tank that holds the latest state
import kotlinx.coroutines.flow.StateFlow // function imports the read-only tank
import kotlinx.coroutines.flow.asStateFlow // function converts the private tank to a public read-only tank
import kotlinx.coroutines.launch // function starts a background task

// function creates a sealed interface to represent the different states of the feed
sealed interface FeedUiState { // function defines the specific shapes our UI state can take (like a vending machine with specific slots)
    data object Loading : FeedUiState // function means we are waiting for data
    data object Empty : FeedUiState // function means we got data, but the list is empty
    data class Success(val posts: List<Post>) : FeedUiState // function means we got a list of posts
    data class Error(val message: String) : FeedUiState // function means something went wrong
}

class FeedViewModel(private val postRepository: PostRepository) : ViewModel() { // function creates the Manager and gives it the Waiter

    // function creates a private tank to hold the current state of the feed
    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading) // function starts the tank with Loading state
    // function creates a public read-only tank for the UI to look at
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow() // function hides the private tank so the UI can't change it directly

    init { // function runs automatically when the ViewModel is created
        loadPosts() // function starts the process of getting posts
    }

    // function collects the live stream of posts from the repository
    private fun loadPosts() { // function defines the background task
        viewModelScope.launch { // function starts the task in the background without freezing the screen
            postRepository.observePosts().collect { posts -> // function drinks from the river of posts
                if (posts.isEmpty()) { // function checks if the river is dry
                    _feedUiState.value = FeedUiState.Empty // function updates the tank to Empty
                } else { // function means we have posts
                    _feedUiState.value = FeedUiState.Success(posts) // function updates the tank with the list of posts
                }
            }
        }
    }
}
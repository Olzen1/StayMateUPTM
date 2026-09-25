package com.staymate.uptm.viewmodel // function tells Android where this file lives

import androidx.lifecycle.ViewModel // function imports the base ViewModel class
import androidx.lifecycle.viewModelScope // function imports the tool to run background tasks
import com.staymate.uptm.model.Post // function imports our Post blueprint
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository // function imports our Waiter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow // function imports the tank that holds the latest state
import kotlinx.coroutines.flow.StateFlow // function imports the read-only tank
import kotlinx.coroutines.flow.asStateFlow // function converts the private tank to a public read-only tank
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch // function starts a background task

// function creates a sealed interface to represent the different states of the feed
sealed interface FeedUiState { // function defines the specific shapes our UI state can take (like a vending machine with specific slots)
    data object Loading : FeedUiState // function means we are waiting for data
    data object Empty : FeedUiState // function means we got data, but the list is empty
    data class Success(val posts: List<Post>) : FeedUiState // function means we got a list of posts
    data class Error(val message: String) : FeedUiState // function means something went wrong
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(private val postRepository: PostRepository, private val authRepository: AuthRepository) : ViewModel() { // function gives the engine BOTH the post waiter and the login sensor
    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading) // function starts the tank with Loading state
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow() // function makes the tank read-only for the UI

    init { // function runs automatically when the ViewModel is created
        viewModelScope.launch { // function starts a background worker
            authRepository.observeAuthUid() // function watches the login sensor for who is logged in
                .flatMapLatest { uid -> // function unplugs the old camera and plugs in a new one when uid changes
                    if (uid == null) flowOf(emptyList()) else postRepository.observePosts() // function shows empty list if logged out, or live posts if logged in
                }
                .catch { error -> // catch = seatbelt: a dying Flow lands here instead of crashing the app
                    _feedUiState.value = FeedUiState.Error(error.message ?: "Could not load posts.") // function shows the red Error card instead of a crash
                }
                .collect { posts -> // function receives every new list the walkie-talkie sends
                    _feedUiState.value = if (posts.isEmpty()) FeedUiState.Empty else FeedUiState.Success(posts) // traffic light: Empty vs Success
                }
        }
    }
}
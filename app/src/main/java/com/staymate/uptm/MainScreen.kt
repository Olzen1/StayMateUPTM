package com.staymate.uptm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog // function imports the pop-up dialog tool
import androidx.compose.material3.Text // function imports the text tool
import androidx.compose.material3.TextButton // function imports the button for dialogs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // function imports the ViewModel tool
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostViewModelFactory
import com.staymate.uptm.viewmodel.SaveViewModel
import com.staymate.uptm.viewmodel.SaveViewModelFactory

import com.staymate.uptm.viewmodel.SearchViewModel
import com.staymate.uptm.viewmodel.SearchViewModelFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen() {
    var currentRoute by rememberSaveable { mutableStateOf("home") } // function remembers the current screen (channel)
    var showCreateSheet by rememberSaveable { mutableStateOf(false) } // function remembers if the Create menu is open
    var showTypeSelector by rememberSaveable { mutableStateOf(false) } // function remembers if the Type pop-up is open
    var selectedPostType by rememberSaveable { mutableStateOf("") } // function remembers which type the user picked
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }

    val saveViewModel: SaveViewModel = viewModel(
        factory = SaveViewModelFactory(SaveRepository(), AuthRepository())
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(PostRepository())
    )
    val isSearchFiltering by searchViewModel.isFiltering.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // Content area (The TV Screen)
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentRoute) { // function checks which channel we are on
                "home" -> HomeScreen(
                    saveViewModel = saveViewModel,
                    // function runs when a post card is tapped
                    onPostClick = { postId ->
                        // function saves the tapped post id
                        selectedPostId = postId
                        // function changes the channel to post details
                        currentRoute = "post_details"
                    }
                )    // function shows the home feed
                "search" -> SearchScreen(
                    searchViewModel = searchViewModel,
                    saveViewModel = saveViewModel,
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "profile" -> ProfileScreen() // function shows the profile
                "add_post" -> AddPostScreen( // function shows the Add Post form
                    addPostViewModel = viewModel(factory = AddPostViewModelFactory(PostRepository(),
                        AuthRepository()
                    )), // function builds the Manager with BOTH waiters
                    postType = selectedPostType, // function passes the chosen type to the screen
                    onNavigateBack = { currentRoute = "home" }, // function goes back to home channel
                    onPostSuccess = { currentRoute = "home" } // function goes back to home channel after posting
                )
                "post_details" -> {
                    // function copies the saved id into a local safe value
                    val postId = selectedPostId

                    // function checks there is a real post id before showing details
                    if (postId != null) {
                        PostDetailsScreen(
                            postId = postId,
                            saveViewModel = saveViewModel,
                            onBack = { currentRoute = "home" }
                        )
                    } else {
                        // function shows a fallback if the app somehow opens details with no id
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No post selected")
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Screen: $currentRoute") // function shows placeholder for other screens
                    }
                }
            }
        }

        // Bottom nav (The Remote Buttons)
        // function marks screens that should not show the bottom remote
        val hideBottomNav = currentRoute == "add_post" || currentRoute == "post_details" || (currentRoute == "search" && isSearchFiltering)

// function shows the bottom nav only when we are not on a focus screen
        if (!hideBottomNav) { // function hides the bottom nav bar while the Add Post form is open
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                BottomNavBar( // function draws the nav bar
                    selectedRoute = currentRoute, // function highlights the current tab
                    onItemSelected = { route -> // function handles tab taps
                        if (route == "add") {
                            showCreateSheet = true // function opens the Create menu
                        } else {
                            currentRoute = route // function changes the channel
                        }
                    }
                )
            }
        }

        // Create options bottom sheet (Pop-up Menu 1)
        if (showCreateSheet) {
            CreateOptionsSheet(
                onDismiss = { showCreateSheet = false }, // function closes the menu
                onAddPost = {
                    showCreateSheet = false // function closes the Create menu
                    showTypeSelector = true // function opens the Type pop-up
                },
                onCreateGroup = {
                    showCreateSheet = false // function closes the Create menu
                }
            )
        }

        // Type Selector Pop-up
        if (showTypeSelector) {
            SelectPostTypeDialog(
                onDismiss = { showTypeSelector = false },
                onSelectType = { type ->
                    selectedPostType = type
                    showTypeSelector = false
                    currentRoute = "add_post"
                }
            )
        }
    }
}
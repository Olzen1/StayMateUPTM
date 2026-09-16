package com.staymate.uptm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.viewmodel.compose.viewModel // function imports the ViewModel tool
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostViewModelFactory

@Composable
fun MainScreen() {
    var currentRoute by rememberSaveable { mutableStateOf("home") } // function remembers the current screen (channel)
    var showCreateSheet by rememberSaveable { mutableStateOf(false) } // function remembers if the Create menu is open
    var showTypeSelector by rememberSaveable { mutableStateOf(false) } // function remembers if the Type pop-up is open
    var selectedPostType by rememberSaveable { mutableStateOf("") } // function remembers which type the user picked

    Box(modifier = Modifier.fillMaxSize()) {
        // Content area (The TV Screen)
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentRoute) { // function checks which channel we are on
                "home" -> HomeScreen() // function shows the home feed
                "profile" -> ProfileScreen() // function shows the profile
                "add_post" -> AddPostScreen( // function shows the Add Post form
                    addPostViewModel = viewModel(factory = AddPostViewModelFactory(PostRepository(),
                        AuthRepository()
                    )), // function builds the Manager with BOTH waiters
                    postType = selectedPostType, // function passes the chosen type to the screen
                    onNavigateBack = { currentRoute = "home" }, // function goes back to home channel
                    onPostSuccess = { currentRoute = "home" } // function goes back to home channel after posting
                )
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Screen: $currentRoute") // function shows placeholder for other screens
                    }
                }
            }
        }

        // Bottom nav (The Remote Buttons)
        if (currentRoute != "add_post") { // function hides the bottom nav bar while the Add Post form is open
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

        // Type Selector Pop-up (Pop-up Menu 2)
        if (showTypeSelector) { // function checks if the Type pop-up should be on screen
            AlertDialog( // function creates the pop-up box
                onDismissRequest = { showTypeSelector = false }, // function closes pop-up if tapped outside
                title = { Text("Select Post Type") }, // function shows the pop-up title
                text = { Text("What kind of post is this?") }, // function shows the pop-up message
                confirmButton = { // function holds the first choice button
                    TextButton(onClick = { // function handles the click
                        selectedPostType = UptmConstants.POST_TYPE_HOUSE_SUGGESTION // function saves the choice using the name tag, not a hand-typed string
                        showTypeSelector = false // function closes the pop-up
                        currentRoute = "add_post" // function changes channel to Add Post
                    }) {
                        Text("House Suggestion") // function shows button text
                    }
                },
                dismissButton = { // function holds the second choice button
                    TextButton(onClick = { // function handles the click
                        selectedPostType = UptmConstants.POST_TYPE_HOUSEMATE_WANTED // function saves the choice using the name tag, not a hand-typed string
                        showTypeSelector = false // function closes the pop-up
                        currentRoute = "add_post" // function changes channel to Add Post
                    }) {
                        Text("Housemate Wanted") // function shows button text
                    }
                }
            )
        }
    }
}
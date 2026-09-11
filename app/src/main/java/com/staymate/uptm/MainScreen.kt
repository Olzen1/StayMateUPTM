package com.staymate.uptm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainScreen() {
    var currentRoute by rememberSaveable { mutableStateOf("home") }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Content area
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentRoute) {
                "home" -> HomeScreen()
                "profile" -> ProfileScreen()
                // Add other screens here as they are implemented
                else -> {
                    // Placeholder for unimplemented screens
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text("Screen: $currentRoute")
                    }
                }
            }
        }

        // Bottom nav
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavBar(
                selectedRoute = currentRoute,
                onItemSelected = { route ->
                    if (route == "add") {
                        showCreateSheet = true
                    } else {
                        currentRoute = route
                    }
                }
            )
        }

        // Create options bottom sheet
        if (showCreateSheet) {
            CreateOptionsSheet(
                onDismiss = { showCreateSheet = false },
                onAddPost = {
                    showCreateSheet = false
                    // TODO: Navigate to Add Post screen
                },
                onCreateGroup = {
                    showCreateSheet = false
                    // TODO: Navigate to Create Group screen
                }
            )
        }
    }
}

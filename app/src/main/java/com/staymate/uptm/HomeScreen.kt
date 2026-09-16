package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // function imports the tool to get ViewModels in UI
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.viewmodel.FeedUiState
import com.staymate.uptm.viewmodel.FeedViewModel
import com.staymate.uptm.viewmodel.FeedViewModelFactory

@Composable
fun HomeScreen(
    feedViewModel: FeedViewModel = viewModel(factory = FeedViewModelFactory(PostRepository())) // function creates the Manager and gives it the Waiter
) {
    val feedUiState by feedViewModel.feedUiState.collectAsStateWithLifecycle() // function watches the Manager's tank and updates the UI automatically

    // The Traffic Light: Decides what to show on the screen
    when (feedUiState) { // function checks which state the Manager is currently in

        is FeedUiState.Loading -> { // function means we are waiting for Firestore
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // function centers the spinner on the screen
                CircularProgressIndicator() // function shows the spinning loading circle
            }
        }

        is FeedUiState.Empty -> { // function means Firestore is empty
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // function centers the text
                Text("No posts yet. Be the first to post!") // function shows a friendly message
            }
        }

        is FeedUiState.Error -> { // function means something went wrong
            val error = feedUiState as FeedUiState.Error // function safely grabs the error message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // function centers the text
                Text("Error: ${error.message}") // function shows what broke
            }
        }

        is FeedUiState.Success -> { // function means we have posts!
            val posts = (feedUiState as FeedUiState.Success).posts // function extracts the list of posts from the state

            // For now, let's just show a simple list of titles to prove it works!
            LazyColumn(modifier = Modifier.fillMaxSize()) { // function creates a scrollable vertical list
                items(posts) { post -> // function loops through every post in the list
                    Text(
                        text = post.title, // function shows the title
                        modifier = Modifier.padding(16.dp), // function adds some space around the text
                        style = MaterialTheme.typography.headlineSmall // function makes the text look nice and big
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 70.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    .background(Color(0xFF0091FF))
                    .padding(horizontal = 20.dp, vertical = 40.dp)
            ) {
                Column {
                    Text(
                        text = "StayMate UPTM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Find your perfect housemate",
                        fontSize = 14.sp,
                        color = Color(0xB3D9FF)
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(8.dp)
                    )
                }
            }

            // Posts area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No posts yet",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
            }
        }
    }
}

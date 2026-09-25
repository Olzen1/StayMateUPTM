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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
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
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.viewmodel.FeedUiState
import com.staymate.uptm.viewmodel.FeedViewModel
import com.staymate.uptm.viewmodel.FeedViewModelFactory

@Composable
fun HomeScreen(
    feedViewModel: FeedViewModel = viewModel(factory = FeedViewModelFactory(PostRepository(),
        AuthRepository())),onPostClick: (String) -> Unit = {}, // // forwards the tapped post's id upward; default = no-op until 3b wires it // function creates the Manager and gives it both Waiters
) {
    val feedUiState by feedViewModel.feedUiState.collectAsStateWithLifecycle() // function watches the Manager's tank and updates the UI automatically

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
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
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 40.dp)
            ) {
                Column {
                    Text(
                        text = "StayMate UPTM",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onPrimary,

                    )
                    Text(
                        text = "Find your perfect housemate",
                        fontSize = 14.sp,
                        color = colorScheme.onPrimary
                    )
                }

                IconButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.TopEnd),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFFFFF))
                            .padding(8.dp)
                    )
                }
            }

            // Posts area - The Traffic Light: Decides what to show based on the data
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (feedUiState) {
                    is FeedUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is FeedUiState.Empty -> {
                        Text(
                            text = "No posts yet. Be the first to post!",
                            fontSize = 14.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    is FeedUiState.Error -> {
                        val error = feedUiState as FeedUiState.Error
                        Text(
                            text = "Error: ${error.message}",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is FeedUiState.Success -> {
                        val posts = (feedUiState as FeedUiState.Success).posts
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(posts) { post ->

                                PostCard(post = post, onClick = { onPostClick(post.id) }) // // hand the id up when the card is tapped
                            }
                        }
                    }
                }
            }
        }
    }
}

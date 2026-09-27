package com.staymate.uptm

// Gate 5 - second brain + the failure sticky-note
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.staymate.uptm.utils.PostNotificationHelper
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.viewmodel.FeedUiState
import com.staymate.uptm.viewmodel.FeedViewModel
import com.staymate.uptm.viewmodel.FeedViewModelFactory
import com.staymate.uptm.viewmodel.SaveToggleState
import com.staymate.uptm.viewmodel.SaveUiState
import com.staymate.uptm.viewmodel.SaveViewModel
import com.staymate.uptm.viewmodel.SaveViewModelFactory

@Composable

fun HomeScreen(
    feedViewModel: FeedViewModel = viewModel(factory = FeedViewModelFactory(PostRepository(), AuthRepository())),
    saveViewModel: SaveViewModel = viewModel(factory = SaveViewModelFactory(
        SaveRepository(),
        AuthRepository()
    )
    ),
    onNotificationClick: () -> Unit = {},
    onPostClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val feedUiState by feedViewModel.feedUiState.collectAsStateWithLifecycle()

// magnet-brain reads: which post ids wear a filled bookmark right now
    val saveUiState by saveViewModel.saveUiState.collectAsStateWithLifecycle()
    val savedIds = (saveUiState as? SaveUiState.Success)?.savedIds ?: emptySet()
    val toggleState by saveViewModel.toggleState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var previousPostIds by remember { mutableStateOf<Set<String>?>(null) }

    LaunchedEffect(feedUiState) {
        if (feedUiState is FeedUiState.Success) {
            val posts = (feedUiState as FeedUiState.Success).posts
            val currentUid = AuthRepository().currentUid()
            if (previousPostIds != null) {
                val newPostsFromOthers = posts.filter { post ->
                    !previousPostIds!!.contains(post.id) && post.authorUid != currentUid
                }
                newPostsFromOthers.forEach { newPost ->
                    PostNotificationHelper.showNewPostNotification(context, newPost.title)
                }
            }
            previousPostIds = posts.map { it.id }.toSet()
        }
    }

// the failure sticky-note: pops up only when a save/unsave is rejected, then dissolves
    LaunchedEffect(toggleState) {
        val ts = toggleState                       // local-val copy: smart-cast a delegated val safely
        if (ts is SaveToggleState.Error) {
            snackbarHostState.showSnackbar(ts.message)
            saveViewModel.resetToggle()            // clear it so it can't ghost on your next visit
        }
    }
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
                    onClick = onNotificationClick,
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
                        val posts = (feedUiState as FeedUiState.Success).posts.filter { it.type != "group_finding" }
                        if (posts.isEmpty()) {
                            Text(
                                text = "No posts yet. Be the first to post!",
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(posts) { post ->
                                    PostCard(
                                        post = post,
                                        onClick = { onPostClick(post.id) },
                                        isSaved = savedIds.contains(post.id),
                                        onBookmarkClick = { saveViewModel.toggleSave(post.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        // the snackbar floats above the bottom nav
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
        )
    }
}

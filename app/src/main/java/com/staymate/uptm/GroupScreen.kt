package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.viewmodel.SaveUiState
import com.staymate.uptm.viewmodel.SaveViewModel

@Composable
fun GroupScreen(
    saveViewModel: SaveViewModel,
    onPostClick: (String) -> Unit = {}
) {
    val postRepository = remember { PostRepository() }
    val postsState by postRepository.observePosts().collectAsStateWithLifecycle(initialValue = emptyList())
    val saveUiState by saveViewModel.saveUiState.collectAsStateWithLifecycle()
    val savedIds = (saveUiState as? SaveUiState.Success)?.savedIds ?: emptySet()

    val groupPosts = remember(postsState) {
        postsState.filter { it.type == "group_finding" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header title "GROUP"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FIND YOUR GROUP",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (groupPosts.isEmpty()) {
                Text(
                    text = "No finding a group posts yet.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 70.dp)
                ) {
                    items(groupPosts) { post ->
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

package com.staymate.uptm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostViewModel
import com.staymate.uptm.viewmodel.AddPostViewModelFactory
import com.staymate.uptm.viewmodel.NotificationsViewModel
import com.staymate.uptm.viewmodel.SaveViewModel
import com.staymate.uptm.viewmodel.SaveViewModelFactory
import com.staymate.uptm.viewmodel.SearchViewModel
import com.staymate.uptm.viewmodel.SearchViewModelFactory

@Composable
fun MainScreen() {
    var currentRoute by rememberSaveable { mutableStateOf("home") }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    var showTypeSelector by rememberSaveable { mutableStateOf(false) }
    var selectedPostType by rememberSaveable { mutableStateOf("") }
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }

    val saveViewModel: SaveViewModel = viewModel(
        factory = SaveViewModelFactory(SaveRepository(), AuthRepository())
    )
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(PostRepository())
    )
    val addPostViewModel: AddPostViewModel = viewModel(
        factory = AddPostViewModelFactory(PostRepository(), AuthRepository())
    )
    val notificationsViewModel: NotificationsViewModel = viewModel()

    val isSearchFiltering by searchViewModel.isFiltering.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentRoute) {
                "home" -> HomeScreen(
                    saveViewModel = saveViewModel,
                    onNotificationClick = { currentRoute = "notifications" },
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "notifications" -> NotificationsScreen(
                    onBack = { currentRoute = "home" },
                    notificationsViewModel = notificationsViewModel
                )
                "search" -> SearchScreen(
                    searchViewModel = searchViewModel,
                    saveViewModel = saveViewModel,
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "group" -> GroupScreen(
                    saveViewModel = saveViewModel,
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "profile" -> ProfileScreen(
                    onNavigateToSavedPosts = { currentRoute = "saved_posts" },
                    onNavigateToEditPosts = { currentRoute = "edit_posts" },
                    onNavigateToEditFindingGroup = { currentRoute = "edit_finding_group" }
                )
                "saved_posts" -> SavedPostsScreen(
                    saveViewModel = saveViewModel,
                    onBack = { currentRoute = "profile" },
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "edit_posts" -> EditPostsScreen(
                    onBack = { currentRoute = "profile" },
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "edit_finding_group" -> EditFindingGroupScreen(
                    onBack = { currentRoute = "profile" },
                    onPostClick = { postId ->
                        selectedPostId = postId
                        currentRoute = "post_details"
                    }
                )
                "edit_post_form" -> AddPostScreen(
                    addPostViewModel = addPostViewModel,
                    postType = selectedPostType,
                    onNavigateBack = { currentRoute = "edit_posts" },
                    onPostSuccess = { currentRoute = "edit_posts" }
                )
                "add_post" -> AddPostScreen(
                    addPostViewModel = addPostViewModel,
                    postType = selectedPostType,
                    onNavigateBack = { currentRoute = "home" },
                    onPostSuccess = { currentRoute = "home" }
                )
                "post_details" -> {
                    val postId = selectedPostId
                    if (postId != null) {
                        PostDetailsScreen(
                            postId = postId,
                            onBack = { currentRoute = "home" },
                            onEditClick = { post ->
                                addPostViewModel.populateForEditing(post)
                                selectedPostType = if (post.type == UptmConstants.POST_TYPE_KEY_SUGGESTION) {
                                    UptmConstants.POST_TYPE_HOUSE_SUGGESTION
                                } else if (post.type == UptmConstants.POST_TYPE_KEY_GROUP_FINDING) {
                                    UptmConstants.POST_TYPE_GROUP_FINDING
                                } else {
                                    UptmConstants.POST_TYPE_HOUSEMATE_WANTED
                                }
                                currentRoute = "edit_post_form"
                            },
                            saveViewModel = saveViewModel
                        )
                    } else {
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
                        Text("Screen: $currentRoute")
                    }
                }
            }
        }

        val hideBottomNav = currentRoute == "add_post" ||
                currentRoute == "edit_post_form" ||
                currentRoute == "post_details" ||
                currentRoute == "saved_posts" ||
                currentRoute == "edit_posts" ||
                currentRoute == "edit_finding_group" ||
                currentRoute == "notifications" ||
                (currentRoute == "search" && isSearchFiltering)

        if (!hideBottomNav) {
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
        }

        if (showCreateSheet) {
            CreateOptionsSheet(
                onDismiss = { showCreateSheet = false },
                onAddPost = {
                    showCreateSheet = false
                    addPostViewModel.resetForm()
                    showTypeSelector = true
                },
                onCreateGroup = {
                    showCreateSheet = false
                    addPostViewModel.resetForm()
                    selectedPostType = UptmConstants.POST_TYPE_GROUP_FINDING
                    currentRoute = "add_post"
                }
            )
        }

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

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.AddPostViewModel
import com.staymate.uptm.viewmodel.AddPostViewModelFactory
import com.staymate.uptm.viewmodel.AdminViewModel
import com.staymate.uptm.viewmodel.RootViewModel
import com.staymate.uptm.viewmodel.SaveViewModel
import com.staymate.uptm.viewmodel.SaveViewModelFactory

@Composable
fun AdminMainScreen(
    rootViewModel: RootViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel()
) {
    var currentRoute by rememberSaveable { mutableStateOf("admin_dashboard") }
    var previousRoute by rememberSaveable { mutableStateOf("admin_dashboard") }
    var selectedPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPostType by rememberSaveable { mutableStateOf("") }

    val saveViewModel: SaveViewModel = viewModel(
        factory = SaveViewModelFactory(SaveRepository(), AuthRepository())
    )
    val addPostViewModel: AddPostViewModel = viewModel(
        factory = AddPostViewModelFactory(PostRepository(), AuthRepository())
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentRoute) {
                "admin_dashboard" -> AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    onLogout = { rootViewModel.logout() }
                )
                "admin_posts" -> AdminPostsScreen(
                    adminViewModel = adminViewModel,
                    onPostClick = { postId ->
                        selectedPostId = postId
                        previousRoute = "admin_posts"
                        currentRoute = "post_details"
                    },
                    onEditClick = { post ->
                        addPostViewModel.populateForEditing(post)
                        selectedPostType = when (post.type) {
                            UptmConstants.POST_TYPE_KEY_SUGGESTION -> UptmConstants.POST_TYPE_HOUSE_SUGGESTION
                            UptmConstants.POST_TYPE_KEY_GROUP_FINDING -> UptmConstants.POST_TYPE_GROUP_FINDING
                            else -> UptmConstants.POST_TYPE_HOUSEMATE_WANTED
                        }
                        previousRoute = "admin_posts"
                        currentRoute = "admin_edit_post"
                    }
                )
                "admin_reports" -> AdminReportsScreen(
                    adminViewModel = adminViewModel,
                    onPostClick = { postId ->
                        selectedPostId = postId
                        previousRoute = "admin_reports"
                        currentRoute = "post_details"
                    },
                    onEditClick = { post ->
                        addPostViewModel.populateForEditing(post)
                        selectedPostType = when (post.type) {
                            UptmConstants.POST_TYPE_KEY_SUGGESTION -> UptmConstants.POST_TYPE_HOUSE_SUGGESTION
                            UptmConstants.POST_TYPE_KEY_GROUP_FINDING -> UptmConstants.POST_TYPE_GROUP_FINDING
                            else -> UptmConstants.POST_TYPE_HOUSEMATE_WANTED
                        }
                        previousRoute = "admin_reports"
                        currentRoute = "admin_edit_post"
                    }
                )
                "admin_users" -> AdminUsersScreen(
                    adminViewModel = adminViewModel
                )
                "admin_edit_post" -> AddPostScreen(
                    addPostViewModel = addPostViewModel,
                    postType = selectedPostType,
                    onNavigateBack = { currentRoute = previousRoute },
                    onPostSuccess = { currentRoute = previousRoute }
                )
                "post_details" -> {
                    val postId = selectedPostId
                    if (postId != null) {
                        PostDetailsScreen(
                            postId = postId,
                            onBack = {
                                selectedPostId = null
                                currentRoute = previousRoute
                            },
                            onEditClick = { post ->
                                addPostViewModel.populateForEditing(post)
                                selectedPostType = when (post.type) {
                                    UptmConstants.POST_TYPE_KEY_SUGGESTION -> UptmConstants.POST_TYPE_HOUSE_SUGGESTION
                                    UptmConstants.POST_TYPE_KEY_GROUP_FINDING -> UptmConstants.POST_TYPE_GROUP_FINDING
                                    else -> UptmConstants.POST_TYPE_HOUSEMATE_WANTED
                                }
                                currentRoute = "admin_edit_post"
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
            }
        }

        val hideBottomNav = currentRoute == "post_details" || currentRoute == "admin_edit_post"

        if (!hideBottomNav) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                AdminBottomNavBar(
                    selectedRoute = currentRoute,
                    onItemSelected = { route ->
                        currentRoute = route
                    }
                )
            }
        }
    }
}

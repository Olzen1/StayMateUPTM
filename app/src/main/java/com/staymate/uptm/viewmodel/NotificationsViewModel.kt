package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.staymate.uptm.model.Post
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.catch

data class AppNotification(
    val id: String,
    val title: String,
    val postTitle: String,
    val timestamp: Timestamp
)

class NotificationsViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private var knownPostIds: Set<String>? = null

    init {
        viewModelScope.launch {
            postRepository.observePosts()
                .catch { /* handle error cleanly */ }
                .collect { posts ->
                    val currentUid = authRepository.currentUid()
                    if (knownPostIds != null) {
                        val newPosts = posts.filter { post ->
                            !knownPostIds!!.contains(post.id) && post.authorUid != currentUid
                        }

                        if (newPosts.isNotEmpty()) {
                            val newNotifs = newPosts.map { post ->
                                AppNotification(
                                    id = post.id,
                                    title = "NEW POST",
                                    postTitle = post.title,
                                    timestamp = post.createdAt
                                )
                            }
                            _notifications.value = (newNotifs + _notifications.value).distinctBy { it.id }
                        }
                    }
                    knownPostIds = posts.map { it.id }.toSet()
                }
        }
    }
}

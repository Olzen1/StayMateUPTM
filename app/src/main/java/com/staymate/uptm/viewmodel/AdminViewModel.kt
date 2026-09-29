package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.Post
import com.staymate.uptm.model.UserProfile
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

// flatMapLatest is experimental: same waiver slip as the saves brain
@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModel(
    private val postRepository: PostRepository = PostRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    // one shared "what went wrong" note the screen shows as red words (last error wins — fine for now)
    private val _adminError = MutableStateFlow<String?>(null)
    val adminError: StateFlow<String?> = _adminError.asStateFlow()

    val allPosts: StateFlow<List<Post>> = authRepository.observeAuthUid()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())          // nobody home: honest empty, no listener at all
            else postRepository.observePosts()
                .catch { e -> _adminError.value = "Posts feed blocked: ${e.message}"; emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<Map<String, Any>>> = authRepository.observeAuthUid()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else postRepository.observeReports()
                .catch { e -> _adminError.value = "Reports feed blocked: ${e.message}"; emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedPosts: StateFlow<List<Post>> = combine(allPosts, reports) { posts, reportList ->
        val reportedPostIds = reportList.mapNotNull { it["postId"] as? String }.toSet()
        posts.filter { post -> post.id in reportedPostIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserProfile>> = authRepository.observeAuthUid()
        .flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList())
            else authRepository.observeAllUsers()
                .catch { e -> _adminError.value = "Users feed blocked: ${e.message}"; emit(emptyList()) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // recent activity = the newest posts we already can read, relabelled — no denied door needed
    val recentActivities: StateFlow<List<Map<String, Any>>> = allPosts.map { posts ->
        posts.take(10).map { post ->
            mapOf(
                "type" to if (post.type == "group_finding") "New Group Post Added" else "New Post Added",
                "postTitle" to post.title
            )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // >>> the four count vals + deletePost below this line are UNCHANGED <<<
    val houseSuggestionCount: StateFlow<Int> = allPosts.map { posts ->
        posts.count { it.type == "house_suggestion" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val housemateWantedCount: StateFlow<Int> = allPosts.map { posts ->
        posts.count { it.type == "housemate_wanted" || it.type.isBlank() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val groupFindingCount: StateFlow<Int> = allPosts.map { posts ->
        posts.count { it.type == "group_finding" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalUsersCount: StateFlow<Int> = allUsers.map { users ->
        users.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId)
        }
    }
}

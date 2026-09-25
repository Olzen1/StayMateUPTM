package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.Post
import com.staymate.uptm.repository.PostRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// the four honest states the details screen can draw (top-level board, mirrors FeedUiState)
sealed interface PostDetailsUiState {
    data object Loading : PostDetailsUiState          // no id picked yet / first frame
    data class Success(val post: Post) : PostDetailsUiState  // got the live doc
    data object NotFound : PostDetailsUiState         // doc vanished or bad id
    data class Error(val message: String) : PostDetailsUiState  // read was denied/broke
}

// details engine; the id is PUSHED IN by the screen (select), not baked into the constructor
@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailsViewModel(private val postRepository: PostRepository) : ViewModel() {

    // which post the screen wants right now; null = nothing tapped yet
    private val _postId = MutableStateFlow<String?>(null)

    // the screen calls this every time it appears (the "bike bell" that re-plugs the listener)
    fun select(id: String) {
        _postId.value = id
    }

    private val _postDetailsUiState = MutableStateFlow<PostDetailsUiState>(PostDetailsUiState.Loading)
    val postDetailsUiState: StateFlow<PostDetailsUiState> = _postDetailsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // new id -> drop the old doc listener, plug a fresh one (same flatMapLatest muscle as feed)
            _postId.flatMapLatest { id ->
                if (id == null) {
                    flowOf<PostDetailsUiState>(PostDetailsUiState.Loading)
                } else {
                    postRepository.observePostById(id)
                        .map { post ->
                            // null from the repo = the doc is gone -> honest NotFound, not a crash
                            if (post == null) PostDetailsUiState.NotFound
                            else PostDetailsUiState.Success(post)
                        }
                        .catch { e ->
                            // seatbelt (locked habit): a denied/broken read lands here as calm Error
                            emit(PostDetailsUiState.Error(e.message ?: "Could not load this post"))
                        }
                }
            }.collect { state ->
                _postDetailsUiState.value = state
            }
        }
    }
}

// one waiter only: details reads posts, never auth, so it doesn't pay rent on an empty auth room
class PostDetailsViewModelFactory(
    private val postRepository: PostRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PostDetailsViewModel(postRepository) as T
    }
}
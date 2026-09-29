package com.staymate.uptm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.staymate.uptm.model.Post
import com.staymate.uptm.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SearchFilterState(
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val selectedBedrooms: Set<Int> = emptySet(), // 1, 2, 3, 4, 5 (for 5+)
    val moveInFromMs: Long? = null,
    val moveInToMs: Long? = null,
    val propertyType: String = "",
    val furnishedStatus: String = "",
    val genderPreference: String = ""
) {
    val isActive: Boolean
        get() = minPrice != null || maxPrice != null || selectedBedrooms.isNotEmpty() ||
                moveInFromMs != null || moveInToMs != null || propertyType.isNotBlank() ||
                furnishedStatus.isNotBlank() || genderPreference.isNotBlank()
}

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val posts: List<Post>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

class SearchViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(SearchFilterState())
    val filterState: StateFlow<SearchFilterState> = _filterState.asStateFlow()

    private val _draftFilterState = MutableStateFlow(SearchFilterState())
    val draftFilterState: StateFlow<SearchFilterState> = _draftFilterState.asStateFlow()

    private val _isFiltering = MutableStateFlow(false)
    val isFiltering: StateFlow<Boolean> = _isFiltering.asStateFlow()

    val searchUiState: StateFlow<SearchUiState> = combine(
        postRepository.observePosts().catch { emit(emptyList()) },
        _searchQuery,
        _filterState
    ) { posts, query, filters ->
        val filtered = posts.filter { post ->
            matchesQuery(post, query) && matchesFilters(post, filters)
        }
        val state: SearchUiState = SearchUiState.Success(filtered)
        state
    }.catch { emit(SearchUiState.Error(it.message ?: "Search error")) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Loading
    )

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun executeSearch(query: String) {
        val trimmed = query.trim()
        _searchQuery.value = trimmed
    }

    fun clearQuery() {
        _searchQuery.value = ""
    }

    fun openFilterScreen() {
        _draftFilterState.value = _filterState.value
        _isFiltering.value = true
    }

    fun closeFilterScreen() {
        _isFiltering.value = false
    }

    fun applyDraftFilters() {
        _filterState.value = _draftFilterState.value
        _isFiltering.value = false
    }

    fun resetDraftFilters() {
        _draftFilterState.value = SearchFilterState()
    }

    fun updateDraftMinPrice(price: Double?) {
        _draftFilterState.value = _draftFilterState.value.copy(minPrice = price)
    }

    fun updateDraftMaxPrice(price: Double?) {
        _draftFilterState.value = _draftFilterState.value.copy(maxPrice = price)
    }

    fun toggleDraftBedroom(bedroom: Int) {
        val current = _draftFilterState.value.selectedBedrooms.toMutableSet()
        if (current.contains(bedroom)) {
            current.remove(bedroom)
        } else {
            current.add(bedroom)
        }
        _draftFilterState.value = _draftFilterState.value.copy(selectedBedrooms = current)
    }

    fun updateDraftMoveInDates(fromMs: Long?, toMs: Long?) {
        _draftFilterState.value = _draftFilterState.value.copy(
            moveInFromMs = fromMs,
            moveInToMs = toMs
        )
    }

    fun updateDraftPropertyType(type: String) {
        _draftFilterState.value = _draftFilterState.value.copy(propertyType = type)
    }

    fun updateDraftFurnishedStatus(status: String) {
        _draftFilterState.value = _draftFilterState.value.copy(furnishedStatus = status)
    }

    fun updateDraftGenderPreference(gender: String) {
        _draftFilterState.value = _draftFilterState.value.copy(genderPreference = gender)
    }

    private fun matchesQuery(post: Post, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.lowercase().trim()
        return post.title.lowercase().contains(q) ||
               post.propertyName.lowercase().contains(q) ||
               post.propertyType.lowercase().contains(q) ||
               post.location.lowercase().contains(q)
    }

    private fun matchesFilters(post: Post, filters: SearchFilterState): Boolean {
        val postPrice = if (post.priceRM > 0) post.priceRM else post.rentPerPerson
        if (filters.minPrice != null && postPrice < filters.minPrice) return false
        if (filters.maxPrice != null && postPrice > filters.maxPrice) return false

        if (filters.selectedBedrooms.isNotEmpty()) {
            val bedrooms = post.bedrooms.toInt()
            val matches = filters.selectedBedrooms.any { sel ->
                if (sel >= 5) bedrooms >= 5 else bedrooms == sel
            }
            if (!matches) return false
        }

        if (filters.moveInFromMs != null || filters.moveInToMs != null) {
            if (post.moveInDate > 0) {
                if (filters.moveInFromMs != null && post.moveInDate < filters.moveInFromMs) return false
                if (filters.moveInToMs != null && post.moveInDate > filters.moveInToMs) return false
            }
        }

        if (filters.propertyType.isNotBlank() && !post.propertyType.equals(filters.propertyType, ignoreCase = true)) {
            return false
        }

        if (filters.furnishedStatus.isNotBlank() && !post.furnishedStatus.equals(filters.furnishedStatus, ignoreCase = true)) {
            return false
        }

        if (filters.genderPreference.isNotBlank() && !post.genderPreference.equals(filters.genderPreference, ignoreCase = true)) {
            return false
        }

        return true
    }
}

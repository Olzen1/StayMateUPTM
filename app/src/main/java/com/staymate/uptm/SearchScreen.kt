@file:Suppress("DEPRECATION")

package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.viewmodel.SaveUiState
import com.staymate.uptm.viewmodel.SaveViewModel
import com.staymate.uptm.viewmodel.SaveViewModelFactory
import com.staymate.uptm.viewmodel.SearchUiState
import com.staymate.uptm.viewmodel.SearchViewModel
import com.staymate.uptm.viewmodel.SearchViewModelFactory

@Composable
fun SearchScreen(
    searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(PostRepository())
    ),
    saveViewModel: SaveViewModel = viewModel(
        factory = SaveViewModelFactory(SaveRepository(), AuthRepository())
    ),
    onPostClick: (String) -> Unit = {}
) {
    val isFiltering by searchViewModel.isFiltering.collectAsStateWithLifecycle()

    if (isFiltering) {
        FilterScreen(
            viewModel = searchViewModel,
            onBack = { searchViewModel.closeFilterScreen() },
            onApply = { searchViewModel.applyDraftFilters() }
        )
    } else {
        val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
        val filterState by searchViewModel.filterState.collectAsStateWithLifecycle()
        val searchUiState by searchViewModel.searchUiState.collectAsStateWithLifecycle()

        val saveUiState by saveViewModel.saveUiState.collectAsStateWithLifecycle()
        val savedIds = (saveUiState as? SaveUiState.Success)?.savedIds ?: emptySet()

        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header title "SEARCH"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SEARCH",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Search Bar + Filter Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search TextField
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchViewModel.onQueryChange(it) },
                    placeholder = { Text("Search...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchViewModel.clearQuery() }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.Gray
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            searchViewModel.executeSearch(searchQuery)
                            focusManager.clearFocus()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                // Filter Button
                OutlinedButton(
                    onClick = { searchViewModel.openFilterScreen() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(56.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (filterState.isActive) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
                        )
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (filterState.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                    )
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = "Filter",
                        tint = if (filterState.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Filter",
                        fontWeight = FontWeight.Bold,
                        color = if (filterState.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Section Header: "Recommended Posts" when empty search/filter, else "Results"
            val sectionTitle = if (searchQuery.isBlank() && !filterState.isActive) "Posts" else "Results"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = sectionTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Results List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = searchUiState) {
                    is SearchUiState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is SearchUiState.Error -> {
                        Text(
                            text = "Could not load posts: ${state.message}",
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }

                    is SearchUiState.Success -> {
                        val posts = state.posts
                        if (posts.isEmpty()) {
                            Text(
                                text = "No posts found matching your search.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 70.dp)
                            ) {
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
        }
    }
}

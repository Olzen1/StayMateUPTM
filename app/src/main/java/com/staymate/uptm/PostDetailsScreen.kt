package com.staymate.uptm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.viewmodel.PostDetailsUiState
import com.staymate.uptm.viewmodel.PostDetailsViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.PostDetailsViewModelFactory

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

// stub details screen: proves the by-id read works; Step 4 replaces the body with the wireframe layout
@Composable
fun PostDetailsScreen(
    postId: String, // // the id MainScreen will hand us in 3b
    onBack: () -> Unit, // // the exit action MainScreen will wire in 3b (stub ignores it for now)
    detailsViewModel: PostDetailsViewModel = viewModel( // // same acquisition style as HomeScreen's feed VM
        factory = PostDetailsViewModelFactory(PostRepository()) // // ONE repo only: details never reads auth
    )
) {
        // the doorbell: rings every time this screen appears with an id -> re-plugs the doc listener
        // (this replaces the feed's uid-helmet; details can't be left running across a logout, so
        //  "screen appeared" is the correct re-plug trigger, not "uid changed")
    LaunchedEffect(postId) {
        detailsViewModel.select(postId)
    }

    // read the engine's current state, same muscle as the feed's collectAsStateWithLifecycle
    val uiState by detailsViewModel.postDetailsUiState.collectAsStateWithLifecycle()

    // short-term memory for the three-dot menu
    var showMenu by remember { mutableStateOf(false) }

// main screen shell: top bar, content, bottom buttons
    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .background(MaterialTheme.colorScheme.background)) {

        // top bar with real back button and stub menu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "back",
                    tint = (MaterialTheme.colorScheme.onSurface))
            }

            Text(
                text = "Post Details",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            // three-dot menu using text, not an icon, to avoid the icon-pack trap
            Box(
                modifier = Modifier
                    .clickable { showMenu = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⋮",
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // the four honest states from the ViewModel
        when (val state = uiState) {

            is PostDetailsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading post…", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }

            is PostDetailsUiState.NotFound -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("This post no longer exists.", color = Color.Gray, fontSize = 16.sp)
                }
            }

            is PostDetailsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Could not load: ${state.message}", color = Color.Red, fontSize = 14.sp)
                }
            }

            is PostDetailsUiState.Success -> {
                val post = state.post

                // scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                ) {

                    // use the first photo if it exists
                    val photoUrl = post.photoUrls.firstOrNull()

                    // photo area with rounded corners and gray empty state
                    //Box(
                    //                        modifier = Modifier
                    //                            .fillMaxWidth()
                    //                            .height(220.dp)
                    //                            .clip(RoundedCornerShape(16.dp))
                    //                            .background(Color.LightGray),
                    //                        contentAlignment = Alignment.Center
                    //                    ) {
                    //                        if (photoUrl != null) {
                    //                            AsyncImage(
                    //                                model = photoUrl,
                    //                                contentDescription = "Post photo",
                    //                                contentScale = ContentScale.Crop,
                    //                                modifier = Modifier.matchParentSize()
                    //                            )
                    //                        } else {
                    //                            Text("No photo yet", color = Color.Gray)
                    //                        }
                    //                    }

                    // main post facts
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // choose pill color and label from the stored snake_case type key
                        val isHouseSuggestion = post.type == UptmConstants.POST_TYPE_KEY_SUGGESTION

                        val pillColor = if (isHouseSuggestion) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }

                        val pillLabel = if (isHouseSuggestion) {
                            UptmConstants.POST_TYPE_HOUSE_SUGGESTION
                        } else {
                            UptmConstants.POST_TYPE_HOUSEMATE_WANTED
                        }

                        // small colored pill like a sticker
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(percent = 50)
                        ) {
                            Text(
                                text = pillLabel,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = post.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold ,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (post.location.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("📍")
                                Text(post.location, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "RM ${post.priceRM}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground

                            )

                            if (post.bedrooms > 0) {
                                Text(
                                    text = "${post.bedrooms} bedroom(s)",
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        if (post.propertyType.isNotBlank()) {
                            Text(
                                text = "Property type: ${post.propertyType}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "Facilities",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground

                        )

                        if (post.facilities.isEmpty()) {
                            Text("No facilities listed", color = Color.Gray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                post.facilities.forEach { facility ->
                                    Text("• $facility", color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }

                        Text(
                            text = "About Us",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (post.description.isNotBlank()) {
                            Text(post.description, color = MaterialTheme.colorScheme.onBackground)
                        } else {
                            Text("No description provided.", color = MaterialTheme.colorScheme.onBackground)
                        }

                        if (post.genderPreference.isNotBlank()) {
                            Text(
                                text = "Gender preference: ${post.genderPreference}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (post.propertyLink.isNotBlank()) {
                            Text(
                                text = "Link: ${post.propertyLink}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // bottom stub actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Message")
                    }

                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)


                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

// stub pop-up menu for the three dots
    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text("Post options") },
            text = { Text("Report post is a stub for now.") },
            confirmButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text("Close")
                }
            }
        )
    }
}

package com.staymate.uptm

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.staymate.uptm.model.Post
import com.staymate.uptm.repository.AuthRepository
import com.staymate.uptm.repository.PostRepository
import com.staymate.uptm.repository.SaveRepository
import com.staymate.uptm.utils.UptmConstants
import com.staymate.uptm.viewmodel.PostDetailsUiState
import com.staymate.uptm.viewmodel.PostDetailsViewModel
import com.staymate.uptm.viewmodel.PostDetailsViewModelFactory
import com.staymate.uptm.viewmodel.SaveUiState
import com.staymate.uptm.viewmodel.SaveViewModel
import com.staymate.uptm.viewmodel.SaveViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun PostDetailsScreen(
    postId: String,
    onBack: () -> Unit,
    onEditClick: ((Post) -> Unit)? = null,
    detailsViewModel: PostDetailsViewModel = viewModel(
        factory = PostDetailsViewModelFactory(PostRepository())
    ),
    saveViewModel: SaveViewModel = viewModel(
        factory = SaveViewModelFactory(SaveRepository(), AuthRepository())
    )
) {
    LaunchedEffect(postId) {
        detailsViewModel.select(postId)
    }

    val uiState by detailsViewModel.postDetailsUiState.collectAsStateWithLifecycle()
    val saveUiState by saveViewModel.saveUiState.collectAsStateWithLifecycle()
    val savedIds = (saveUiState as? SaveUiState.Success)?.savedIds ?: emptySet()

    var showMenu by remember { mutableStateOf(false) }
    var showContactDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val currentUid = AuthRepository().currentUid()
    val currentPost = (uiState as? PostDetailsUiState.Success)?.post
    val isOwner = currentPost != null && currentPost.authorUid == currentUid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Post Details",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Box(
                modifier = Modifier.padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⋮",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { showMenu = true }
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isOwner) {
                        DropdownMenuItem(
                            text = { Text("Delete Post", color = Color.Red, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Report", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = "Report", tint = Color(0xFFFF9800)) },
                            onClick = {
                                showMenu = false
                                showReportDialog = true
                            }
                        )
                    }
                }
            }
        }

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

                if (showContactDialog) {
                    ContactInfoDialog(
                        post = post,
                        onDismiss = { showContactDialog = false }
                    )
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Post", fontWeight = FontWeight.Bold) },
                        text = { Text("Are you sure you want to delete this post? This action cannot be undone.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showDeleteDialog = false
                                    coroutineScope.launch {
                                        if (post.id.isNotBlank()) {
                                            val result = PostRepository().deletePost(post.id)
                                            result.onSuccess {
                                                onBack()
                                            }.onFailure { e ->
                                                android.util.Log.e("StayMateDelete", "Failed to delete post: ${e.message}", e)
                                                onBack()
                                            }
                                        } else {
                                            onBack()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Delete", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showReportDialog) {
                    AlertDialog(
                        onDismissRequest = { showReportDialog = false },
                        title = { Text("Are you sure to report this post?", fontWeight = FontWeight.Bold) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showReportDialog = false
                                    coroutineScope.launch {
                                        PostRepository().reportPost(post.id, post.title, currentUid ?: "")
                                        android.widget.Toast.makeText(context, "Post reported successfully.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("Yes", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showReportDialog = false }) {
                                Text("No")
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.background)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isHouseSuggestion = post.type == UptmConstants.POST_TYPE_KEY_SUGGESTION
                        val isGroupFinding = post.type == UptmConstants.POST_TYPE_KEY_GROUP_FINDING
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (post.authorPhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = post.authorPhotoUrl,
                                        contentDescription = "Author photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Poster avatar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = post.authorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = relativeTime(post.createdAt),
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = when (post.type) {
                                    "house_suggestion" -> MaterialTheme.colorScheme.primaryContainer
                                    "group_finding" -> MaterialTheme.colorScheme.tertiaryContainer
                                    else -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val labelText = when (post.type) {
                                    "house_suggestion" -> "House Suggestion"
                                    "group_finding" -> "Finding a Group"
                                    else -> "Housemate Wanted"
                                }
                                Text(
                                    text = labelText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = post.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (post.propertyName.isNotBlank()) {
                            Text(
                                text = post.propertyName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

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
                            val priceVal = if (post.rentPerPerson > 0) post.rentPerPerson else post.priceRM
                            val priceDisplay = if (post.priceRM > 0 && post.rentPerPerson > 0 && post.type == "group_finding") {
                                "RM${post.priceRM.toInt()} - RM${post.rentPerPerson.toInt()}"
                            } else {
                                "RM ${priceVal.toInt()}"
                            }
                            Text(
                                text = priceDisplay,
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

                        // Money & Room Info: only meaningful for Housemate Wanted posts
                        // (group posts show a price RANGE up top; suggestions carry no money info)
                        if (!isHouseSuggestion && !isGroupFinding) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Money & Room Info",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                if (post.rentPerPerson > 0) {
                                    Text(
                                        text = "Rent per person: RM ${post.rentPerPerson.toInt()}",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                if (post.deposit > 0) {
                                    Text(
                                        text = "Deposit: RM ${post.deposit.toInt()}",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                if (post.currentHousemates > 0) {
                                    Text(
                                        text = "Current housemates: ${post.currentHousemates}",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        if (post.moveInDate > 0) {
                            Text(
                                text = "Move-in Date: ${formatMoveInDate(post.moveInDate)}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (post.furnishedStatus.isNotBlank()) {
                            Text(
                                text = "Furnished: ${post.furnishedStatus}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (post.propertyType.isNotBlank()) {
                            Text(
                                text = "Property type: ${post.propertyType}",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Facilities: hidden for Finding-a-Group posts (they never pick facilities)
                        if (!isGroupFinding) {
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
                        }

                        Text(
                            text = "    About Us / Information",
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

                // Bottom actions: Contact & Save/Edit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showContactDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Contact")
                    }

                    if (isOwner) {
                        OutlinedButton(
                            onClick = { onEditClick?.invoke(post) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { saveViewModel.toggleSave(post.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (savedIds.contains(post.id)) "Saved" else "Save")
                        }
                    }
                }
            }
        }
    }

    BackHandler {
        onBack()
    }
}

@Composable
fun ContactInfoDialog(
    post: Post,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with photo and info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile Photo / Avatar
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (post.authorPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = post.authorPhotoUrl,
                                contentDescription = "Author Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    // Contact Details
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = post.authorName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Email: ${post.contactEmail.ifBlank { "Not provided" }}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Text(
                            text = "No phone: ${post.contactPhone.ifBlank { "Not provided" }}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Text(
                            text = "Gender: ${post.contactGender.ifBlank { post.genderPreference.ifBlank { "Not specified" } }}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        if (post.contactOtherInfo.isNotBlank()) {
                            Text(
                                text = "Other Information: ${post.contactOtherInfo}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Green WhatsApp Button
                val rawNum = post.whatsappNumber.ifBlank { post.contactPhone }
                val whatsappNum = rawNum.replace(Regex("[^0-9]"), "")

                Button(
                    onClick = {
                        val formattedNum = if (whatsappNum.startsWith("60") || whatsappNum.startsWith("6")) {
                            whatsappNum
                        } else {
                            "60$whatsappNum"
                        }
                        val url = if (whatsappNum.isNotBlank()) "https://wa.me/$formattedNum" else "https://wa.me/"
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF70E000),
                        contentColor = Color.Black
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Whatsapp",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

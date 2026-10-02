package com.staymate.uptm

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KingBed
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.vector.ImageVector
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

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (isOwner || onEditClick != null) {
                        DropdownMenuItem(
                            text = { Text("Delete Post", color = Color.Red, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                    if (!isOwner) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("This post no longer exists.", color = Color.Gray, fontSize = 16.sp)
                    }
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.width(20.dp))
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
                    }
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val isHouseSuggestion = post.type == UptmConstants.POST_TYPE_KEY_SUGGESTION
                        val isGroupFinding = post.type == UptmConstants.POST_TYPE_KEY_GROUP_FINDING

                        //HERO CARD: type badge + title + location
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // type badge chip
                                Surface(
                                    color = when (post.type) {
                                        "house_suggestion" -> MaterialTheme.colorScheme.primaryContainer
                                        "group_finding" -> MaterialTheme.colorScheme.tertiaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    },
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = when (post.type) {
                                            "house_suggestion" -> "House Suggestion"
                                            "group_finding" -> "Finding a Group"
                                            else -> "Housemate Wanted"
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = post.title,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 28.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                if (post.propertyName.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = post.propertyName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                if (post.location.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(
                                                if (!isGroupFinding) Modifier.clickable {
                                                    openInGoogleMaps(context, post.location)
                                                } else Modifier
                                            )
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFFED1C24),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = post.location,
                                            fontSize = 14.sp,
                                            color = if (!isGroupFinding) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // KEY FACTS CARD
                        KeyFactsCard(post = post)

                        // MONEY & ROOM INFO (Housemate Wanted only)
                        if (!isHouseSuggestion && !isGroupFinding) {
                            MoneyInfoCard(post = post)
                        }

                        //  FACILITIES (hidden for Finding-a-Group posts)
                        if (!isGroupFinding) {
                            FacilitiesCard(post = post)
                        }

                        // ABOUT / DESCRIPTION CARD
                        AboutCard(
                            post = post,
                            isHouseSuggestion = isHouseSuggestion,
                            isGroupFinding = isGroupFinding
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // STICKY BOTTOM ACTIONS: Contact & Save/Edit
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showContactDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact", fontWeight = FontWeight.Bold)
                        }

                        if (isOwner || onEditClick != null) {
                            OutlinedButton(
                                onClick = { onEditClick?.invoke(post) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Edit", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { saveViewModel.toggleSave(post.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    if (savedIds.contains(post.id)) "Saved" else "Save",
                                    fontWeight = FontWeight.Bold
                                )
                            }
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

// key facts tiles: price / bedrooms / move-in / furnished / property type
@Composable
private fun KeyFactsCard(post: Post) {
    val priceDisplay = "RM${post.priceRM.toInt()}"
    val hasBedrooms = post.bedrooms > 0
    val hasMoveIn = post.moveInDate > 0
    val hasFurnished = post.furnishedStatus.isNotBlank()
    val hasPropertyType = post.propertyType.isNotBlank()

    val hasAnyFacts = hasBedrooms || hasMoveIn || hasFurnished || hasPropertyType
    if (!hasAnyFacts) return

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // price strip: the number everyone scans for, in the accent color
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Payments,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$priceDisplay / monthly",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )

            // 2-up tiles: bedrooms + move-in
            if (hasBedrooms || hasMoveIn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasBedrooms) {
                        FactTile(
                            icon = Icons.Default.KingBed,
                            value = "${post.bedrooms}",
                            label = "bedroom(s)",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (hasMoveIn) {
                        FactTile(
                            icon = Icons.Default.CalendarMonth,
                            value = formatMoveInDate(post.moveInDate),
                            label = "Move-in date",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2-up tiles: furnished + property type
            if (hasFurnished || hasPropertyType) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (hasFurnished) {
                        FactTile(
                            icon = Icons.Default.Home,
                            value = post.furnishedStatus,
                            label = "Furnished",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (hasPropertyType) {
                        FactTile(
                            icon = Icons.Default.LocationOn,
                            value = post.propertyType,
                            label = "Property type",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FactTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============ money tiles: deposit / current housemates ============
@Composable
private fun MoneyInfoCard(post: Post) {
    val hasDeposit = post.deposit > 0
    val hasHousemates = post.currentHousemates > 0
    if (!hasDeposit && !hasHousemates) return

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Current Housemate",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (hasDeposit) {
                    FactTile(
                        icon = Icons.Default.Savings,
                        value = "RM ${post.deposit.toInt()}",
                        label = "Deposit",
                        modifier = Modifier.weight(1f)
                    )
                }
                if (hasHousemates) {
                    FactTile(
                        icon = Icons.Default.Person,
                        value = "${post.currentHousemates}",
                        label = "Current housemates",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


// ============ facilities as little check-chips ============
@Composable
private fun FacilitiesCard(post: Post) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Facilities",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (post.facilities.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("No facilities listed", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.facilities.forEach { facility ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = facility,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============ about: description + gender preference + property link ============
@Composable
private fun AboutCard(
    post: Post,
    isHouseSuggestion: Boolean,
    isGroupFinding: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isGroupFinding) "About the Group" else if (isHouseSuggestion) "Information" else "About Us",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (post.description.isNotBlank()) {
                Text(
                    text = post.description,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text("No description provided.", color = Color.Gray, fontSize = 14.sp)
            }

            if (post.genderPreference.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Gender preference: ${post.genderPreference}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (post.propertyLink.isNotBlank()) {
                val context = LocalContext.current
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            var url = post.propertyLink.trim()
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "https://$url"
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                // Fallback
                            }
                        }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = post.propertyLink,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
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

// ============ opens the post location inside Google Maps (Maps app first, browser fallback) ============
private fun openInGoogleMaps(context: Context, location: String) {
    val query = java.net.URLEncoder.encode(location, "UTF-8")
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "geo:0,0?q=$query".toUri())
                .setPackage("com.google.android.apps.maps")
        )
    } catch (_: Exception) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/search/?api=1&query=$query".toUri())
            )
        } catch (_: Exception) {
            // no maps app and no browser — quietly ignore
        }
    }
}
package com.staymate.uptm

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.staymate.uptm.viewmodel.ProfileSaveState
import com.staymate.uptm.viewmodel.ProfileUiState
import com.staymate.uptm.viewmodel.ProfileViewModel
import com.staymate.uptm.viewmodel.RootViewModel

@Composable
fun ProfileScreen(
    onNavigateToSavedPosts: () -> Unit = {},
    onNavigateToEditPosts: () -> Unit = {},
    onNavigateToEditFindingGroup: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel(),
    rootViewModel: RootViewModel = viewModel() // Shared instance from RootScreen
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Extract data from Firestore via ViewModel (Single Source of Truth!)
    val profile = (uiState as? ProfileUiState.Success)?.profile
    val userName = profile?.fullName ?: "Student"
    val userCourse = profile?.course ?: ""
    val userSemester = profile?.semester ?: ""
// function read the save button's live memory
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
// function is the write running right now? (used to lock the Save button)
    val isSaving = saveState == ProfileSaveState.Saving
// function pull the error words out, or null if there is no error
    val saveError = (saveState as? ProfileSaveState.Error)?.message
    // ... keep the profileImageUri and photoPicker code exactly as it is ...

    // PROFILE PHOTO — default icon until the user picks one

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- TOP: blue header, PROFILE title, NO gear icon ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primary,
                            colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "PROFILE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = colorScheme.primaryContainer,
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-50).dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- PROFILE PHOTO (tap to change) ----------
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(4.dp)

            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    ProfileAvatar(
                        photoUrl = profile?.photoUrl,
                        fullName = userName,
                        modifier = Modifier
                            .size(120.dp)  // Back to the original big size!
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // NAME
            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // COURSE & SEMESTER
            val details = listOf(userCourse, userSemester)
                .filter { it.isNotBlank() }
                .joinToString("\n")
            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details,
                    fontSize = 14.sp,
                    color = colorScheme.onSurface
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "course & semester",
                    fontSize = 13.sp,
                    color = Color(0xFF0091FF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showEditDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

    val postCount by viewModel.userPostCount.collectAsStateWithLifecycle()
    val savedPostCount by viewModel.savedPostCount.collectAsStateWithLifecycle()

    // STATS: Posts / Saved Post
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surface)
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(postCount.toString(), "Posts", colorScheme.onSurface)
        StatItem(savedPostCount.toString(), "Saved Post", colorScheme.onSurface)
    }

            Spacer(modifier = Modifier.height(20.dp))

            // ----------    divider line between top and middle section
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(18.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                //MenuItem(Icons.Default.Edit, "Edit Profile") { showEditDialog = true }
                //                HorizontalDivider(
                //                    modifier = Modifier.padding(horizontal = 16.dp),
                //                    color = colorScheme.tertiary
                //                )
                MenuItem(Icons.Default.BookmarkBorder, "Saved Post") { onNavigateToSavedPosts() }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorScheme.tertiary
                )
                MenuItem(Icons.AutoMirrored.Filled.ListAlt, "Edit Posts") { onNavigateToEditPosts() }
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = colorScheme.tertiary
                )
                MenuItem(Icons.Default.Groups, "Edit Finding a Group Post") { onNavigateToEditFindingGroup() }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- LOG OUT ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = colorScheme.surface)
                    .clickable { showLogoutDialog = true }
                    .padding(vertical = 16.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFFF1744), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF1744))
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
// function when the write truly lands, close the sheet and reset the save memory
    LaunchedEffect(saveState) {
        if (saveState == ProfileSaveState.Success) {
            showEditDialog = false // function leave the sheet
            viewModel.resetSave() // function back to Idle so it cannot re-fire next open
        }
    }
    // ---------- EDIT PROFILE (UI only — no saving to database yet) ----------
    if (showEditDialog) {
        EditProfileDialog(
            onDismiss = { showEditDialog = false },
            onSave = { name, course, semester ->
                // function hand the typed values to the ViewModel; do NOT close here (close only on real Success, so errors keep the sheet open)
                viewModel.saveProfile(name, course, semester)
            },
            isSaving = isSaving, // function tells the sheet to lock + relabel its Save button
            errorMessage = saveError, // function tells the sheet to show red words if the write bounced
            currentName = userName,
            currentCourse = userCourse,
            currentSemester = userSemester
        )
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = {
                showLogoutDialog = false // function close the sheet
                viewModel.resetSave() // function wipe any old red error so the next open starts clean

            },
            onConfirm = {
                rootViewModel.logout() // Magic! Auth listener flips screen to Login
                showLogoutDialog = false
            }
        )
            }

    }





@Composable
private fun StatItem(number: String, label: String, surface: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(number, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = colorScheme.onSurface)
    }
}

@Composable
private fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Text(text, fontSize = 15.sp, color = colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}
// Profile avatar with Google photo and initials fallback
// Profile avatar with Google photo and initials fallback
// Profile avatar with Google photo and initials fallback
@Composable
fun ProfileAvatar(
    photoUrl: String?,
    fullName: String,
    modifier: Modifier = Modifier
) {
    // 1. Calculate the initials
    val initials = fullName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    // Check if we actually have a Google photo link
    if (photoUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        // MAIN PLAN: We have a link, use Coil to load the Google photo!
        AsyncImage(
            model = photoUrl,
            contentDescription = "Profile picture",
            modifier = modifier
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
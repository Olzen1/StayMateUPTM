package com.staymate.uptm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    // NAME — comes from the logged-in account, never hardcoded
    var userName by remember { mutableStateOf(resolveDisplayName(firebaseUser)) }

    // COURSE & SEMESTER — empty right after login, filled via Edit Profile (UI only for now)
    var userCourse by remember { mutableStateOf("") }
    var userSemester by remember { mutableStateOf("") }

    // PROFILE PHOTO — default icon until the user picks one
    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        profileImageUri = uri
    }

    // Whenever a new photo is picked, decode it into a small bitmap we can display
    LaunchedEffect(profileImageUri) {
        profileBitmap = profileImageUri?.let { decodeUriToImageBitmap(context, it, 224) }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
    ) {
        // ---------- TOP: blue header, PROFILE title, NO gear icon ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFF0091FF)),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "PROFILE",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                color = Color.White,
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
                    .clickable {
                        photoPicker.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Default picture when the user hasn't added one
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Default profile photo",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Small camera badge (signals "tap me")
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0091FF))
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Camera,
                            contentDescription = "Add photo",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- NAME (bold, auto from login) ----------
            Text(
                text = userName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )

            // ---------- COURSE & SEMESTER (nothing until the user adds them) ----------
            val details = listOf(userCourse, userSemester)
                .filter { it.isNotBlank() }
                .joinToString(", ")
            if (details.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = details,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.clickable { showEditDialog = true }
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap here to add your course & semester",
                    fontSize = 13.sp,
                    color = Color(0xFF0091FF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showEditDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- STATS: Posts / Saved Houses / Groups (placeholder numbers) ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("12", "Posts")
                StatItem("8", "Saved Houses")
                StatItem("5", "Groups")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- divider line between top and middle section ----------
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Color(0xFFE5E7EB))

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- MENU: Edit Profile / Settings / About ----------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(vertical = 8.dp)
            ) {
                MenuItem(Icons.Default.Edit, "Edit Profile") { showEditDialog = true }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                MenuItem(Icons.Default.Settings, "Settings") { /* TODO (later phase) */ }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF3F4F6))
                MenuItem(Icons.Default.Info, "About StayMate UPTM") { /* TODO (later phase) */ }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- LOG OUT ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
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

    // ---------- EDIT PROFILE (UI only — no saving to database yet) ----------
    if (showEditDialog) {
        EditProfileDialog(
            onDismiss = { showEditDialog = false },
            onSave = { name, course, semester ->
                userName = name
                userCourse = course
                userSemester = semester
                showEditDialog = false
            },
            currentName = userName,
            currentCourse = userCourse,
            currentSemester = userSemester
        )
    }

    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                FirebaseAuth.getInstance().signOut()
                (context as? Activity)?.let { activity ->
                    val intent = Intent(activity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    activity.startActivity(intent)
                    activity.finish()
                }
                showLogoutDialog = false
            }
        )
    }
}

// ---------- helpers ----------

/**
 * Decides what name to show. Priority:
 * 1. The name stored in the Firebase account (empty for email/password logins by default)
 * 2. A friendly name built from the email: "ilman.aidil@uptm.edu.my" -> "Ilman Aidil"
 */
private fun resolveDisplayName(user: FirebaseUser?): String {
    user?.displayName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }

    val emailName = user?.email
        ?.substringBefore("@")
        ?.replace('.', ' ')
        ?.replace('_', ' ')
        ?.replace('-', ' ')
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return "Student"

    return emailName.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}

/**
 * Turns a picked photo (Uri) into a Compose ImageBitmap, shrunk to roughly
 * targetPx so we don't load a giant 12-megapixel photo into memory for a 112dp circle.
 */
private fun decodeUriToImageBitmap(context: Context, uri: Uri, targetPx: Int): ImageBitmap? {
    return try {
        // Pass 1: read only the image's SIZE (no pixels loaded)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        // Work out how much to shrink (each step halves the size)
        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= targetPx &&
            bounds.outHeight / (sampleSize * 2) >= targetPx
        ) {
            sampleSize *= 2
        }

        // Pass 2: load the actual pixels, already shrunk
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }?.asImageBitmap()
    } catch (e: Exception) {
        null // anything goes wrong -> we just show the default icon
    }
}

@Composable
private fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(number, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
        Spacer(modifier = Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = Color(0xFF9CA3AF))
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
        Text(text, fontSize = 15.sp, color = Color(0xFF1A1A2E), fontWeight = FontWeight.Medium)
    }
}
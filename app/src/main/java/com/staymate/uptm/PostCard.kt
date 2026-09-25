package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.firebase.Timestamp
import com.staymate.uptm.model.Post
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme

@Composable
// was:  fun PostCard(post: Post) {
fun PostCard(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick), // // tap anywhere on the card -> fire the handler
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
    ) {
        // inner padding so nothing is glued to the card edges
        Column(modifier = Modifier.padding(12.dp)) {

            // poster header: avatar + name + time, side by side
            Row(verticalAlignment = Alignment.CenterVertically) {
                // round avatar placeholder
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.LightGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Poster avatar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // name + time stack; weight(1f) + ellipsis = long names get "..." not overflow
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = relativeTime(post.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } // <-- lobby door CLOSED here (this is the line that was missing)

            Spacer(modifier = Modifier.height(12.dp))

            // type badge pill
            Surface(
                color = if (post.type == "house_suggestion") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(20.dp)
            ) {
                Text(
                    text = if (post.type == "house_suggestion") "House Suggestion" else "Housemate Wanted",
                    modifier = Modifier.padding(horizontal = 6.dp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // hero photo: real https image when present, gray box when not
            //Box(
            //                modifier = Modifier
            //                    .fillMaxWidth()
            //                    .height(180.dp)
            //                    .clip(RoundedCornerShape(12.dp))
            //                    .background(Color.LightGray),
            //                contentAlignment = Alignment.Center
            //            ) {
            //                val cover = post.photoUrls.firstOrNull()
            //                if (cover != null) {
            //                    AsyncImage(
            //                        model = cover,
            //                        contentDescription = "Post photo",
            //                        contentScale = ContentScale.Crop,
            //                        modifier = Modifier.matchParentSize()
            //                    )
            //                } else {
            //                    Icon(
            //                        imageVector = Icons.Default.Image,
            //                        contentDescription = "No photo yet",
            //                        tint = Color.DarkGray,
            //                        modifier = Modifier.size(48.dp)
            //                    )
            //                }
            //            }
            Spacer(modifier = Modifier.height(12.dp))

            // big title text
            Text(
                text = post.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // location line: pin + address (long ones get "...")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = post.location,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // price text (no green, your call)
            Text(
                text = "RM ${post.priceRM.toInt()}",
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // beds + property type line
            Text(
                text = "${post.bedrooms} Beds • ${post.propertyType}",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // footer: save button hugs the right edge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {

                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Save post",
                    tint = Color.Gray
                )
            }
        }
    }
}


// turns a Firestore Timestamp into "2h ago" words
fun relativeTime(timestamp: Timestamp): String {
    val minutesOld = (System.currentTimeMillis() - timestamp.toDate().time) / 60000
    return when {
        minutesOld < 1 -> "just now"
        minutesOld < 60 -> "${minutesOld}m ago"
        minutesOld < 1440 -> "${minutesOld / 60}h ago"
        else -> "${minutesOld / 1440}d ago"
    }
}
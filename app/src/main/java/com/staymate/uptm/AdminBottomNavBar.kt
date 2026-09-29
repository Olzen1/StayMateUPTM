package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class AdminNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val adminNavItems = listOf(
    AdminNavItem("Admin Dashboard", Icons.Default.Analytics, "admin_dashboard"),
    AdminNavItem("Posts", Icons.Default.Description, "admin_posts"),
    AdminNavItem("Reports", Icons.Default.Warning, "admin_reports"),
    AdminNavItem("Users", Icons.Default.People, "admin_users")
)

@Composable
fun AdminBottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            adminNavItems.forEach { item ->
                IconButton(
                    onClick = { onItemSelected(item.route) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (selectedRoute == item.route) {
                            Icon(
                                item.icon,
                                contentDescription = null,
                                tint = Color(0xFF0091FF).copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(26.dp)
                                    .blur(6.dp)
                            )
                        }

                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selectedRoute == item.route)
                                MaterialTheme.colorScheme.tertiary
                            else
                                Color(0xFF9CA3AF),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

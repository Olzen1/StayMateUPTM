package com.staymate.uptm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.offset
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val navItems = listOf(
    NavItem("Home", Icons.Default.Home, "home"),
    NavItem("Search", Icons.Default.Search, "search"),
    NavItem("Add", Icons.Default.Add, "add"),
    NavItem("Message", Icons.Default.Chat, "message"),
    NavItem("Profile", Icons.Default.Person, "profile")
)

@Composable
fun BottomNavBar(
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
                .padding(horizontal = 8.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                if (item.route == "add") {
                    Spacer(modifier = Modifier.width(56.dp))
                } else {
                    NavItemButton(
                        item = item,
                        isSelected = selectedRoute == item.route,
                        onClick = { onItemSelected(item.route) }
                    )
                }
            }
        }

        // + button pushed UP above the nav bar
        FloatingActionButton(
            onClick = { onItemSelected("add") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-26).dp)
                .size(52.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF0091FF).copy(alpha = 0.4f),
                    spotColor = Color(0xFF0091FF).copy(alpha = 0.4f)
                ),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun NavItemButton(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color(0xFF9CA3AF),
            modifier = Modifier.size(26.dp)
        )
    }
}
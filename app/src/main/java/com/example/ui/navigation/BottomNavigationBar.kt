package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.ui.theme.PharbMetallicSilver
import com.example.ui.theme.PharbRoyalBlue
import com.example.ui.theme.PharbVibrantBlue
import com.example.ui.viewmodel.MainBottomTab

data class BottomNavItem(
    val tab: MainBottomTab,
    val screen: AppScreen,
    val icon: ImageVector
)

val PharbBottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(MainBottomTab.HOME, AppScreen.Home, Icons.Filled.Home),
    BottomNavItem(MainBottomTab.DISCOVER, AppScreen.Discover, Icons.Filled.Explore),
    BottomNavItem(MainBottomTab.CREATE, AppScreen.Create, Icons.Filled.Add),
    BottomNavItem(MainBottomTab.MESSAGES, AppScreen.Messages, Icons.AutoMirrored.Filled.Chat),
    BottomNavItem(MainBottomTab.PROFILE, AppScreen.Profile, Icons.Filled.Person)
)

/**
 * Reusable BottomNavigationBar component using Material 3 `NavigationBar` and `NavigationBarItem`.
 * Allows users to switch seamlessly between Home, Explore (Discover), Create (elevated centerpiece), Messages, and Profile.
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    isArabic: Boolean,
    unreadMessagesCount: Int = 0,
    navController: NavHostController? = null,
    onSelectTab: (MainBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.testTag("pharb_bottom_nav")
    ) {
        PharbBottomNavItems.forEach { item ->
            val route = item.screen.route
            val isSelected = currentRoute == route
            val labelText = when (item.tab) {
                MainBottomTab.DISCOVER -> if (isArabic) "Explore • اكتشف" else "Explore"
                else -> if (isArabic) item.tab.labelAr else item.tab.labelEn
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    onSelectTab(item.tab)
                    navController?.navigateToBottomBarRoute(route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    when {
                        item.tab == MainBottomTab.CREATE -> {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(PharbRoyalBlue, PharbVibrantBlue)
                                        )
                                    )
                                    .border(
                                        width = 1.2.dp,
                                        color = PharbMetallicSilver.copy(alpha = 0.75f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = labelText,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        item.tab == MainBottomTab.MESSAGES && unreadMessagesCount > 0 -> {
                            BadgedBox(
                                badge = {
                                    Badge { Text(unreadMessagesCount.toString()) }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = labelText
                                )
                            }
                        }
                        else -> {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = labelText
                            )
                        }
                    }
                },
                label = {
                    Text(text = labelText)
                },
                modifier = Modifier.testTag("nav_${route}")
            )
        }
    }
}

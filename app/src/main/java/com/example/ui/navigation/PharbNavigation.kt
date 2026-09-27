package com.example.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.ui.viewmodel.MainBottomTab

/**
 * PHARB Official Navigation Routes (Home, Discover, Create, Messages, Profile)
 * Follows Jetpack Navigation Compose foundational architecture standards.
 */
object PharbRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_DISCOVER = "discover"
    const val ROUTE_CREATE = "create"
    const val ROUTE_MESSAGES = "messages"
    const val ROUTE_PROFILE = "profile"

    val primaryBottomRoutes: List<String> = listOf(
        ROUTE_HOME,
        ROUTE_DISCOVER,
        ROUTE_CREATE,
        ROUTE_MESSAGES,
        ROUTE_PROFILE
    )

    fun fromTab(tab: MainBottomTab): String = when (tab) {
        MainBottomTab.HOME -> ROUTE_HOME
        MainBottomTab.DISCOVER -> ROUTE_DISCOVER
        MainBottomTab.CREATE -> ROUTE_CREATE
        MainBottomTab.MESSAGES -> ROUTE_MESSAGES
        MainBottomTab.PROFILE -> ROUTE_PROFILE
    }

    fun toTab(route: String?): MainBottomTab = when (route) {
        ROUTE_DISCOVER -> MainBottomTab.DISCOVER
        ROUTE_CREATE -> MainBottomTab.CREATE
        ROUTE_MESSAGES -> MainBottomTab.MESSAGES
        ROUTE_PROFILE -> MainBottomTab.PROFILE
        else -> MainBottomTab.HOME
    }
}

fun NavHostController.navigateToBottomBarRoute(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Spa, Icons.Outlined.Spa),
    FOREST("forest", "Forest", Icons.Filled.Park, Icons.Outlined.Park),
    STATISTICS("statistics", "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SOCIAL("social", "Friends", Icons.Filled.People, Icons.Outlined.PeopleOutline),
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
}

object SubRoutes {
    const val FOCUS_ACTIVE = "focus_active"
    const val SHOP = "shop"
    const val TASKS = "tasks"
    const val SETTINGS = "settings"
    const val AI_PLANNER = "ai_planner"
    const val MINDFUL = "mindful"
    const val ONBOARDING = "onboarding"
}

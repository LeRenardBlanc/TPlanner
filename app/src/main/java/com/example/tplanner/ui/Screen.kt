package com.example.tplanner.ui

import androidx.annotation.StringRes
import com.example.tplanner.R

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Progress : Screen("progress")

    object WorkoutSession : Screen("workout/{day}") {
        fun createRoute(day: String) = "workout/$day"
    }

    // For the bottom navigation bar
    sealed class BottomNavItem(route: String, @StringRes val resourceId: Int, val icon: Int) : Screen(route) {
        object Home : BottomNavItem("home", R.string.home, R.drawable.ic_launcher_foreground)
        object Progress : BottomNavItem("progress", R.string.progress, R.drawable.ic_launcher_foreground)
    }
}
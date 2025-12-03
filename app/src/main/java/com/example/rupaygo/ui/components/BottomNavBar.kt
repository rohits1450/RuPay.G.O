package com.example.rupaygo.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send

import com.example.rupaygo.ui.navigation.Routes

@Composable
fun BottomNavBar(navController: NavController) {

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {

        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = { navController.navigate(Routes.HOME) },
            label = { Text("Home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.ISSUE,
            onClick = { navController.navigate(Routes.ISSUE) },
            label = { Text("Issue") },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Issue") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.TOKENS,
            onClick = { navController.navigate(Routes.TOKENS) },
            label = { Text("Tokens") },
            icon = { Icon(Icons.Default.List, contentDescription = "Tokens") }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.TRANSFER,
            onClick = { navController.navigate(Routes.TRANSFER) },
            label = { Text("Transfer") },
            icon = { Icon(Icons.Default.Send, contentDescription = "Transfer") }
        )
    }
}

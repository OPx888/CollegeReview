package com.example.collegereview

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.collegereview.authorisation.AuthViewModel
import com.example.collegereview.screens.CreatePage
import com.example.collegereview.screens.home.HomePage
import com.example.collegereview.screens.home.CollegeReviewsPage
import com.example.collegereview.screens.profile.YouPage

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    appViewModel: AppViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Add, contentDescription = "Create") },
                    label = { Text("Create") },
                    selected = currentDestination?.hierarchy?.any { it.route == "create" } == true,
                    onClick = {
                        navController.navigate("create") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "You") },
                    label = { Text("You") },
                    selected = currentDestination?.hierarchy?.any { it.route == "you" } == true,
                    onClick = {
                        navController.navigate("you") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomePage(
                    authViewModel = authViewModel,
                    appViewModel = appViewModel,
                    onSignOut = onSignOut,
                    onCollegeClick = { collegeName ->
                        navController.navigate("college_reviews/$collegeName")
                    }
                )
            }
            composable("create") {
                CreatePage(appViewModel = appViewModel)
            }
            composable("you") {
                YouPage(
                    authViewModel = authViewModel,
                    appViewModel = appViewModel,
                    onSignOut = onSignOut
                )
            }
            composable(
                route = "college_reviews/{collegeName}",
                arguments = listOf(navArgument("collegeName") { type = NavType.StringType })
            ) { backStackEntry ->
                val collegeName = backStackEntry.arguments?.getString("collegeName") ?: ""
                CollegeReviewsPage(
                    collegeName = collegeName,
                    appViewModel = appViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

package com.example.collegereview.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.collegereview.AppViewModel
import com.example.collegereview.MainScreen
import com.example.collegereview.authorisation.AuthViewModel
import com.example.collegereview.authorisation.LoginPage
import com.example.collegereview.authorisation.SignUpPage

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    appViewModel: AppViewModel,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") {
            LoginPage(
                viewModel = authViewModel,
                onNavigateToSignUp = {
                    navController.navigate("signup")
                }
            )
        }
        composable("signup") {
            SignUpPage(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("home") {
            MainScreen(
                authViewModel = authViewModel,
                appViewModel = appViewModel,
                onSignOut = {
                    // When signing out, pop back to login and clear back stack so user can't go back to home
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

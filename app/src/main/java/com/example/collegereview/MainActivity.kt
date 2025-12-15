package com.example.collegereview


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.collegereview.authorisation.AuthState
import com.example.collegereview.authorisation.AuthViewModel
import com.example.collegereview.navigation.AppNavigation
import com.example.collegereview.ui.theme.CollegeReviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val authViewModel: AuthViewModel by viewModels()
        val appViewModel: AppViewModel by viewModels()

        setContent {
            CollegeReviewTheme {
                val navController = rememberNavController()
                val authState by authViewModel.authState.collectAsState()
                val currentUser by authViewModel.currentUser.collectAsState()

                // Determine start destination based on authentication state
                val startDestination = if (currentUser != null) "home" else "login"

                LaunchedEffect(authState) {
                    if (authState is AuthState.Success) {
                         // Navigate to home and clear back stack to prevent going back to login
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        authViewModel = authViewModel,
                        appViewModel = appViewModel,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

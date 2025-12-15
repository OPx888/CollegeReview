package com.example.collegereview.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collegereview.authorisation.AuthViewModel
import com.example.collegereview.AppViewModel
import com.example.collegereview.authorisation.AuthState
import com.google.firebase.auth.EmailAuthProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouPage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    appViewModel: AppViewModel? = null,
    onSignOut: () -> Unit
) {
    val user by authViewModel.currentUser.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    
    // Observe user profile from AppViewModel if available
    val userProfile by appViewModel?.userProfile?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(emptyMap()) }
    
    var name by remember(userProfile) { mutableStateOf(userProfile["name"] ?: "") }
    var status by remember(userProfile) { mutableStateOf(userProfile["status"] ?: "Studying") }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    // Check if the user has a password provider
    val hasPasswordProvider = remember(user) {
        user?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true
    }
    
    // Treat null user (during sign out transition) as Guest to prevent UI flicker
    val isGuest = user == null || user?.isAnonymous == true

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Profile Photo (Simple Placeholder)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = "Profile Photo",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Current Status", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = status == "Studying",
                        onClick = { status = "Studying" }
                    )
                    Text("Studying")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = status == "Passed Out",
                        onClick = { status = "Passed Out" }
                    )
                    Text("Passed Out")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        appViewModel?.saveUserProfile(name, status)
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Profile")
                }
                
            } else {
                Text(
                    text = name.ifBlank { if(isGuest) "Guest User" else "No Name Set" },
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = user?.email ?: "Guest",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AssistChip(
                    onClick = { },
                    label = { Text(status) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedButton(
                    onClick = { isEditing = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign Out / Create Account Button
            Button(
                onClick = {
                    authViewModel.signOut()
                    onSignOut()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGuest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer, 
                    contentColor = if (isGuest) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isGuest) "Create Account / Login" else "Sign Out")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Delete Account Button - Hidden for Guest Users
            if (!isGuest) {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account")
                }
            }
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteDialog = false 
                    deletePassword = ""
                },
                title = { Text("Delete Account") },
                text = {
                    Column {
                        Text("Are you sure you want to delete your account? This action cannot be undone.")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (hasPasswordProvider) {
                            Text("Please enter your password to confirm:")
                            OutlinedTextField(
                                value = deletePassword,
                                onValueChange = { deletePassword = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text("Confirm deletion for your Google account.")
                        }
                        
                        if (authState is AuthState.Error) {
                            Text(
                                text = (authState as AuthState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            if (hasPasswordProvider) {
                                authViewModel.deleteAccount(deletePassword)
                            } else {
                                authViewModel.deleteAccount() // No password needed
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = authState !is AuthState.Loading
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDeleteDialog = false 
                        deletePassword = ""
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

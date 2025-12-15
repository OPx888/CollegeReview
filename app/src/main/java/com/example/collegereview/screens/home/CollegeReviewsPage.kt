package com.example.collegereview.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collegereview.AppViewModel
import com.example.collegereview.ReviewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeReviewsPage(
    collegeName: String,
    appViewModel: AppViewModel,
    onBack: () -> Unit
) {
    val allReviews by appViewModel.allReviews.collectAsStateWithLifecycle()
    val currentUserId = appViewModel.currentUserId
    
    // Filter reviews for this specific college
    val collegeReviews = remember(allReviews, collegeName) {
        allReviews.filter { it.collegeName.equals(collegeName, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Box(
                        modifier = Modifier.fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = collegeName, 
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.height(56.dp), // Standard height
                windowInsets = WindowInsets(0.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (collegeReviews.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reviews found for this college.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(collegeReviews) { review ->
                        ReviewItem(
                            review = review,
                            canDelete = currentUserId == review.userId,
                            onDelete = { appViewModel.deleteReview(review) }
                        )
                    }
                }
            }
        }
    }
}

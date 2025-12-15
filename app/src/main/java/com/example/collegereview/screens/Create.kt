package com.example.collegereview.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.collegereview.AppViewModel
import com.example.collegereview.ReviewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePage(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel
) {
    var collegeName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var reviewDescription by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0f) }
    
    var collegeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    val reviewState by appViewModel.reviewState.collectAsStateWithLifecycle()
    val colleges by appViewModel.colleges.collectAsStateWithLifecycle()
    val categories by appViewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(reviewState) {
        when (val state = reviewState) {
            is ReviewState.Success -> {
                Toast.makeText(context, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                collegeName = ""
                category = ""
                reviewDescription = ""
                rating = 0f
                collegeExpanded = false
                categoryExpanded = false
                appViewModel.resetReviewState()
            }
            is ReviewState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                appViewModel.resetReviewState()
            }
            else -> Unit
        }
    }
    
    // Optimize filtering
    val filteredColleges = remember(collegeName, colleges) {
        if (collegeName.isBlank()) colleges.take(5) else colleges.filter { it.contains(collegeName, ignoreCase = true) }.take(5)
    }
    
    val filteredCategories = remember(category, categories) {
        if (category.isBlank()) categories else categories.filter { it.contains(category, ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Share Your Experience",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // College Name Input
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = collegeName,
                    onValueChange = { 
                        collegeName = it
                        collegeExpanded = true 
                    },
                    label = { Text("College Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { collegeExpanded = !collegeExpanded }) {
                            Icon(
                                imageVector = if (collegeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse"
                            )
                        }
                    },
                    enabled = reviewState !is ReviewState.Loading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                if (collegeExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column {
                            if (filteredColleges.isNotEmpty()) {
                                filteredColleges.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                collegeName = item
                                                collegeExpanded = false
                                            }
                                            .padding(16.dp)
                                    ) {
                                        Text(text = item)
                                    }
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            } else if (collegeName.isNotBlank()) {
                                Text(
                                    text = "No result found",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Category Input
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { 
                        category = it
                        categoryExpanded = true 
                    },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Academic, Sports") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                            Icon(
                                imageVector = if (categoryExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = "Expand/Collapse"
                            )
                        }
                    },
                    enabled = reviewState !is ReviewState.Loading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                if (categoryExpanded) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column {
                            if (filteredCategories.isNotEmpty()) {
                                filteredCategories.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                category = item
                                                categoryExpanded = false
                                            }
                                            .padding(16.dp)
                                    ) {
                                        Text(text = item)
                                    }
                                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            } else if (category.isNotBlank()) {
                                Text(
                                    text = "New category will be added",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = reviewDescription,
                onValueChange = { reviewDescription = it },
                label = { Text("Your Review") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = reviewState !is ReviewState.Loading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Rating Stars
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Rate your experience", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (i in 1..5) {
                            val isSelected = i <= rating
                            val icon = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star
                            val tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant // Gold color for stars
                            
                            Icon(
                                imageVector = icon,
                                contentDescription = "Star $i",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(4.dp)
                                    .clickable { rating = i.toFloat() },
                                tint = tint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { appViewModel.submitReview(collegeName, category, reviewDescription, rating) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = reviewState !is ReviewState.Loading
            ) {
                if (reviewState is ReviewState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit Review", fontSize = 16.sp)
                }
            }
        }
    }
}

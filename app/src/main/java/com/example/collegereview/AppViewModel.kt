package com.example.collegereview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.collegereview.database.AppDatabase
import com.example.collegereview.database.ReviewEntity
import com.example.collegereview.database.ReviewRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class Review(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val collegeName: String = "",
    val category: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

data class CollegeStats(
    val collegeName: String,
    val averageRating: Float,
    val reviewCount: Int
)

sealed class ReviewState {
    object Idle : ReviewState()
    object Loading : ReviewState()
    object Success : ReviewState()
    data class Error(val message: String) : ReviewState()
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = Firebase.auth
    private val repository: ReviewRepository

    init {
        val db = AppDatabase.getDatabase(application)
        val firestoreDb = FirebaseFirestore.getInstance()
        repository = ReviewRepository(db.reviewDao(), firestoreDb)
        
        // Sync reviews from Firestore on startup
        viewModelScope.launch {
            repository.syncReviews()
        }
        
        // Fetch colleges on startup
        viewModelScope.launch {
             val fetchedColleges = repository.getColleges()
             _colleges.value = fetchedColleges
        }
        
        // Fetch categories
        viewModelScope.launch {
            val fetchedCategories = repository.getCategories(getApplication())
            _categories.value = fetchedCategories
        }
        
        // Fetch user profile
        fetchUserProfile()
    }

    private val _reviewState = MutableStateFlow<ReviewState>(ReviewState.Idle)
    val reviewState: StateFlow<ReviewState> = _reviewState.asStateFlow()
    
    private val _colleges = MutableStateFlow<List<String>>(emptyList())
    val colleges: StateFlow<List<String>> = _colleges.asStateFlow()
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()
    
    private val _userProfile = MutableStateFlow<Map<String, String>>(emptyMap())
    val userProfile: StateFlow<Map<String, String>> = _userProfile.asStateFlow()

    val allReviews: StateFlow<List<ReviewEntity>> = repository.allReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    val collegeStats: StateFlow<List<CollegeStats>> = allReviews.map { reviews ->
        reviews.groupBy { it.collegeName }
            .map { (name, collegeReviews) ->
                val avg = if (collegeReviews.isNotEmpty()) {
                    collegeReviews.map { it.rating }.average().toFloat()
                } else 0f
                CollegeStats(name, avg, collegeReviews.size)
            }
            .sortedByDescending { it.reviewCount } 
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isGuest: Boolean
        get() = auth.currentUser?.isAnonymous == true
        
    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun fetchUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val profile = repository.getUserProfile(userId)
            if (profile != null) {
                val mappedProfile = mapOf(
                    "name" to (profile["name"] as? String ?: ""),
                    "status" to (profile["status"] as? String ?: "")
                )
                _userProfile.value = mappedProfile
            }
        }
    }

    fun saveUserProfile(name: String, status: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.saveUserProfile(userId, name, status)
            // Update local state immediately
            _userProfile.value = mapOf("name" to name, "status" to status)
        }
    }

    fun submitReview(collegeName: String, category: String, description: String, rating: Float) {
        val user = auth.currentUser
        if (user == null) {
            _reviewState.value = ReviewState.Error("User not logged in")
            return
        }

        if (user.isAnonymous) {
            _reviewState.value = ReviewState.Error("Guests cannot submit reviews. Please sign in.")
            return
        }

        if (collegeName.isBlank() || description.isBlank() || category.isBlank()) {
            _reviewState.value = ReviewState.Error("Please fill in all fields")
            return
        }
        
        if (rating == 0f) {
            _reviewState.value = ReviewState.Error("Please add a rating")
            return
        }
        
        val userName = _userProfile.value["name"]?.takeIf { it.isNotBlank() } ?: "Anonymous"

        viewModelScope.launch {
            _reviewState.value = ReviewState.Loading
            
            val review = Review(
                userId = user.uid,
                userEmail = user.email ?: "Anonymous",
                userName = userName,
                collegeName = collegeName,
                category = category,
                description = description,
                rating = rating
            )

            try {
                repository.submitReview(review)
                _reviewState.value = ReviewState.Success
            } catch (e: Exception) {
                _reviewState.value = ReviewState.Error(e.message ?: "Failed to submit review")
            }
        }
    }
    
    fun deleteReview(review: ReviewEntity) {
        val user = auth.currentUser
        if (user == null || user.uid != review.userId) {
            _reviewState.value = ReviewState.Error("You can only delete your own reviews")
            return
        }
        
        viewModelScope.launch {
            try {
                repository.deleteReview(review.id)
            } catch (e: Exception) {
                _reviewState.value = ReviewState.Error(e.message ?: "Failed to delete review")
            }
        }
    }
    
    fun uploadCollegesDB() {
        viewModelScope.launch {
            repository.uploadCollegesFromJson(getApplication())
            // Refresh the list after upload
            val fetchedColleges = repository.getColleges()
            _colleges.value = fetchedColleges
        }
    }
    
    fun resetReviewState() {
        _reviewState.value = ReviewState.Idle
    }
}

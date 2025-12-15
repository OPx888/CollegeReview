package com.example.collegereview.database

import android.content.Context
import com.example.collegereview.R
import com.example.collegereview.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class ReviewRepository(
    private val reviewDao: ReviewDao,
    private val firestoreDb: FirebaseFirestore
) {
    val allReviews: Flow<List<ReviewEntity>> = reviewDao.getAllReviews()

    suspend fun submitReview(review: Review) {
        val id = UUID.randomUUID().toString()
        // Save to Room first to ensure immediate UI feedback
        val reviewEntity = ReviewEntity(
            id = id,
            userId = review.userId,
            userEmail = review.userEmail,
            collegeName = review.collegeName,
            category = review.category,
            description = review.description,
            rating = review.rating,
            timestamp = review.timestamp,
        )
        reviewDao.insertReview(reviewEntity)

        // Save to Firestore asynchronously (Fire and Forget)
        try {
            val firestoreReview = review.copy(id = id)
            firestoreDb.collection("reviews").document(id).set(firestoreReview)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun deleteReview(reviewId: String) {
        // Delete from Room
        reviewDao.deleteReviewById(reviewId)
        
        // Delete from Firestore
        try {
            firestoreDb.collection("reviews").document(reviewId).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun syncReviews() {
        try {
            val snapshot = firestoreDb.collection("reviews").get().await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ReviewEntity(
                    id = doc.id,
                    userId = data["userId"] as? String ?: "",
                    userEmail = data["userEmail"] as? String ?: "",
                    collegeName = data["collegeName"] as? String ?: "",
                    category = data["category"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    rating = (data["rating"] as? Double)?.toFloat() ?: 0f,
                    timestamp = (data["timestamp"] as? Long) ?: System.currentTimeMillis(),
                )
            }
            if (reviews.isNotEmpty()) {
                reviewDao.insertReviews(reviews)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getColleges(): List<String> {
        return try {
            val snapshot = firestoreDb.collection("colleges").orderBy("name").get().await()
            snapshot.documents.mapNotNull { it.getString("name") }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getCategories(context: Context): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.resources.openRawResource(R.raw.categories)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                
                val list = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    list.add(jsonArray.getString(i))
                }
                list.sorted()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun uploadCollegesFromJson(context: Context) {
        try {
            val inputStream = context.resources.openRawResource(R.raw.colleges)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = reader.use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            
            val batch = firestoreDb.batch()
            
            for (i in 0 until jsonArray.length()) {
                val collegeObj = jsonArray.getJSONObject(i)
                if (!collegeObj.has("name")) continue
                
                val collegeName = collegeObj.getString("name")
                val data = HashMap<String, Any>()
                data["name"] = collegeName
                if (collegeObj.has("type")) data["type"] = collegeObj.getString("type")
                if (collegeObj.has("location")) {
                    val locObj = collegeObj.getJSONObject("location")
                    val locMap = HashMap<String, Any>()
                    if (locObj.has("city")) locMap["city"] = locObj.getString("city")
                    if (locObj.has("state")) locMap["state"] = locObj.getString("state")
                    if (locObj.has("country")) locMap["country"] = locObj.getString("country")
                    data["location"] = locMap
                }

                val docRef = firestoreDb.collection("colleges").document() 
                batch.set(docRef, data)
            }
            batch.commit().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveUserProfile(userId: String, name: String, status: String) {
        try {
            val data = mapOf(
                "name" to name,
                "status" to status
            )
            firestoreDb.collection("users").document(userId).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val snapshot = firestoreDb.collection("users").document(userId).get().await()
            snapshot.data
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

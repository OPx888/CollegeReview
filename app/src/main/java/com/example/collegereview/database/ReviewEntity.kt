package com.example.collegereview.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val userEmail: String,
    val collegeName: String,
    val category: String = "",
    val description: String,
    val rating: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

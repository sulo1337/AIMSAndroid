package com.example.aimsandroid.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.aimsandroid.database.Review
import com.example.aimsandroid.database.ReviewDatabase

class ReviewsRepository(private val database: ReviewDatabase) {
    val reviews: LiveData<List<Review>> = database.reviewDao.getReviews()
}
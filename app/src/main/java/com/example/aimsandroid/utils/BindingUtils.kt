package com.example.aimsandroid.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.database.Review
import com.example.aimsandroid.homefragment.ReviewsAdapter

@BindingAdapter("bindReviews")
fun bindListOfReviewsToRecyclerView(recyclerView: RecyclerView, data: List<Review>?) {
    val adapter = recyclerView.adapter as ReviewsAdapter
    adapter.submitList(data)
}
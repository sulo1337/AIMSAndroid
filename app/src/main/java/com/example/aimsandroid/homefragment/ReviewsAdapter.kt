package com.example.aimsandroid.homefragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.database.Review
import com.example.aimsandroid.databinding.ReviewItemBinding

class ReviewsAdapter: ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReviewsAdapter.ReviewViewHolder {
        return ReviewViewHolder(ReviewItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ReviewsAdapter.ReviewViewHolder, position: Int) {
        val thisReview = getItem(position)
        holder.bind(thisReview)
    }

    class ReviewViewHolder(private var binding: ReviewItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(thisReview: Review?) {
            binding.review = thisReview
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

    }

}
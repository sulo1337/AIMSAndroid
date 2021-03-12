//package com.example.aimsandroid.homefragment
//
//import android.app.Application
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import android.view.animation.Animation
//import android.view.animation.AnimationUtils
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.example.aimsandroid.R
//import com.example.aimsandroid.database.Review
//import com.example.aimsandroid.databinding.ReviewItemBinding
//
//class ReviewsAdapter(application: Application): ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(DiffCallback) {
//    private val application = application
//    override fun onCreateViewHolder(
//        parent: ViewGroup,
//        viewType: Int
//    ): ReviewsAdapter.ReviewViewHolder {
//        return ReviewViewHolder(ReviewItemBinding.inflate(LayoutInflater.from(parent.context)))
//    }
//
//    override fun onBindViewHolder(holder: ReviewsAdapter.ReviewViewHolder, position: Int) {
//        val thisReview = getItem(position)
//        val animation: Animation = AnimationUtils.loadAnimation(application, R.anim.slide_in_animation)
//        holder.itemView.startAnimation(animation)
//        holder.bind(thisReview)
//    }
//
//
//
//    class ReviewViewHolder(private var binding: ReviewItemBinding): RecyclerView.ViewHolder(binding.root) {
//        fun bind(thisReview: Review?) {
//            binding.review = thisReview
//        }
//    }
//
//    companion object DiffCallback: DiffUtil.ItemCallback<Review>() {
//        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//    }
//
//}
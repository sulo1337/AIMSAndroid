package com.example.aimsandroid.fragments.trips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.databinding.TripItemBinding

class TripsAdapter(val tripsClickListener: TripsClickListener): ListAdapter<TripWithWaypoints, TripsAdapter.TripsViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripsAdapter.TripsViewHolder {
        return TripsViewHolder(TripItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TripsAdapter.TripsViewHolder, position: Int) {
        val thisTripWithWaypoints = getItem(position)
        holder.bind(thisTripWithWaypoints, tripsClickListener)
    }

    class TripsViewHolder(private var binding: TripItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(thisTripWithWaypoints: TripWithWaypoints, tripsClickListener: TripsClickListener){
            binding.tripWithWaypoints = thisTripWithWaypoints
            binding.clickListener = tripsClickListener
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<TripWithWaypoints>() {
        override fun areItemsTheSame(oldItem: TripWithWaypoints, newItem: TripWithWaypoints): Boolean {
            return (oldItem.trip.tripId == newItem.trip.tripId)
        }

        override fun areContentsTheSame(oldItem: TripWithWaypoints, newItem: TripWithWaypoints): Boolean {
            return (oldItem.waypoints == newItem.waypoints && oldItem.trip == newItem.trip)
        }

    }

    class TripsClickListener(val clickListener: (trip: TripWithWaypoints) -> Unit) {
        fun onClick(tripWithWaypoints: TripWithWaypoints) = clickListener(tripWithWaypoints)
    }
}
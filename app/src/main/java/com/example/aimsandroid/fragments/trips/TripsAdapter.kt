package com.example.aimsandroid.fragments.trips

import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.databinding.TripItemBinding

class TripsAdapter(val tripsClickListener: TripsClickListener, private val prefs: SharedPreferences): ListAdapter<TripWithWaypoints, TripsAdapter.TripsViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripsAdapter.TripsViewHolder {
        return TripsViewHolder(TripItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TripsAdapter.TripsViewHolder, position: Int) {
        val thisTripWithWaypoints = getItem(position)
        Log.i("aimsDebug", thisTripWithWaypoints.tripStatus.toString())
        holder.bind(thisTripWithWaypoints, tripsClickListener, prefs)
    }

    class TripsViewHolder(private var binding: TripItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(
            thisTripWithWaypoints: TripWithWaypoints,
            tripsClickListener: TripsClickListener,
            prefs: SharedPreferences
        ){
            binding.tripWithWaypoints = thisTripWithWaypoints
            binding.clickListener = tripsClickListener

            if(prefs.getLong("currentTripId", -1) == thisTripWithWaypoints.trip.tripId) {
                binding.pendingIcon.visibility = View.VISIBLE
                binding.completeIcon.visibility = View.GONE
            } else {
                if(thisTripWithWaypoints.tripStatus == null) {
                    binding.completeIcon.visibility = View.GONE
                    binding.pendingIcon.visibility = View.GONE
                } else if (thisTripWithWaypoints.tripStatus.complete) {
                    binding.completeIcon.visibility = View.VISIBLE
                    binding.pendingIcon.visibility = View.GONE
                }
            }

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
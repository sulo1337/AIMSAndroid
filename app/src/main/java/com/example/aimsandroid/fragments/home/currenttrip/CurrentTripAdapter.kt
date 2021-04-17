package com.example.aimsandroid.fragments.home.currenttrip

import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.databinding.CurrentTripDetailItemBinding
import com.example.aimsandroid.databinding.DialogTripDetailsItemBinding
import java.lang.Exception

class CurrentTripAdapter(val clickListener: CurrentTripClickListener, private val prefs: SharedPreferences): ListAdapter<WayPoint, CurrentTripAdapter.CurrentTripsDetailViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentTripAdapter.CurrentTripsDetailViewHolder {
        return CurrentTripsDetailViewHolder(CurrentTripDetailItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: CurrentTripsDetailViewHolder, position: Int) {
        val thisWaypoint = getItem(position)
        val nextWaypointSeqNum = prefs.getLong("nextWaypointSeqNumber", -1)
        val currentTripId = prefs.getLong("currentTripId", -1)
        if(thisWaypoint.owningTripId == currentTripId && thisWaypoint.seqNum == nextWaypointSeqNum) {
            holder.bind(thisWaypoint, position, clickListener, true)
        } else {
            holder.bind(thisWaypoint, position, clickListener, false)
        }
    }

    class CurrentTripsDetailViewHolder(private var binding: CurrentTripDetailItemBinding): RecyclerView.ViewHolder(binding.root){
        //TODO set colors
        fun bind(thisWayPoint: WayPoint, position: Int, clickListener: CurrentTripClickListener, isNextWaypoint: Boolean){
            binding.wayPoint = thisWayPoint
            binding.address.text = thisWayPoint.address1.trim() + ", " + thisWayPoint.city.trim() + ", " + thisWayPoint.state.trim() + " " + thisWayPoint.postalCode
            binding.deadline.text = "19 December, 2020"
            binding.fuelQuantity.text = thisWayPoint.requestedQty.toString() + " " + thisWayPoint.uom
            binding.fuelType.text = thisWayPoint.productDesc
            binding.waypointTitle.text = thisWayPoint.destinationName
            binding.clickListener = clickListener
            when(thisWayPoint.waypointTypeDescription.trim()){
                "Source" -> {
                    binding.waypointType.text = "L" + (position+1).toString()
                    binding.waypointTypeIcon.setImageResource(R.drawable.ic_source)
                }
                else -> {
                    binding.waypointType.text = "U" + (position+1).toString()
                    binding.waypointTypeIcon.setImageResource(R.drawable.ic_destination)
                }
            }
            if(!isNextWaypoint){
                binding.waypointTypeIcon.setColorFilter(Color.LTGRAY)
                binding.waypointType.setTextColor(Color.GRAY)
                binding.waypointTitle.setTextColor(Color.LTGRAY)
                binding.deadline.setTextColor(Color.LTGRAY)
                binding.address.setTextColor(Color.LTGRAY)
                binding.divider.setBackgroundColor(Color.LTGRAY)
                binding.fuelType.setTextColor(Color.LTGRAY)
                binding.fuelQuantity.setTextColor(Color.LTGRAY)
                binding.detailsButton.setBackgroundColor(Color.LTGRAY)
                binding.navigateButton.visibility = View.GONE
            }
        }
    }

    companion object DiffCallback: DiffUtil.ItemCallback<WayPoint>() {
        override fun areItemsTheSame(oldItem: WayPoint, newItem: WayPoint): Boolean {
            return oldItem.owningTripId == newItem.owningTripId && oldItem.seqNum == newItem.seqNum
        }

        override fun areContentsTheSame(oldItem: WayPoint, newItem: WayPoint): Boolean {
            return oldItem == newItem
        }
    }

    class CurrentTripClickListener(val detailsClickListener: (wayPoint: WayPoint) -> Unit, val navigateClickListener: (wayPoint: WayPoint) -> Unit) {
        fun onClickDetails(wayPoint: WayPoint) = detailsClickListener(wayPoint)
        fun onClickNavigate(wayPoint: WayPoint) = navigateClickListener(wayPoint)
    }
}
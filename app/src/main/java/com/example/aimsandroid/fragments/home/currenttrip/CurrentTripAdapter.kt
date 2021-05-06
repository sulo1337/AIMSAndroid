package com.example.aimsandroid.fragments.home.currenttrip

import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import colorGreen
import colorSecondaryLight
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.databinding.CurrentTripDetailItemBinding
import com.example.aimsandroid.databinding.DialogTripDetailsItemBinding
import com.example.aimsandroid.repository.TripRepository
import getFullAddress
import getWaypointDate
import kotlinx.coroutines.*
import java.lang.Exception

/*
* Adapter to convert data into view holder of recycler view
* See android ListAdapter documentation for details about this class
* Mostly boilerplate code
* */
class CurrentTripAdapter(val clickListener: CurrentTripClickListener, private val prefs: SharedPreferences, private val tripRepository: TripRepository): ListAdapter<WayPoint, CurrentTripAdapter.CurrentTripsDetailViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentTripAdapter.CurrentTripsDetailViewHolder {
        return CurrentTripsDetailViewHolder(CurrentTripDetailItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: CurrentTripsDetailViewHolder, position: Int) {
        val thisWaypoint = getItem(position)
        val scope = CoroutineScope(Job())
        scope.launch {
            withContext(Dispatchers.IO){
                var isNextWaypoint = false
                var isCompleted = false
                val nextWaypointSeqNum = prefs.getLong("nextWaypointSeqNumber", -1)
                val currentTripId = prefs.getLong("currentTripId", -1)
                val billOfLading = tripRepository.getWaypointWithBillOfLading(thisWaypoint.seqNum, thisWaypoint.owningTripId).billOfLading
                if(billOfLading!= null) {
                    if(billOfLading.complete == true){
                        isCompleted = true
                        isNextWaypoint = false
                    } else {
                            if(thisWaypoint.owningTripId == currentTripId && thisWaypoint.seqNum == nextWaypointSeqNum) {
                                isNextWaypoint = true
                                isCompleted = false
                            } else {
                                isNextWaypoint = false
                                isCompleted = false
                            }
                    }
                } else {
                        if(thisWaypoint.owningTripId == currentTripId && thisWaypoint.seqNum == nextWaypointSeqNum) {
                            isNextWaypoint = true
                            isCompleted = false
                        } else {
                            isNextWaypoint = false
                            isCompleted = false
                        }
                }
                withContext(Dispatchers.Main) {
                    holder.bind(thisWaypoint, position, clickListener, isNextWaypoint, isCompleted)
                }
            }
        }
    }

    class CurrentTripsDetailViewHolder(private var binding: CurrentTripDetailItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(thisWayPoint: WayPoint, position: Int, clickListener: CurrentTripClickListener, isNextWaypoint: Boolean, isCompleted: Boolean){
            binding.wayPoint = thisWayPoint
            binding.address.text = getFullAddress(thisWayPoint)
            binding.deadline.text = getWaypointDate(thisWayPoint.date?.trim()?.substring(0,11)?:"")
            if(thisWayPoint.requestedQty == null || thisWayPoint.uom == null) {
                binding.fuelQuantity.visibility = View.GONE
            } else {
                binding.fuelQuantity.text = thisWayPoint.requestedQty.toString() + " " + thisWayPoint.uom.toString()
            }
            if(thisWayPoint.productDesc != null) {
                binding.fuelType.text = thisWayPoint.productDesc
            } else {
                binding.fuelType.visibility = View.GONE
            }
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

            if (isNextWaypoint) {
                binding.waypointTitle.setTextColor(colorSecondaryLight)
                binding.waypointType.setTextColor(colorSecondaryLight)
            } else {
                binding.waypointTypeIcon.setColorFilter(Color.LTGRAY)
                binding.waypointTitle.setTextColor(Color.GRAY)
                binding.deadline.setTextColor(Color.LTGRAY)
                binding.address.setTextColor(Color.LTGRAY)
                binding.divider.setBackgroundColor(Color.LTGRAY)
                binding.fuelType.setTextColor(Color.LTGRAY)
                binding.fuelQuantity.setTextColor(Color.LTGRAY)
                binding.detailsButton.setBackgroundColor(Color.LTGRAY)
                binding.navigateButton.visibility = View.GONE
                binding.fuelType.visibility = View.GONE
                binding.deadline.visibility = View.GONE
                binding.navigateButton.visibility = View.GONE
            }

            if(isCompleted) {
                binding.divider.setBackgroundColor(colorGreen)
                binding.waypointTypeIcon.setColorFilter(colorGreen)
                binding.waypointType.setTextColor(colorGreen)
                binding.waypointTitle.setTextColor(colorGreen)
                binding.address.setTextColor(Color.GRAY)
                binding.detailsButton.setBackgroundColor(Color.GRAY)
                binding.fuelQuantity.visibility = View.GONE
                binding.fuelType.visibility = View.GONE
                binding.deadline.visibility = View.GONE
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
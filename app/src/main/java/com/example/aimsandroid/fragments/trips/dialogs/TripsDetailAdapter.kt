package com.example.aimsandroid.fragments.trips.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.databinding.DialogTripDetailsItemBinding
import getFullAddress
import getWaypointDate

/*
* Adapter to convert data into view holder of recycler view
* See android ListAdapter documentation for details about this class
* Mostly boilerplate code
* */
class TripsDetailAdapter(val clickListener: TripsDetailClickListener): ListAdapter<WayPoint, TripsDetailAdapter.TripsDetailViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripsDetailAdapter.TripsDetailViewHolder {
        return TripsDetailViewHolder(DialogTripDetailsItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: TripsDetailViewHolder, position: Int) {
        val thisWaypoint = getItem(position)
        holder.bind(thisWaypoint, position, clickListener)
    }

    class TripsDetailViewHolder(private var binding: DialogTripDetailsItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(thisWayPoint: WayPoint, position: Int, clickListener: TripsDetailClickListener){
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

    class TripsDetailClickListener(val clickListener: (wayPoint: WayPoint) -> Unit) {
        fun onClick(wayPoint: WayPoint) = clickListener(wayPoint)
    }
}
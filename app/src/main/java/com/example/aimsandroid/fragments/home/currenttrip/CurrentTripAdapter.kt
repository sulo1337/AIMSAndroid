package com.example.aimsandroid.fragments.home.currenttrip

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.databinding.CurrentTripDetailItemBinding
import com.example.aimsandroid.databinding.DialogTripDetailsItemBinding

class CurrentTripAdapter(val clickListener: CurrentTripClickListener): ListAdapter<WayPoint, CurrentTripAdapter.CurrentTripsDetailViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentTripAdapter.CurrentTripsDetailViewHolder {
        return CurrentTripsDetailViewHolder(CurrentTripDetailItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: CurrentTripsDetailViewHolder, position: Int) {
        val thisWaypoint = getItem(position)
        holder.bind(thisWaypoint, position, clickListener)
    }

    class CurrentTripsDetailViewHolder(private var binding: CurrentTripDetailItemBinding): RecyclerView.ViewHolder(binding.root){
        //TODO set colors
        fun bind(thisWayPoint: WayPoint, position: Int, clickListener: CurrentTripClickListener){
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
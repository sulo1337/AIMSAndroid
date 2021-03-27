package com.example.aimsandroid.fragments.home.currenttrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.databinding.DialogWaypointDetailsBinding
import com.example.aimsandroid.repository.TripRepository
import com.nokia.maps.restrouting.Waypoint

class WaypointDetailDialog(private val waypoint: WayPoint): DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding = DialogWaypointDetailsBinding.inflate(inflater);
        binding.waypoint = waypoint
        //TODO fetch real data instead of hard-coding
        binding.tripName.text = "A-159"
        binding.truck.text = "PETERBILT TRANSPORT"
        binding.trailer.text = "TANKER #2"
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.address.text = waypoint.address1.trim() + ", " + waypoint.city.trim() + ", " + waypoint.state.trim() + " " + waypoint.postalCode
        if(waypoint.waypointTypeDescription.equals("Source")){
            binding.pickupOrDeliverTitle.text = "Pick Up"
            binding.siteContainerTitle.visibility = View.GONE
            binding.siteContainer.visibility = View.GONE
        } else{
            binding.pickupOrDeliverTitle.text = "Delivery"
            binding.siteContainerTitle.visibility = View.VISIBLE
            binding.siteContainer.visibility = View.VISIBLE
            binding.siteContainer.text = waypoint.siteContainerDescription + " " + waypoint.siteContainerCode + " - " + waypoint.fill
        }

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {
        fun newInstance(waypoint: WayPoint): WaypointDetailDialog{
            return WaypointDetailDialog(waypoint)
        }
    }
}
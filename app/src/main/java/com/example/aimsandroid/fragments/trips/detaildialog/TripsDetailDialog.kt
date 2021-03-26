package com.example.aimsandroid.fragments.trips.detaildialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.aimsandroid.R
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.databinding.DialogTripDetailsBinding

class TripsDetailDialog(private val tripWithWaypoints: TripWithWaypoints): DialogFragment(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = DialogTripDetailsBinding.inflate(inflater)
        val closeButton = binding.closeButton
        binding.dialogTitle.text = "Trip #"+tripWithWaypoints.trip.tripId
        closeButton.setOnClickListener {
            it?.let {
                dismiss()
            }
        }
        binding.lifecycleOwner = this
        val adapter = TripsDetailAdapter(TripsDetailAdapter.TripsDetailClickListener {
            Toast.makeText(requireActivity(), "Yet to be implemented...", Toast.LENGTH_SHORT).show()
        })
        adapter.submitList(tripWithWaypoints.waypoints)
        binding.tripDetailRecyclerView.adapter = adapter
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {
        fun newInstance(tripWithWaypoints: TripWithWaypoints): TripsDetailDialog {
            return TripsDetailDialog(tripWithWaypoints)
        }
    }
}
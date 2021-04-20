package com.example.aimsandroid.fragments.trips.detaildialog

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.databinding.DialogTripDetailsBinding
import com.example.aimsandroid.fragments.trips.TripsFragment
import com.example.aimsandroid.fragments.trips.TripsFragmentDirections
import com.example.aimsandroid.fragments.trips.TripsViewModel
import com.example.aimsandroid.fragments.trips.TripsViewModelFactory
import com.example.aimsandroid.repository.TripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sortWaypointBySeqNum

class TripsDetailDialog(private val tripWithWaypoints: TripWithWaypoints): DialogFragment(){

    private lateinit var binding: DialogTripDetailsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogTripDetailsBinding.inflate(inflater)
        prefs = requireActivity().getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)

        val closeButton = binding.closeButton
        binding.dialogTitle.text = "Trip #"+tripWithWaypoints.trip.tripId
        closeButton.setOnClickListener {
            it?.let {
                dismiss()
            }
        }
        binding.lifecycleOwner = this
        val adapter = TripsDetailAdapter(TripsDetailAdapter.TripsDetailClickListener {
            val dialog: WaypointDetailDialog = WaypointDetailDialog.newInstance(it)
            dialog.show(childFragmentManager, "wayPointDetailDialog")
        })
        adapter.submitList(sortWaypointBySeqNum(tripWithWaypoints.waypoints))
        binding.tripDetailRecyclerView.adapter = adapter

        if (tripWithWaypoints.tripStatus == null){
            if(prefs.getLong("currentTripId", -1) == tripWithWaypoints.trip.tripId){
                binding.startTrip.visibility = View.GONE
                binding.continueTrip.visibility = View.VISIBLE
            } else {
                binding.continueTrip.visibility  = View.GONE
                binding.startTrip.visibility = View.VISIBLE
            }
        } else if(tripWithWaypoints.tripStatus.complete) {
            binding.startTrip.visibility = View.GONE
            binding.continueTrip.visibility = View.GONE
        }

        binding.continueTrip.setOnClickListener {
            this.findNavController().navigate(TripsFragmentDirections.actionTripsFragmentToHomeFragment(true))
            dismiss()
        }

        binding.startTrip.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Confirm?")
                .setMessage("Do you want to start this trip?")
                .setNegativeButton(
                    "No"
                ) { dialoginterface, i ->
                    dialoginterface.dismiss()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialoginterface, i ->
                    prefs.edit().putLong("currentTripId", tripWithWaypoints.trip.tripId).apply()
                    this.findNavController().navigate(TripsFragmentDirections.actionTripsFragmentToHomeFragment(true))
                    dismiss()
                }.create().show()
        }
        return binding.root
    }

    override fun onDestroyView() {
        if(retainInstance && dialog!= null) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    companion object {
        fun newInstance(tripWithWaypoints: TripWithWaypoints): TripsDetailDialog {
            return TripsDetailDialog(tripWithWaypoints)
        }
    }
}
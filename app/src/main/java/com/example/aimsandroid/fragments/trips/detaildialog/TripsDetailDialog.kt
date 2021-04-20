package com.example.aimsandroid.fragments.trips.detaildialog

import android.content.Context
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

class TripsDetailDialog(private val tripWithWaypoints: TripWithWaypoints, private val completed: Boolean): DialogFragment(){

    private lateinit var binding: DialogTripDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogTripDetailsBinding.inflate(inflater)

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

        if(completed) {
            binding.startTrip.visibility = View.GONE
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
                    val prefs = requireActivity().getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
                    prefs.edit().putLong("currentTripId", tripWithWaypoints.trip.tripId).apply()
                    this.findNavController().navigate(TripsFragmentDirections.actionTripsFragmentToHomeFragment(true))
                    dismiss()
                }.create().show()
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {
        fun newInstance(tripWithWaypoints: TripWithWaypoints, completed: Boolean): TripsDetailDialog {
            return TripsDetailDialog(tripWithWaypoints, completed)
        }
    }
}
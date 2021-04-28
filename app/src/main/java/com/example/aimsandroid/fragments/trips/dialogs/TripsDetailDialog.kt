package com.example.aimsandroid.fragments.trips.dialogs

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.databinding.DialogTripDetailsBinding
import com.example.aimsandroid.fragments.trips.TripsFragment
import com.example.aimsandroid.fragments.trips.TripsFragmentDirections
import com.example.aimsandroid.utils.FileLoaderListener
import com.example.aimsandroid.utils.OnSaveListener
import sortWaypointBySeqNum


class TripsDetailDialog(private val tripWithWaypoints: TripWithWaypoints): DialogFragment(){

    private lateinit var binding: DialogTripDetailsBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        root.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }

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
            //if this is the current trip
            if(prefs.getLong("currentTripId", -1L) == tripWithWaypoints.trip.tripId){
                binding.startTrip.visibility = View.GONE
                binding.continueTrip.visibility = View.VISIBLE
            }
            //if there is no value for current trip
            else if (prefs.getLong("currentTripId", -1L) == -1L) {
                binding.continueTrip.visibility  = View.GONE
                binding.startTrip.visibility = View.VISIBLE
            }
            //if this is not current trip and there is a value for current trip
            else {
                binding.startTrip.visibility = View.GONE
                binding.continueTrip.visibility = View.GONE
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

    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap, onSaveListener: OnSaveListener) {
        (parentFragment as TripsFragment).getViewModel().saveForm(billOfLading, bolBitmap, onSaveListener)
    }

    companion object {
        fun newInstance(tripWithWaypoints: TripWithWaypoints): TripsDetailDialog {
            return TripsDetailDialog(tripWithWaypoints)
        }
    }

    suspend fun getSignatureUri(tripIdFk: Long, waypointSeqNum: Long, fileLoaderListener: FileLoaderListener) {
        (parentFragment as TripsFragment).getSignatureUri(tripIdFk, waypointSeqNum, fileLoaderListener)
    }

    suspend fun getBolUri(tripIdFk: Long, waypointSeqNum: Long, fileLoaderListener: FileLoaderListener) {
        (parentFragment as TripsFragment).getBolUri(tripIdFk, waypointSeqNum, fileLoaderListener)
    }
}
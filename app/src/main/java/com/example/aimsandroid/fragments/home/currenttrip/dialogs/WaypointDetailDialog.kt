package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.databinding.AlertStartBolBinding
import com.example.aimsandroid.databinding.DialogWaypointDetailsBinding
import com.example.aimsandroid.fragments.home.HomeFragment
import com.example.aimsandroid.repository.TripRepository
import com.here.android.mpa.common.GeoCoordinate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WaypointDetailDialog(private val waypoint: WayPoint): DialogFragment() {

    private lateinit var tripRepository: TripRepository
    private lateinit var binding: DialogWaypointDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //TODO decouple into viewmodel
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DialogWaypointDetailsBinding.inflate(inflater);
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
            binding.startLoading.text = "Start Loading"
            binding.stopLoading.text = "Stop Loading"
            binding.siteContainerTitle.visibility = View.GONE
            binding.siteContainer.visibility = View.GONE
        } else{
            binding.pickupOrDeliverTitle.text = "Delivery"
            binding.startLoading.text = "Start Delivery"
            binding.stopLoading.text = "Stop Delivery"
            binding.siteContainerTitle.visibility = View.VISIBLE
            binding.siteContainer.visibility = View.VISIBLE
            binding.siteContainer.text = waypoint.siteContainerDescription + " " + waypoint.siteContainerCode + " - " + waypoint.fill
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //fetch this waypoint's bill of lading
            tripRepository = TripRepository(getDatabase(requireActivity().application))
            val billOfLading: LiveData<BillOfLading> = tripRepository.getBillOfLading(waypoint.seqNum, waypoint.owningTripId)
            billOfLading.observe(viewLifecycleOwner, Observer {
                if (it == null) {
                    notArrived()
                    binding.billOfLadingDescription.text = "not available"
                } else if (it.complete!!){
                    waypointCaptured()
                    binding.billOfLadingDescription.text = it.toString()
                }
                else {
                    if(it.loadingStarted == null){
                        loadingNotStarted()
                        binding.billOfLadingDescription.text = it.toString()
                    } else if (it.loadingEnded == null) {
                        loadingNotEnded()
                        binding.billOfLadingDescription.text = it.toString()
                    } else {
                        loadingEnded()
                        binding.billOfLadingDescription.text = it.toString()
                    }
                }
            })

        binding.directionsButton.setOnClickListener {
            val parentFragment = parentFragment as HomeFragment
            parentFragment.showDirections(GeoCoordinate(waypoint.latitude, waypoint.longitude))
            dismiss()
        }

        binding.arrivedButton.setOnClickListener {
            val billOfLading = BillOfLading(
                waypoint.seqNum,
                waypoint.owningTripId,
                false,
                null,
                null,
                null,
                "",
                "",
                null,
                null,
                null,
                null,
                null,
                getCurrentDateTimeString()
            )
            updateBillOfLading(billOfLading)
        }

        binding.startLoading.setOnClickListener {
            val alertStartBolBinding = AlertStartBolBinding.inflate(requireActivity().layoutInflater)
            val dialog = AlertDialog.Builder(ContextThemeWrapper(requireActivity(), R.style.AlertDialogTheme))
                .setTitle("Fuel Stick Reading")
                .setView(alertStartBolBinding.root)
                .create()
            val fuelStickReadingBtn = alertStartBolBinding.fuelStickReadingBtn
            fuelStickReadingBtn.setOnClickListener {
                val fuelStickReading = alertStartBolBinding.fuelStickReading
                if(!fuelStickReading.text.toString().equals("")){
                    dialog.dismiss()
                    billOfLading.value.let{
                        it?.let{
                            val billOfLading = BillOfLading(
                                it.wayPointSeqNum,
                                it.tripIdFk,
                                it.complete,
                                it.deliveryTicketNumber,
                                java.lang.Double.parseDouble(fuelStickReading.text.toString()),
                                it.finalMeterReading,
                                it.pickedUpBy,
                                it.comments,
                                it.billOfLadingNumber,
                                getCurrentDateTimeString(),
                                null,
                                null,
                                null,
                                it.arrivedAt
                            )
                            updateBillOfLading(billOfLading)
                        }
                    }
                }
            }
            dialog.show()
        }

        binding.stopLoading.setOnClickListener {
            billOfLading.value.let{
                it?.let{
                    val billOfLading = BillOfLading(
                        it.wayPointSeqNum,
                        it.tripIdFk,
                        false,
                        it.deliveryTicketNumber,
                        it.initialMeterReading,
                        it.finalMeterReading,
                        it.pickedUpBy,
                        it.comments,
                        it.billOfLadingNumber,
                        it.loadingStarted,
                        getCurrentDateTimeString(),
                        null,
                        null,
                        it.arrivedAt
                    )
                    updateBillOfLading(billOfLading)
                }
            }
        }

        binding.captureButton.setOnClickListener {
            CaptureBolDialog.newInstance(waypoint).show(childFragmentManager, "captureBolFormDialog")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDateTimeString(): String{
        val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        val currentDateandTime: String = sdf.format(Date())
        return currentDateandTime
    }

    private fun updateBillOfLading(billOfLading: BillOfLading){
        viewLifecycleOwner.lifecycleScope.launch {
            tripRepository.insertBillOfLading(billOfLading)
        }
    }

    private fun notArrived() {
        binding.arrivedButton.visibility = View.VISIBLE
        binding.startLoading.visibility = View.GONE
        binding.stopLoading.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
    }

    private fun waypointCaptured() {
        binding.arrivedButton.visibility = View.GONE
        binding.startLoading.visibility = View.GONE
        binding.stopLoading.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
    }

    private fun loadingNotStarted() {
        binding.arrivedButton.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.startLoading.visibility = View.VISIBLE
        binding.stopLoading.visibility = View.VISIBLE
        binding.stopLoading.isEnabled = false
        binding.startLoading.isEnabled = true
        binding.startLoading.alpha = 1.0f
        binding.stopLoading.alpha = 0.5f
    }

    private fun loadingNotEnded() {
        binding.arrivedButton.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.startLoading.visibility = View.VISIBLE
        binding.stopLoading.visibility = View.VISIBLE
        binding.stopLoading.isEnabled = true
        binding.startLoading.isEnabled = false
        binding.startLoading.alpha = 0.5f
        binding.stopLoading.alpha = 1.0f
    }

    private fun loadingEnded() {
        binding.captureButton.visibility = View.VISIBLE
        binding.arrivedButton.visibility = View.GONE
        binding.startLoading.visibility = View.GONE
        binding.stopLoading.visibility = View.GONE

    }

    override fun onDestroyView() {
        if(dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap, signatureBitmap: Bitmap) {
        (parentFragment as HomeFragment).saveForm(billOfLading, bolBitmap, signatureBitmap)
    }

    companion object {
        fun newInstance(waypoint: WayPoint): WaypointDetailDialog {
            return WaypointDetailDialog(waypoint)
        }
    }
}
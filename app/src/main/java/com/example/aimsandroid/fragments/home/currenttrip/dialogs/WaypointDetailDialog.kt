package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.databinding.AlertStartBolBinding
import com.example.aimsandroid.databinding.DialogWaypointDetailsBinding
import com.example.aimsandroid.fragments.home.HomeFragment
import com.example.aimsandroid.fragments.home.HomeFragmentDirections
import com.example.aimsandroid.repository.TripRepository
import com.example.aimsandroid.utils.FileLoaderListener
import com.example.aimsandroid.utils.OnSaveListener
import com.example.aimsandroid.utils.TripStatusCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.here.android.mpa.common.GeoCoordinate
import getCurrentDateTimeString
import getFuel
import getFullAddress
import getReqQty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WaypointDetailDialog(private val waypoint: WayPoint): DialogFragment() {

    private lateinit var tripRepository: TripRepository
    private lateinit var binding: DialogWaypointDetailsBinding

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
        //TODO decouple into viewmodel
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DialogWaypointDetailsBinding.inflate(inflater)
        binding.waypoint = waypoint
        tripRepository = TripRepository(requireActivity().application)
        binding.pickUpFormLayout.visibility = View.GONE
        binding.deliveryFormLayout.visibility = View.GONE
        binding.fuelQuantity.text = getReqQty(waypoint)
        binding.fuelType.text = getFuel(waypoint.productDesc)
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val trip = tripRepository.getTripByTripId(waypoint.owningTripId)
                withContext(Dispatchers.Main){
                    binding.tripName.text = trip.tripName
                    binding.truck.text = trip.truckDesc
                    binding.trailer.text = trip.trailerDesc
                }
            }
        }
        binding.closeButton.setOnClickListener {
            dismiss()
        }
        binding.address.text =getFullAddress(waypoint)
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

            val billOfLading: LiveData<BillOfLading> = tripRepository.getBillOfLading(waypoint.seqNum, waypoint.owningTripId)
            billOfLading.observe(viewLifecycleOwner, Observer {
                if (it == null) {
                    notArrived()
                } else if (it.complete!!){
                    waypointCaptured(it)
                }
                else {
                    if(it.loadingStarted == null){
                        loadingNotStarted()
                    } else if (it.loadingEnded == null) {
                        loadingNotEnded()
                    } else {
                        loadingEnded()
                    }
                }
            })

        binding.directionsButton.setOnClickListener {
            val parentFragment = parentFragment as HomeFragment
            parentFragment.showDirections(GeoCoordinate(waypoint.latitude, waypoint.longitude))
            dismiss()
        }

        binding.arrivedButton.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Confirm?")
            .setMessage("Have you arrived at ${waypoint.destinationName}?")
            .setNegativeButton(
                "No"
            ) { dialoginterface, i ->
               dialoginterface.dismiss()
            }
            .setPositiveButton(
                "Yes"
            ) { dialoginterface, i ->
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
                    waypoint.productDesc?.trim(),
                    null,
                    null,
                    getCurrentDateTimeString(),
                    false,
                    waypoint.sourceId,
                    waypoint.siteId
                )
                updateBillOfLading(billOfLading)
                if(waypoint.waypointTypeDescription == "Source"){
                    onTripEvent(waypoint.owningTripId, TripStatusCode.ARRIVE_AT_SOURCE)
                } else {
                    onTripEvent(waypoint.owningTripId, TripStatusCode.ARRIVE_AT_SITE)
                }
            }.create().show()
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
                                it.product,
                                null,
                                null,
                                it.arrivedAt,
                                false,
                                waypoint.sourceId,
                                waypoint.siteId
                            )
                            updateBillOfLading(billOfLading)
                        }
                    }
                }
            }
            dialog.show()
        }

        binding.stopLoading.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Confirm?")
                .setNegativeButton(
                    "No"
                ) { dialoginterface, i ->
                    dialoginterface.dismiss()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialoginterface, i ->
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
                                it.product,
                                null,
                                null,
                                it.arrivedAt,
                                false,
                                waypoint.sourceId,
                                waypoint.siteId
                            )
                            updateBillOfLading(billOfLading)
                        }
                    }
                }.create().show()
        }

        binding.editFormButton.setOnClickListener {
            val editBolDialog = EditBolDialog.newInstance(waypoint)
            editBolDialog.show(childFragmentManager, "editBolDialog")
        }

        binding.captureButton.setOnClickListener {
            CaptureBolDialog.newInstance(waypoint).show(childFragmentManager, "captureBolFormDialog")
        }
        getSignatureUri()
        getBolUri()
    }

    private fun updateBillOfLading(billOfLading: BillOfLading){
        viewLifecycleOwner.lifecycleScope.launch {
            tripRepository.insertBillOfLading(billOfLading)
        }
    }

    private fun notArrived() {
        val prefs = requireActivity().application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        // force only next waypoint to be used
        if(prefs.getLong("nextWaypointSeqNumber", 0L) == waypoint.seqNum) {
            binding.arrivedButton.visibility = View.VISIBLE
        } else {
            binding.arrivedButton.visibility = View.GONE
        }
        binding.editFormButton.visibility = View.GONE
        binding.startLoading.visibility = View.GONE
        binding.stopLoading.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.billOfLadingTitle.visibility = View.GONE
        binding.deliveryFormLayout.visibility = View.GONE
        binding.pickUpFormLayout.visibility = View.GONE
    }

    private fun waypointCaptured(billOfLading: BillOfLading) {
        binding.editFormButton.visibility = View.VISIBLE
        binding.arrivedButton.visibility = View.GONE
        binding.startLoading.visibility = View.GONE
        binding.stopLoading.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        resolveFormView(billOfLading)
    }

    private fun loadingNotStarted() {
        binding.editFormButton.visibility = View.GONE
        binding.arrivedButton.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.startLoading.visibility = View.VISIBLE
        binding.stopLoading.visibility = View.VISIBLE
        binding.stopLoading.isEnabled = false
        binding.startLoading.isEnabled = true
        binding.startLoading.alpha = 1.0f
        binding.stopLoading.alpha = 0.5f
        binding.billOfLadingTitle.visibility = View.GONE
        binding.deliveryFormLayout.visibility = View.GONE
        binding.pickUpFormLayout.visibility = View.GONE
    }

    private fun loadingNotEnded() {
        binding.editFormButton.visibility = View.GONE
        binding.arrivedButton.visibility = View.GONE
        binding.captureButton.visibility = View.GONE
        binding.startLoading.visibility = View.VISIBLE
        binding.stopLoading.visibility = View.VISIBLE
        binding.stopLoading.isEnabled = true
        binding.startLoading.isEnabled = false
        binding.startLoading.alpha = 0.5f
        binding.stopLoading.alpha = 1.0f
        binding.billOfLadingTitle.visibility = View.GONE
        binding.deliveryFormLayout.visibility = View.GONE
        binding.pickUpFormLayout.visibility = View.GONE
    }

    private fun loadingEnded() {
        binding.editFormButton.visibility = View.GONE
        binding.captureButton.visibility = View.VISIBLE
        binding.arrivedButton.visibility = View.GONE
        binding.startLoading.visibility = View.GONE
        binding.stopLoading.visibility = View.GONE
        binding.billOfLadingTitle.visibility = View.GONE
        binding.deliveryFormLayout.visibility = View.GONE
        binding.pickUpFormLayout.visibility = View.GONE

    }

    private fun resolveFormView(billOfLading: BillOfLading) {
        if(waypoint.waypointTypeDescription=="Source"){
            binding.billOfLadingTitle.visibility = View.VISIBLE
            binding.deliveryFormLayout.visibility = View.GONE
            binding.pickUpFormLayout.visibility = View.VISIBLE
            resolvePickupFormView(billOfLading)
        } else {
            binding.billOfLadingTitle.visibility = View.VISIBLE
            binding.deliveryFormLayout.visibility = View.VISIBLE
            binding.pickUpFormLayout.visibility = View.GONE
            resolveDeliveryFormView(billOfLading)
        }
    }

    private fun resolveDeliveryFormView(billOfLading: BillOfLading) {
        deliveryFormNonEditable()
        binding.deliveryForm.initialFuelStickReading.setText(billOfLading.initialMeterReading.toString())
        binding.deliveryForm.finalFuelStickReading.setText(billOfLading.finalMeterReading.toString())
        binding.deliveryForm.productDropped.setText(billOfLading.product)
        binding.deliveryForm.grossQuantity.setText(billOfLading.grossQuantity.toString())
        binding.deliveryForm.netQuantity.setText(billOfLading.netQuantity.toString())
        binding.deliveryForm.deliveryTicketNumber.setText(billOfLading.deliveryTicketNumber.toString())
        binding.deliveryForm.billOfLadingNumber.setText(billOfLading.billOfLadingNumber.toString())
        binding.deliveryForm.pickedUpBy.setText(billOfLading.pickedUpBy.toString())
        binding.deliveryForm.comment.setText(billOfLading.comments.toString())
        binding.deliveryForm.deliveryStarted.setText(billOfLading.loadingStarted.toString())
        binding.deliveryForm.deliveryEnded.setText(billOfLading.loadingEnded.toString())
    }

    private fun resolvePickupFormView(billOfLading: BillOfLading) {
        pickUpFormNonEditable()
        binding.pickUpForm.initialFuelStickReading.setText(billOfLading.initialMeterReading.toString())
        binding.pickUpForm.finalFuelStickReading.setText(billOfLading.finalMeterReading.toString())
        binding.pickUpForm.productPickedUp.setText(billOfLading.product)
        binding.pickUpForm.grossQuantity.setText(billOfLading.grossQuantity.toString())
        binding.pickUpForm.netQuantity.setText(billOfLading.netQuantity.toString())
        binding.pickUpForm.pickupTicketNumber.setText(billOfLading.deliveryTicketNumber.toString())
        binding.pickUpForm.billOfLadingNumber.setText(billOfLading.billOfLadingNumber.toString())
        binding.pickUpForm.pickedUpBy.setText(billOfLading.pickedUpBy.toString())
        binding.pickUpForm.comment.setText(billOfLading.comments.toString())
        binding.pickUpForm.pickupStarted.setText(billOfLading.loadingStarted.toString())
        binding.pickUpForm.pickupEnded.setText(billOfLading.loadingEnded.toString())
    }


    private fun deliveryFormNonEditable(){
        binding.deliveryForm.initialFuelStickReading.isEnabled = false
        binding.deliveryForm.finalFuelStickReading.isEnabled = false
        binding.deliveryForm.productDropped.isEnabled = false
        binding.deliveryForm.grossQuantity.isEnabled = false
        binding.deliveryForm.netQuantity.isEnabled = false
        binding.deliveryForm.deliveryTicketNumber.isEnabled = false
        binding.deliveryForm.billOfLadingNumber.isEnabled = false
        binding.deliveryForm.pickedUpBy.isEnabled = false
        binding.deliveryForm.comment.isEnabled = false
        binding.deliveryForm.saveButton.visibility = View.GONE
        binding.deliveryForm.captureSignatureButton.visibility = View.GONE
        binding.deliveryForm.scanBOL.visibility = View.GONE
        binding.deliveryForm.initialFuelStickReadingLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.finalFuelStickLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.productDroppedLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.grossQuantityLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.netQuantityLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.deliveryTicketLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.billOfLadingNumberLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.deliveryForm.pickedUpByLayout.endIconMode = TextInputLayout.END_ICON_NONE
    }

    private fun pickUpFormNonEditable() {
        binding.pickUpForm.initialFuelStickReading.isEnabled = false
        binding.pickUpForm.finalFuelStickReading.isEnabled = false
        binding.pickUpForm.productPickedUp.isEnabled = false
        binding.pickUpForm.grossQuantity.isEnabled = false
        binding.pickUpForm.netQuantity.isEnabled = false
        binding.pickUpForm.pickupTicketNumber.isEnabled = false
        binding.pickUpForm.billOfLadingNumber.isEnabled = false
        binding.pickUpForm.pickedUpBy.isEnabled = false
        binding.pickUpForm.comment.isEnabled = false
        binding.pickUpForm.saveButton.visibility = View.GONE
        binding.pickUpForm.captureSignatureButton.visibility = View.GONE
        binding.pickUpForm.scanBOL.visibility = View.GONE
        binding.pickUpForm.initialFuelStickReadingLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.finalFuelStickLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.productPickedUpLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.grossQuantityLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.netQuantityLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.deliveryTicketLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.billOfLadingNumberLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.pickUpForm.pickedUpByLayout.endIconMode = TextInputLayout.END_ICON_NONE
    }

    override fun onDestroyView() {
        if(dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap?, signatureBitmap: Bitmap?, onSaveListener: OnSaveListener) {
        (parentFragment as HomeFragment).saveForm(billOfLading, bolBitmap, signatureBitmap, onSaveListener)
    }

    fun getSignatureUri(){
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                (parentFragment as HomeFragment).getSignatureUri(waypoint.owningTripId, waypoint.seqNum, object: FileLoaderListener{
                    override fun onSuccess(uri: Uri) {
                        loadSignature(uri)
                    }
                    override fun onError(error: String) {
                        Toast.makeText(requireContext(), "Could not load signature!", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    fun getBolUri(){
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                (parentFragment as HomeFragment).getBolUri(waypoint.owningTripId, waypoint.seqNum, object: FileLoaderListener{
                    override fun onSuccess(uri: Uri) {
                        loadBol(uri)
                    }
                    override fun onError(error: String) {
                        Toast.makeText(requireContext(), "Could not load Bill of Lading Image!", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }

    fun loadBol(uri: Uri) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main){
                if(waypoint.waypointTypeDescription == "Source") {
                    Glide.with(requireContext())
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.pickUpForm.bolView)
                    binding.pickUpForm.bolView.visibility = View.VISIBLE
                } else {
                    Glide.with(requireContext())
                        .load(uri)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(binding.deliveryForm.bolView)
                    binding.deliveryForm.bolView.visibility = View.VISIBLE
                }
            }
        }
    }


    fun loadSignature(uri: Uri){
        lifecycleScope.launch {
            withContext(Dispatchers.Main){
                if(waypoint.waypointTypeDescription == "Source") {
                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.pickUpForm.signatureView)
                    binding.pickUpForm.signatureView.visibility = View.VISIBLE
                } else {
                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.deliveryForm.signatureView)
                    binding.deliveryForm.signatureView.visibility = View.VISIBLE
                }
            }
        }
    }

    fun refreshRecyclerView() {
        (parentFragment as HomeFragment).refreshRecyclerView()
    }

    fun onTripCompleted() {
        Toast.makeText(requireContext(), "Trip Completed!", Toast.LENGTH_LONG).show()
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToTripsFragment())
        dismiss()
    }

    fun onTripEvent(tripId: Long, tripStatusCode: TripStatusCode){
        (parentFragment as HomeFragment).onTripEvent(tripId, tripStatusCode)
    }

    companion object {
        fun newInstance(waypoint: WayPoint): WaypointDetailDialog {
            return WaypointDetailDialog(waypoint)
        }
    }
}
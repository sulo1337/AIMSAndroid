package com.example.aimsandroid.fragments.trips.dialogs

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.databinding.DialogWaypointDetailsBinding
import com.example.aimsandroid.fragments.home.HomeFragment
import com.example.aimsandroid.fragments.trips.TripsFragment
import com.example.aimsandroid.repository.TripRepository
import com.example.aimsandroid.utils.FileLoaderListener
import com.example.aimsandroid.utils.OnSaveListener
import com.google.android.material.textfield.TextInputLayout
import getFullAddress
import getLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WaypointDetailDialog(private val waypoint: WayPoint): DialogFragment() {
    private lateinit var tripRepository: TripRepository
    private lateinit var binding: DialogWaypointDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        root.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.BLACK))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DialogWaypointDetailsBinding.inflate(inflater)
        binding.deliveryFormLayout.visibility = View.GONE
        binding.pickUpFormLayout.visibility = View.GONE
        tripRepository = TripRepository(getDatabase(requireActivity().application), requireActivity().application.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.waypoint = waypoint
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
        binding.billOfLadingTitle.visibility = View.GONE
        binding.buttonContainer.visibility = View.GONE
        binding.arrivedButton.visibility = View.GONE
        binding.directionsButton.visibility = View.GONE
        binding.editFormButton.visibility = View.GONE
        binding.address.text = getFullAddress(waypoint)

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
        val billOfLading: LiveData<BillOfLading> = tripRepository.getBillOfLading(waypoint.seqNum, waypoint.owningTripId)
        billOfLading.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                if(it.complete == true) {
                    resolveFormView(it)
                }
            }
        })

        binding.editFormButton.setOnClickListener {
            val editBolDialog = EditBolDialog.newInstance(waypoint)
            editBolDialog.show(childFragmentManager, "editBolDialog")
        }
        getBolUri()
        getSignatureUri()
    }

    private fun resolveFormView(billOfLading: BillOfLading) {
        binding.editFormButton.visibility = View.VISIBLE
        binding.billOfLadingTitle.visibility = View.VISIBLE
        if(waypoint.waypointTypeDescription=="Source"){
            binding.deliveryFormLayout.visibility = View.GONE
            binding.pickUpFormLayout.visibility = View.VISIBLE
            resolvePickupFormView(billOfLading)
        } else {
            binding.deliveryFormLayout.visibility = View.VISIBLE
            binding.pickUpFormLayout.visibility = View.GONE
            resolveDeliveryFormView(billOfLading)
        }
    }

    private fun resolveDeliveryFormView(billOfLading: BillOfLading) {
        deliveryFormNonEditable()
        binding.deliveryForm.initialFuelStickReading.setText(billOfLading.initialMeterReading.toString())
        binding.deliveryForm.finalFuelStickReading.setText(billOfLading.finalMeterReading.toString())
        binding.deliveryForm.productDropped.setText("Implement this")
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
        binding.pickUpForm.productPickedUp.setText("Implement this")
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
        binding.deliveryForm.commentLayout.endIconMode = TextInputLayout.END_ICON_NONE
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
        binding.pickUpForm.commentLayout.endIconMode = TextInputLayout.END_ICON_NONE
    }

    override fun onDestroyView() {
        if(dialog != null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap?, onSaveListener: OnSaveListener) {
        (parentFragment as TripsDetailDialog).saveForm(billOfLading, bolBitmap, onSaveListener)
    }

    companion object {
        fun newInstance(waypoint: WayPoint): WaypointDetailDialog{
            return WaypointDetailDialog(waypoint)
        }
    }

    fun getSignatureUri(){
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                (parentFragment as TripsDetailDialog).getSignatureUri(waypoint.owningTripId, waypoint.seqNum, object:
                    FileLoaderListener {
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
                (parentFragment as TripsDetailDialog).getBolUri(waypoint.owningTripId, waypoint.seqNum, object:
                    FileLoaderListener {
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
}
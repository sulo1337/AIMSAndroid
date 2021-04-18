package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import RotateBitmap
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Binder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.databinding.FormContainerBinding
import com.example.aimsandroid.databinding.FormDeliveryBinding
import com.example.aimsandroid.repository.TripRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.form_capture_signature.*
import kotlinx.android.synthetic.main.form_container.view.*
import kotlinx.android.synthetic.main.form_delivery.view.*
import java.lang.Long.parseLong

class CaptureBolDialog(private val waypoint: WayPoint) : DialogFragment() {
    private lateinit var binding: FormContainerBinding
    private lateinit var tripRepository: TripRepository
    private lateinit var billOfLading: LiveData<BillOfLading>
    private var signatureBitmap: Bitmap? = null
    private var bolBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    companion object {
        fun newInstance(waypoint: WayPoint): CaptureBolDialog {
            return CaptureBolDialog(waypoint)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FormContainerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripRepository = TripRepository(getDatabase(requireActivity().application))
        billOfLading = tripRepository.getBillOfLading(waypoint.seqNum, waypoint.owningTripId)

        binding.destInfo.text = waypoint.destinationName
        binding.addrInfo.text = waypoint.address1.trim() + ", " + waypoint.city.trim() + ", " + waypoint.state.trim() + " " + waypoint.postalCode
        if(waypoint.waypointTypeDescription.equals("Source")){
            binding.deliveryFormLayout.visibility = View.GONE
        } else {
            binding.pickUpFormLayout.visibility = View.GONE
            billOfLading.observe(viewLifecycleOwner, Observer {
                binding.deliveryForm.initialFuelStickReading.setText(it.initialMeterReading.toString())
                binding.deliveryForm.productDropped.setText(waypoint.productDesc)
                binding.deliveryForm.deliveryStarted.setText(it.loadingStarted)
                binding.deliveryForm.deliveryEnded.setText(it.loadingEnded)
            })
            binding.deliveryForm.captureSignatureButton.setOnClickListener {
                val captureSignatureDialog = CaptureSignatureDialog.newInstance()
                captureSignatureDialog.show(childFragmentManager, "captureSignatureDialog")
            }
            binding.deliveryForm.scanBOL.setOnClickListener {
                val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 1888)
            }
            binding.deliveryForm.saveButton.setOnClickListener {
                validateDeliveryForm()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1888 && resultCode == Activity.RESULT_OK) {
            val bolBitmap = data?.extras?.get("data") as Bitmap
            binding.deliveryForm.bolView.visibility = View.VISIBLE
            binding.deliveryForm.bolView.setImageBitmap(bolBitmap)
            this.bolBitmap = bolBitmap
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    override fun onDestroyView() {
        if(dialog != null && retainInstance){
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    fun saveSignature(signatureBitmap: Bitmap) {
        binding.deliveryForm.signatureView.visibility = View.VISIBLE
        binding.deliveryForm.signatureView.setImageBitmap(RotateBitmap(signatureBitmap, 270.0f))
        this.signatureBitmap = signatureBitmap
    }

    fun saveForm() {
        val billOfLading = BillOfLading(
            waypoint.seqNum,
            waypoint.owningTripId,
            true,
            parseLong(binding.deliveryForm.deliveryTicketNumber.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.initialFuelStickReading.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.finalFuelStickReading.text.toString()),
            binding.deliveryForm.pickedUpBy.text.toString(),
            binding.deliveryForm.comment.text.toString(),
            parseLong(binding.deliveryForm.billOfLadingNumber.text.toString()),
            binding.deliveryForm.deliveryStarted.text.toString(),
            binding.deliveryForm.deliveryEnded.text.toString(),
            java.lang.Double.parseDouble(binding.deliveryForm.grossQuantity.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.netQuantity.text.toString()),
            this.billOfLading.value!!.arrivedAt
        )
        (parentFragment as WaypointDetailDialog).saveForm(billOfLading, bolBitmap!!, signatureBitmap!!)
        dismiss()
    }

    fun validateDeliveryForm() {
        var valid = true

        if(binding.deliveryForm.initialFuelStickReading.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.initialFuelStickReading.error = "Required"
        }

        if(binding.deliveryForm.finalFuelStickReading.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.finalFuelStickReading.error = "Required"
        }

        if(binding.deliveryForm.productDropped.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.productDropped.error = "Required"
        }

        if(binding.deliveryForm.deliveryStarted.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.deliveryStarted.error = "Required"
        }

        if(binding.deliveryForm.deliveryEnded.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.deliveryEnded.error = "Required"
        }

        if(binding.deliveryForm.grossQuantity.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.grossQuantity.error = "Required"
        }

        if(binding.deliveryForm.netQuantity.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.netQuantity.error = "Required"
        }

        if(binding.deliveryForm.deliveryTicketNumber.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.deliveryTicketNumber.error = "Required"
        }

        if(binding.deliveryForm.billOfLadingNumber.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.billOfLadingNumber.error = "Required"
        }

        if(binding.deliveryForm.pickedUpBy.text.toString().isEmpty()){
            valid = false
            binding.deliveryForm.pickedUpBy.error = "Required"
        }

        if(signatureBitmap == null) {
            valid = false
            Snackbar.make(requireView(), "Signature not captured", Snackbar.LENGTH_SHORT).show()
        }

        if(bolBitmap == null) {
            valid = false
            Snackbar.make(requireView(), "Bill of lading not scanned", Snackbar.LENGTH_SHORT).show()
        }

        if(valid) {
            saveForm()
        }
    }
}
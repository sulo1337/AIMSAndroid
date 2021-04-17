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
import kotlinx.android.synthetic.main.form_capture_signature.*
import kotlinx.android.synthetic.main.form_container.view.*
import kotlinx.android.synthetic.main.form_delivery.view.*

class CaptureBolDialog(private val waypoint: WayPoint) : DialogFragment() {
    private lateinit var binding: FormContainerBinding
    private lateinit var tripRepository: TripRepository
    private lateinit var billOfLading: LiveData<BillOfLading>
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1888 && resultCode == Activity.RESULT_OK) {
            val bolBitmap = data?.extras?.get("data") as Bitmap
            binding.deliveryForm.bolView.visibility = View.VISIBLE
            binding.deliveryForm.bolView.setImageBitmap(bolBitmap)
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
    }
}
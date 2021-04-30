package com.example.aimsandroid.fragments.trips.dialogs

import RotateBitmap
import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.database.getDatabase
import com.example.aimsandroid.databinding.FormContainerBinding
import com.example.aimsandroid.fragments.home.currenttrip.dialogs.CaptureBolDialog
import com.example.aimsandroid.fragments.home.currenttrip.dialogs.CaptureSignatureDialog
import com.example.aimsandroid.repository.TripRepository
import com.example.aimsandroid.utils.OnSaveListener
import com.example.aimsandroid.utils.getDeliveryFormSummary
import com.example.aimsandroid.utils.getPickUpFormSummary
import com.example.aimsandroid.utils.validatePickUpForm
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import getFullAddress
import getLoader
import java.io.File
import java.lang.Exception
import java.lang.Long
import java.lang.StringBuilder
import kotlin.math.sign

class EditBolDialog(private val waypoint: WayPoint): DialogFragment() {
    private lateinit var binding: FormContainerBinding
    private lateinit var tripRepository: TripRepository
    private lateinit var billOfLading: LiveData<BillOfLading>
    private var signatureBitmap: Bitmap? = null
    private var bolBitmap: Bitmap? = null
    private var tempBolPath: String? = null
    private var photoUri: Uri? = null
    private var photoFile: File? = null
    private lateinit var loader: AlertDialog
    companion object {
        fun newInstance(waypoint: WayPoint): EditBolDialog {
            return EditBolDialog(waypoint)
        }
    }

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
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FormContainerBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tripRepository = TripRepository(requireActivity().application)
        billOfLading = tripRepository.getBillOfLading(waypoint.seqNum, waypoint.owningTripId)

        binding.destInfo.text = waypoint.destinationName
        binding.addrInfo.text = getFullAddress(waypoint)
        binding.pickUpForm.scanBOL.text = "Update Bill of Lading"
        binding.deliveryForm.scanBOL.text = "Update Bill of Lading"
        if(waypoint.waypointTypeDescription.equals("Source")){
            binding.deliveryFormLayout.visibility = View.GONE
            billOfLading.observe(viewLifecycleOwner, Observer {
                binding.pickUpForm.captureSignatureButton.visibility = View.GONE
                binding.pickUpForm.initialFuelStickReading.setText(it.initialMeterReading.toString())
                binding.pickUpForm.finalFuelStickReading.setText(it.finalMeterReading.toString())
                binding.pickUpForm.productPickedUp.setText("Implement this")
                binding.pickUpForm.grossQuantity.setText(it.grossQuantity.toString())
                binding.pickUpForm.netQuantity.setText(it.netQuantity.toString())
                binding.pickUpForm.pickupTicketNumber.setText(it.deliveryTicketNumber.toString())
                binding.pickUpForm.billOfLadingNumber.setText(it.billOfLadingNumber.toString())
                binding.pickUpForm.pickedUpBy.setText(it.pickedUpBy.toString())
                binding.pickUpForm.comment.setText(it.comments.toString())
                binding.pickUpForm.pickupStarted.setText(it.loadingStarted.toString())
                binding.pickUpForm.pickupEnded.setText(it.loadingEnded.toString())
            })
            binding.pickUpForm.scanBOL.setOnClickListener {
                startCameraActivity()
            }
            binding.pickUpForm.saveButton.setOnClickListener {
                validatePickupForm()
            }
        } else {
            binding.pickUpFormLayout.visibility = View.GONE
            billOfLading.observe(viewLifecycleOwner, Observer {
                binding.deliveryForm.captureSignatureButton.visibility = View.GONE
                binding.deliveryForm.initialFuelStickReading.setText(it.initialMeterReading.toString())
                binding.deliveryForm.finalFuelStickReading.setText(it.finalMeterReading.toString())
                binding.deliveryForm.productDropped.setText("Implement this")
                binding.deliveryForm.grossQuantity.setText(it.grossQuantity.toString())
                binding.deliveryForm.netQuantity.setText(it.netQuantity.toString())
                binding.deliveryForm.deliveryTicketNumber.setText(it.deliveryTicketNumber.toString())
                binding.deliveryForm.billOfLadingNumber.setText(it.billOfLadingNumber.toString())
                binding.deliveryForm.pickedUpBy.setText(it.pickedUpBy.toString())
                binding.deliveryForm.comment.setText(it.comments.toString())
                binding.deliveryForm.deliveryStarted.setText(it.loadingStarted.toString())
                binding.deliveryForm.deliveryEnded.setText(it.loadingEnded.toString())
            })
            binding.deliveryForm.captureSignatureButton.setOnClickListener {
                val captureSignatureDialog = CaptureSignatureDialog.newInstance()
                captureSignatureDialog.show(childFragmentManager, "captureSignatureDialog")
            }
            binding.deliveryForm.scanBOL.setOnClickListener {
                startCameraActivity()
            }
            binding.deliveryForm.saveButton.setOnClickListener {
                validateDeliveryForm()
            }
        }
        loader = getLoader(requireActivity())
    }

    private fun startCameraActivity() {
        TedPermission.with(requireContext())
            .setPermissionListener(object : PermissionListener{
                override fun onPermissionGranted() {
                    val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        photoFile = createTempImageFile()
                        photoUri = FileProvider.getUriForFile(requireContext(), "com.example.aimsandroid", photoFile!!)
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startActivityForResult(cameraIntent, 1888)
                    } catch (e: Exception) {
                        Log.i("aimsDebug_fh", e.toString())
                    }
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(requireContext(), "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("Please allow camera permission")
            .setGotoSettingButtonText("Open App Settings")
            .setPermissions(
                Manifest.permission.CAMERA
            )
            .check()
    }

    private fun createTempImageFile(): File {
        val tempFileName = waypoint.owningTripId.toString() + "_" + waypoint.seqNum.toString()
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile(
            tempFileName,
            ".png",
            storageDir
        )
        tempFile.deleteOnExit()
        tempBolPath = tempFile.absolutePath
        return tempFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1888 && resultCode == Activity.RESULT_OK){
            try {
                bolBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                if(waypoint.waypointTypeDescription == "Source") {
                    Glide.with(requireContext())
                        .load(photoUri)
                        .into(binding.pickUpForm.bolView)
                    binding.pickUpForm.bolView.visibility = View.VISIBLE
                } else {
                    Glide.with(requireContext())
                        .load(photoUri)
                        .into(binding.deliveryForm.bolView)
                    binding.deliveryForm.bolView.visibility = View.VISIBLE
                }
            } catch (e: Exception){
                Log.i("aimsDebug_fh", e.toString())
            }
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

    private fun generateDeliveryBillOfLading(): BillOfLading{
        return BillOfLading(
            waypoint.seqNum,
            waypoint.owningTripId,
            true,
            Long.parseLong(binding.deliveryForm.deliveryTicketNumber.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.initialFuelStickReading.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.finalFuelStickReading.text.toString()),
            binding.deliveryForm.pickedUpBy.text.toString(),
            binding.deliveryForm.comment.text.toString(),
            Long.parseLong(binding.deliveryForm.billOfLadingNumber.text.toString()),
            binding.deliveryForm.deliveryStarted.text.toString(),
            binding.deliveryForm.deliveryEnded.text.toString(),
            binding.deliveryForm.productDropped.text.toString(),
            java.lang.Double.parseDouble(binding.deliveryForm.grossQuantity.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.netQuantity.text.toString()),
            this.billOfLading.value!!.arrivedAt,
            false
        )
    }

    private fun generatePickUpBillOfLading(): BillOfLading{
        return  BillOfLading(
            waypoint.seqNum,
            waypoint.owningTripId,
            true,
            Long.parseLong(binding.pickUpForm.pickupTicketNumber.text.toString()),
            java.lang.Double.parseDouble(binding.pickUpForm.initialFuelStickReading.text.toString()),
            java.lang.Double.parseDouble(binding.pickUpForm.finalFuelStickReading.text.toString()),
            binding.pickUpForm.pickedUpBy.text.toString(),
            binding.pickUpForm.comment.text.toString(),
            Long.parseLong(binding.pickUpForm.billOfLadingNumber.text.toString()),
            binding.pickUpForm.pickupStarted.text.toString(),
            binding.pickUpForm.pickupEnded.text.toString(),
            binding.pickUpForm.productPickedUp.text.toString(),
            java.lang.Double.parseDouble(binding.pickUpForm.grossQuantity.text.toString()),
            java.lang.Double.parseDouble(binding.pickUpForm.netQuantity.text.toString()),
            this.billOfLading.value!!.arrivedAt,
            false
        )
    }

    fun saveDeliveryForm() {
        (parentFragment as WaypointDetailDialog).saveForm(generateDeliveryBillOfLading(), bolBitmap, object : OnSaveListener{
            override fun onSave() {
                Handler(Looper.getMainLooper()).postDelayed({
                    loader.dismiss()
                    dismiss()
                    Toast.makeText(requireActivity(), "Successfully Updated", Toast.LENGTH_LONG).show()
                }, 3000)
                (parentFragment as WaypointDetailDialog).getBolUri()
            }
            override fun onSaving() {
                loader.show()
            }

            override fun onTripCompleted() {

            }
        })

    }

    fun savePickupForm() {
        (parentFragment as WaypointDetailDialog).saveForm(generatePickUpBillOfLading(), bolBitmap, object : OnSaveListener{
            override fun onSave() {
                Handler(Looper.getMainLooper()).postDelayed({
                    loader.dismiss()
                    dismiss()
                    Toast.makeText(requireActivity(), "Successfully Updated", Toast.LENGTH_LONG).show()
                }, 1000)
                (parentFragment as WaypointDetailDialog).getBolUri()
            }
            override fun onSaving(){
                loader.show()
            }
            override fun onTripCompleted() {

            }
        })
    }

    fun validateDeliveryForm() {
        var valid = com.example.aimsandroid.utils.validateDeliveryForm(binding.deliveryForm)

        if(valid) {
            val sb: StringBuilder = StringBuilder("Is the data correct?\n\n")
            sb.append(getDeliveryFormSummary(binding.deliveryForm))
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Confirm?")
                .setMessage(sb.toString())
                .setNegativeButton(
                    "No"
                ) { dialoginterface, i ->
                    dialoginterface.dismiss()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialoginterface, i ->
                    saveDeliveryForm()
                }.create().show()
        }
    }

    fun validatePickupForm() {
        var valid = validatePickUpForm(binding.pickUpForm)

        if(valid) {
            val sb: StringBuilder = StringBuilder("Is the data correct?\n\n")
            sb.append(getPickUpFormSummary(binding.pickUpForm))
            AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Confirm?")
                .setMessage(sb.toString())
                .setNegativeButton(
                    "No"
                ) { dialoginterface, i ->
                    dialoginterface.dismiss()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialoginterface, i ->
                    savePickupForm()
                }.create().show()
        }
    }
}
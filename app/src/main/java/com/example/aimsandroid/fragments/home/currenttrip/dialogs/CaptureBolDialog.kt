package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import RotateBitmap
import android.Manifest
import android.app.Activity
import android.app.Dialog
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
import com.example.aimsandroid.repository.TripRepository
import com.example.aimsandroid.utils.*
import com.google.android.material.snackbar.Snackbar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import getFullAddress
import getLoader
import java.io.File
import java.lang.Exception
import java.lang.Long.parseLong
import java.lang.StringBuilder

/*
* Android Dialog fragment to show form view
* @param waypoint: The waypoint for which the form needs to be shown
* */
open class CaptureBolDialog(protected val waypoint: WayPoint) : DialogFragment() {
    protected lateinit var binding: FormContainerBinding
    private lateinit var tripRepository: TripRepository
    protected lateinit var billOfLading: LiveData<BillOfLading>
    private var signatureBitmap: Bitmap? = null
    private var bolBitmap: Bitmap? = null
    private var tempBolPath: String? = null
    private var photoUri: Uri? = null
    private var photoFile: File? = null
    private lateinit var loader: AlertDialog

    //Refer to android onCreateDialog method for documentation
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

    //Refer to android onCreate method for documentation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    //Static object to create a new instance of this dialog
    companion object {
        fun newInstance(waypoint: WayPoint): CaptureBolDialog {
            return CaptureBolDialog(waypoint)
        }
    }

    //Refer to android onCreateView method for documentation
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = FormContainerBinding.inflate(inflater)
        return binding.root
    }

    //Refer to android onViewCreated method for documentation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = getLoader(requireActivity())
        tripRepository = TripRepository(requireActivity().application)
        billOfLading = tripRepository.getBillOfLading(waypoint.seqNum, waypoint.owningTripId)

        binding.destInfo.text = waypoint.destinationName
        binding.addrInfo.text = getFullAddress(waypoint)
        if(waypoint.waypointTypeDescription.equals("Source")){
            binding.deliveryFormLayout.visibility = View.GONE
            billOfLading.observe(viewLifecycleOwner, Observer {
                binding.pickUpForm.initialFuelStickReading.setText(it.initialMeterReading.toString())
                binding.pickUpForm.productPickedUp.setText(waypoint.productDesc)
                binding.pickUpForm.pickupStarted.setText(it.loadingStarted)
                binding.pickUpForm.pickupEnded.setText(it.loadingEnded)
            })
            binding.pickUpForm.captureSignatureButton.setOnClickListener {
                val captureSignatureDialog = CaptureSignatureDialog.newInstance()
                captureSignatureDialog.show(childFragmentManager, "captureSignatureDialog")
            }
            binding.pickUpForm.scanBOL.setOnClickListener {
                startCameraActivity()
            }
            binding.pickUpForm.saveButton.setOnClickListener {
                validatePickupForm()
            }
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
                startCameraActivity()
            }
            binding.deliveryForm.saveButton.setOnClickListener {
                validateDeliveryForm()
            }
        }
    }

    /*
    * This method first checks the permission required to open the camera to scan the Bill of Lading
    * After permission is allowed, it opens the camera to scan the bill of lading
    * */
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

    /*
    * This method creates a temporary file to temporarily store the bill of lading picture in local file system
    * */
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

    /*
    * This method is called when user clicks the camera and confirms the picture
    * It tries to capture the picture information and save it as a local object in this class
    * */
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

    /*
    * Clearing any bundle information stored when this fragment is destroyed
    *  */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    /*
    * Check android's onDestroyView() documentation for retainable dialog
    * */
    override fun onDestroyView() {
        if(dialog != null && retainInstance){
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    /*
    * This method saves the signature that was captured as a local object
    * */
     fun saveSignature(signatureBitmap: Bitmap) {
        binding.deliveryForm.signatureView.visibility = View.VISIBLE
        binding.deliveryForm.signatureView.setImageBitmap(RotateBitmap(signatureBitmap, 270.0f))
        binding.pickUpForm.signatureView.visibility = View.VISIBLE
        binding.pickUpForm.signatureView.setImageBitmap(RotateBitmap(signatureBitmap, 270.0f))
        this.signatureBitmap = RotateBitmap(signatureBitmap, 270.0f)
    }

    /*
    * This object generates bill of lading POJO for site container based on form input data
    * */
    private fun generateDeliveryBillOfLading(): BillOfLading{
        return BillOfLading(
            waypoint.seqNum,
            waypoint.owningTripId,
            true,
            parseLong(binding.deliveryForm.deliveryTicketNumber.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.initialFuelStickReading.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.finalFuelStickReading.text.toString()),
            binding.deliveryForm.pickedUpBy.text.toString(),
            binding.deliveryForm.comment.text.toString(),
            binding.deliveryForm.billOfLadingNumber.text.toString(),
            binding.deliveryForm.deliveryStarted.text.toString(),
            binding.deliveryForm.deliveryEnded.text.toString(),
            binding.deliveryForm.productDropped.text.toString(),
            java.lang.Double.parseDouble(binding.deliveryForm.grossQuantity.text.toString()),
            java.lang.Double.parseDouble(binding.deliveryForm.netQuantity.text.toString()),
            this.billOfLading.value!!.arrivedAt,
            false,
            waypoint.sourceId,
            waypoint.siteId
        )
    }

    /*
    * This object generates bill of lading POJO for source based on form input data
    * */
    private fun generatePickUpBillOfLading(): BillOfLading{
        return  BillOfLading(
            waypoint.seqNum,
            waypoint.owningTripId,
            true,
            parseLong(binding.pickUpForm.pickupTicketNumber.text.toString()),
            java.lang.Double.parseDouble(binding.pickUpForm.initialFuelStickReading.text.toString()),
            java.lang.Double.parseDouble(binding.pickUpForm.finalFuelStickReading.text.toString()),
            binding.pickUpForm.pickedUpBy.text.toString(),
            binding.pickUpForm.comment.text.toString(),
            binding.pickUpForm.billOfLadingNumber.text.toString(),
            binding.pickUpForm.pickupStarted.text.toString(),
            binding.pickUpForm.pickupEnded.text.toString(),
            binding.pickUpForm.productPickedUp.text.toString(),
            java.lang.Double.parseDouble(binding.pickUpForm.grossQuantity.text.toString()),
            java.lang.Double.parseDouble(binding.pickUpForm.netQuantity.text.toString()),
            this.billOfLading.value!!.arrivedAt,
            false,
            waypoint.sourceId,
            waypoint.siteId
        )
    }

    /*
    * This method saves the delivery form into the database, while showing the loader when in progress
    * */
    fun saveDeliveryForm() {
        (parentFragment as WaypointDetailDialog).saveForm(generateDeliveryBillOfLading(), bolBitmap!!, signatureBitmap!!, object : OnSaveListener{
            override fun onSaving() {
                loader.show()
            }

            override fun onSave() {
                Handler(Looper.getMainLooper()).postDelayed({
                    loader.dismiss()
                    Toast.makeText(requireContext(), "Saved Successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                }, 1000)
                (parentFragment as WaypointDetailDialog).onTripEvent(waypoint.owningTripId, TripStatusCode.LEAVE_SITE)
                (parentFragment as WaypointDetailDialog).getBolUri()
                (parentFragment as WaypointDetailDialog).getSignatureUri()
                (parentFragment as WaypointDetailDialog).refreshRecyclerView()
            }

            override fun onTripCompleted() {
                Handler(Looper.getMainLooper()).postDelayed({
                    loader.dismiss()
                    Toast.makeText(requireContext(), "Saved Successfully", Toast.LENGTH_SHORT).show()
                    (parentFragment as WaypointDetailDialog).onTripCompleted()
                    dismiss()
                }, 1000)
            }
        })
    }

    /*
    * This method saves pickup form into the database while showing the loader when in progress
    * */
    fun savePickupForm() {
        (parentFragment as WaypointDetailDialog).saveForm(generatePickUpBillOfLading(), bolBitmap!!, signatureBitmap!!, object : OnSaveListener{
            override fun onSaving() {
                loader.show()
            }

            override fun onSave() {
                Handler(Looper.getMainLooper()).postDelayed({
                    loader.dismiss()
                    Toast.makeText(requireContext(), "Saved Successfully", Toast.LENGTH_SHORT).show()
                    dismiss()
                }, 1000)
                (parentFragment as WaypointDetailDialog).onTripEvent(waypoint.owningTripId, TripStatusCode.LEAVE_SRC)
                (parentFragment as WaypointDetailDialog).getBolUri()
                (parentFragment as WaypointDetailDialog).getSignatureUri()
                (parentFragment as WaypointDetailDialog).refreshRecyclerView()
            }

            override fun onTripCompleted() {
                (parentFragment as WaypointDetailDialog).onTripCompleted()
                dismiss()
            }
        })
    }

    /*
    * A method for validating form input data for site container
    * */
    private fun validateDeliveryForm() {
        var valid = validateDeliveryForm(binding.deliveryForm)

        if(signatureBitmap == null) {
            valid = false
            Snackbar.make(requireView(), "Signature not captured", Snackbar.LENGTH_SHORT).show()
        }

        if(bolBitmap == null) {
            valid = false
            Snackbar.make(requireView(), "Bill of lading not scanned", Snackbar.LENGTH_SHORT).show()
        }

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

    /*
    * A method for validating form input data for source
    * */
    fun validatePickupForm() {
        var valid = validatePickUpForm(binding.pickUpForm)

        if(signatureBitmap == null) {
            valid = false
            Snackbar.make(requireView(), "Signature not captured", Snackbar.LENGTH_SHORT).show()
        }

        if(bolBitmap == null) {
            valid = false
            Snackbar.make(requireView(), "Bill of lading not scanned", Snackbar.LENGTH_SHORT).show()
        }

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
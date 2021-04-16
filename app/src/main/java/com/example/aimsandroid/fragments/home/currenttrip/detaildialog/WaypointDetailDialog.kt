package com.example.aimsandroid.fragments.home.currenttrip.detaildialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                    binding.startLoading.isEnabled = true
                    binding.stopLoading.isEnabled = false
                    binding.startLoading.alpha = 1.0f
                    binding.stopLoading.alpha = 0.5f
                    binding.billOfLadingDescription.text = "Not available!"
                } else if (it.complete!!){
                    binding.startLoading.visibility = View.GONE
                    binding.stopLoading.visibility = View.GONE
                    binding.billOfLadingDescription.text = it.toString()
                }
                else {
                    binding.startLoading.isEnabled = false
                    binding.stopLoading.isEnabled = true
                    binding.startLoading.alpha = 0.5f
                    binding.stopLoading.alpha = 1.0f
                    binding.billOfLadingDescription.text = it.toString()
                }
            })

        binding.directions.setOnClickListener {
            val parentFragment = parentFragment as HomeFragment
            parentFragment.showDirections(GeoCoordinate(waypoint.latitude, waypoint.longitude))
            dismiss()
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
                    val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
                    val currentDateandTime: String = sdf.format(Date())
                    val billOfLading = BillOfLading(
                        waypoint.seqNum,
                        waypoint.owningTripId,
                        false,
                        null,
                        java.lang.Double.parseDouble(fuelStickReading.text.toString()),
                        null,
                        "",
                        "",
                        null,
                        currentDateandTime,
                        ""
                    )
                    viewLifecycleOwner.lifecycleScope.launch {
                        tripRepository.insertBillOfLading(billOfLading)
                    }
                }
            }
            dialog.show()
        }

        binding.stopLoading.setOnClickListener {
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
                    val sdf = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
                    val currentDateandTime: String = sdf.format(Date())
                    billOfLading.value.let{
                        it?.let{
                            val billOfLading = BillOfLading(
                                it.wayPointSeqNum,
                                it.tripIdFk,
                                true,
                                it.deliveryTicketNumber,
                                it.initialMeterReading,
                                java.lang.Double.parseDouble(fuelStickReading.text.toString()),
                                it.pickedUpBy,
                                it.comments,
                                it.billOfLadingNumber,
                                it.loadingStarted,
                                currentDateandTime
                            )
                            viewLifecycleOwner.lifecycleScope.launch {
                                tripRepository.insertBillOfLading(billOfLading)
                            }
                        }
                    }

                }
            }
            dialog.show()
        }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {
        fun newInstance(waypoint: WayPoint): WaypointDetailDialog {
            return WaypointDetailDialog(waypoint)
        }
    }
}
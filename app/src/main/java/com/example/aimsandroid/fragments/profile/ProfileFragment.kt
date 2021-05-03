package com.example.aimsandroid.fragments.profile

import PHONE_NUMBER
import android.Manifest
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import colorGreen
import com.example.aimsandroid.MapDownloadActivity
import com.example.aimsandroid.R
import com.example.aimsandroid.SplashActivity
import com.example.aimsandroid.databinding.AlertAboutAppBinding
import com.example.aimsandroid.databinding.AlertTruckSettingsBinding
import com.example.aimsandroid.databinding.FragmentProfileBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.jakewharton.processphoenix.ProcessPhoenix
import getGreeting
import getLoader


class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding
    private lateinit var loader: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater);
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModelFactory = ProfileViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
        loader = getLoader(requireActivity())
        viewModel.trips.observe(viewLifecycleOwner, Observer {
            refreshInfo()
        })
        binding.logoutContainer.setOnClickListener {
            handleLogout()
        }
        binding.downloadMapContainer.setOnClickListener {
            handleDownloadMap()
        }
        binding.aboutContainer.setOnClickListener {
            handleAbout()
        }
        binding.truckSettingContainer.setOnClickListener {
            handleTruckSettings()
        }
        binding.clockToggleButton.setOnClickListener {
            handleClockToggle()
        }
    }

    fun handleLogout() {
        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setCancelable(true)
            .setTitle("Confirm Logout?")
            .setMessage("Do you want to logout?")
            .setPositiveButton("Confirm"){ dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
                if(viewModel.internetIsConnected()) {
                    if(viewModel.isClockedIn()) {
                        handleClockToggle()
                    }
                    viewModel.logout(object : LogoutEventListener{
                        override fun onLogoutStarted() {
                            loader.show()
                        }

                        override fun onLogoutComplete() {
                            Handler(Looper.getMainLooper()).postDelayed({
                                loader.hide()
                                ProcessPhoenix.triggerRebirth(context, Intent(requireContext(), SplashActivity::class.java))
                            }, 1000)
                        }
                    })
                } else {
                    Toast.makeText(requireContext(), "Logout disabled while offline", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel"){ dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            .show()
    }

    fun handleDownloadMap() {
        startActivity(Intent(requireContext(), MapDownloadActivity::class.java))
    }

    fun handleAbout() {
        val alertAboutAppBinding = AlertAboutAppBinding.inflate(layoutInflater)
        AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setCancelable(true)
            .setView(alertAboutAppBinding.root)
            .setPositiveButton("Help") { dialogInterface: DialogInterface, i: Int ->
                TedPermission.with(requireContext())
                    .setPermissionListener(object: PermissionListener {
                        override fun onPermissionGranted() {
                            startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel: $PHONE_NUMBER")))
                        }

                        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                        }
                    })
                    .setDeniedMessage("Please allow phone call permission for help.")
                    .setGotoSettingButtonText("Open App Settings")
                    .setPermissions(
                        Manifest.permission.CALL_PHONE
                    )
                    .check()
            }
            .setNegativeButton("Close"){dialog: DialogInterface?, i: Int ->
                dialog?.dismiss()
            }
            .show()
    }

    fun handleTruckSettings() {
        val truckSettingsBinding = AlertTruckSettingsBinding.inflate(layoutInflater)
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            .setCancelable(false)
            .setTitle("Please Enter Truck info")
            .setView(truckSettingsBinding.root)
            .setPositiveButton("Save"){ dialogInterface: DialogInterface, i: Int -> }
            .setNegativeButton("Cancel"){ dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }.create()

        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if(isValidTruckSettingsForm(truckSettingsBinding)){
                Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Invalid form data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isValidTruckSettingsForm(binding: AlertTruckSettingsBinding): Boolean {
        var valid = true
        if(binding.truckHeight.text.toString() == "") {
            valid = false
            binding.truckHeight.error = "Required"
        }
        if(binding.truckWeight.text.toString() == "") {
            valid = false
            binding.truckWeight.error = "Required"
        }
        return valid
    }

    fun refreshInfo() {
        val driverName = viewModel.getDriverName()
        driverName?.let{
            val greetingText = "${getGreeting()},\n ${it.trim()}"
            binding.driverName.text = greetingText
        }
        if(viewModel.isClockedIn()) {
            clockInState()
        } else {
            clockOutState()
        }
        updateTotalTrips()
        updateTotalTripsCompleted()
        updateHoursCompleted()
    }

    fun updateTotalTrips(){
        binding.numTrips.text = viewModel.getNumTrips().toString()
    }

    fun updateTotalTripsCompleted(){
        binding.numCompletedTrips.text = viewModel.getNumCompletedTrips().toString()
    }

    fun updateHoursCompleted() {
        binding.hoursComplete.text = viewModel.getHoursCompleted()
    }

    fun handleClockToggle() {
        if(viewModel.isClockedIn()){
            viewModel.clockOut(object : EventListener{
                override fun onStarted() {
                    loader.show()
                }

                override fun onComplete() {
                    Handler(Looper.getMainLooper()).postDelayed({
                        refreshInfo()
                        loader.hide()
                    }, 1000)
                }
            })
        } else {
            viewModel.clockIn(object: EventListener{
                override fun onStarted() {
                    loader.show()
                }

                override fun onComplete() {
                    Handler(Looper.getMainLooper()).postDelayed({
                        refreshInfo()
                        loader.hide()
                    }, 1000)
                }
            })
        }
    }

    fun clockInState() {
        binding.clockToggleButton.text = "Clock out"
        binding.clockStatusText.text = "You're clocked in right now"
        binding.clockStatusText.setTextColor(colorGreen)
    }

    fun clockOutState() {
        binding.clockToggleButton.text = "Clock in"
        binding.clockStatusText.text = "You're clocked out right now"
        binding.clockStatusText.setTextColor(Color.RED)
    }

    interface LogoutEventListener{
        fun onLogoutStarted()
        fun onLogoutComplete()
    }

    interface EventListener{
        fun onStarted()
        fun onComplete()
    }
}
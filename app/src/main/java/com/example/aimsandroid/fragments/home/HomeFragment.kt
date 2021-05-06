package com.example.aimsandroid.fragments.home


import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import colorBlue
import colorGreen
import colorSecondaryLight
import com.example.aimsandroid.R
import com.example.aimsandroid.database.BillOfLading
import com.example.aimsandroid.database.TripWithWaypoints
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.fragments.home.currenttrip.CurrentTripAdapter
import com.example.aimsandroid.fragments.home.currenttrip.dialogs.WaypointDetailDialog
import com.example.aimsandroid.utils.FileLoaderListener
import com.example.aimsandroid.utils.OnSaveListener
import com.example.aimsandroid.utils.TripStatusCode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.here.android.mpa.common.GeoBoundingBox
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.Image
import getLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import putDouble
import sortWaypointBySeqNum
import java.lang.Exception

/*
* Fragment (Android SDK) class to generate Home Tab.
* */
class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    lateinit var binding: FragmentHomeBinding
    private lateinit var currentTripObserver: Observer<TripWithWaypoints>
    private var mapFragmentView: MapFragmentView? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var loader: AlertDialog

    //Refer to android sdk for overridden method documentation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    //Refer to android sdk for overridden method documentation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //initialize viewmodel and data binding
        binding = FragmentHomeBinding.inflate(inflater);
        val homeViewModelFactory = HomeViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, homeViewModelFactory).get(HomeViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        prefs = requireActivity().applicationContext.getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        return binding.root;
    }

    //Refer to android sdk for overridden method documentation
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loader = getLoader(requireActivity())
        resetButtons()
        hideGpsFab()
        binding.navFab.setOnClickListener {
            resumeRoadView()
        }

        binding.startNavFab.setOnClickListener {
            AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                .setTitle("Are you sure?")
                .setMessage("Do you want to start navigation?")
                .setNegativeButton(
                    "No"
                ) { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    startNavigationMode()
                    val currentTripId = viewModel.currentTrip.value?.trip?.tripId
                    if(currentTripId != null) {
                        onTripEvent(currentTripId, TripStatusCode.DRIVING)
                    }
                }.create().show()
        }

        binding.stopNavFab.setOnClickListener {
            AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                .setTitle("Are you sure?")
                .setMessage("Do you want to stop navigation?")
                .setNegativeButton(
                    "No"
                ) { dialog, which ->
                    dialog.cancel()
                }
                .setPositiveButton(
                    "Yes"
                ) { dialog, which ->
                    stopNavigationMode()
                }.create().show()
        }

        //drawer layout
        val backdropHeader = binding.backdropHeader
        val contentLayout = binding.contentLayout
        sheetBehavior = BottomSheetBehavior.from(contentLayout)
        sheetBehavior.isFitToContents = true
        sheetBehavior.isHideable = false
        sheetBehavior.isDraggable = false

        backdropHeader.setOnClickListener {
            if(viewModel.currentTrip.value != null) {
                toggleFilters()
            }
        }

        currentTripObserver = Observer {
            if(it == null) {
                noCurrentTrip()
            } else {
                lifecycleScope.launch {
                    viewModel.resolveNextWaypoint()
                    binding.currentTripRecyclerView.visibility = View.VISIBLE
                    sheetBehavior.isDraggable = true
                    binding.bottomSheetTitle.text = "Current Trip"
                    binding.currentTripTitle.text = "Trip #"+it.trip.tripId

                    val clickListeners = CurrentTripAdapter.CurrentTripClickListener(
                        detailsClickListener = {
                            val dialog = WaypointDetailDialog.newInstance(it)
                            dialog.show(childFragmentManager, "wayPointDetailDialogCurrentTrip")
                        },
                        navigateClickListener = {
                            val destination = GeoCoordinate(it.latitude, it.longitude)
                            showDirections(destination)
                        })
                    val adapter = CurrentTripAdapter(clickListeners, prefs, viewModel.tripRepository)
                    adapter.submitList(sortWaypointBySeqNum(it.waypoints))
                    binding.currentTripRecyclerView.adapter = adapter
                }
            }
        }

        //check if args is passed to expand or collapse drawer layout
        arguments?.let {
            val fromStartTrip = HomeFragmentArgs.fromBundle(it).tripStarted
            if(fromStartTrip){
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }

        viewModel.currentTrip.observe(viewLifecycleOwner, currentTripObserver)
        viewModel.currentTripCompleted.observe(viewLifecycleOwner, Observer {
            if(it){
                noCurrentTrip()
                viewModel.onCurrentTripRemoved()
            }
        })
    }

    //Method to refresh the recycler view in home page with latest data
    fun refreshRecyclerView() {
        val adapter = binding.currentTripRecyclerView.adapter
        binding.currentTripRecyclerView.adapter = adapter
        adapter?.notifyDataSetChanged()
    }

    //Method to show direction from the driver's current location to the passed destination
    /* @param destination the coordinate of the destination */
    fun showDirections(destination: GeoCoordinate) {
        if(prefs.getBoolean("navigating", false)) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            Toast.makeText(requireContext(), "Please End Current Navigation", Toast.LENGTH_SHORT).show()
        } else {
            try{
                val startPoint = GeoCoordinate(viewModel.latitude.value!!, viewModel.longitude.value!!)
                stopNavigationMode()
                prefs.edit().putDouble("lastNavigatedLatitude", destination.latitude).apply()
                prefs.edit().putDouble("lastNavigatedLongitude", destination.longitude).apply()
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                mapFragmentView?.showDirections(startPoint, destination)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Cannot find your current location", Toast.LENGTH_SHORT).show()
            }

        }
    }

    //Method to toggle drawer layout
    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            collapseSheet()
        } else {
            expandSheet()
        }
    }

    //Method to run after route has been calculated
    fun afterRouteCalculated(boundingBox: GeoBoundingBox?) {
        hideLoader()
        if(prefs.getBoolean("navigating", false)){
            continueNavigationMode()
        } else {
            mapFragmentView?.zoomTo(boundingBox)
            viewStartNavFab()
            hideStopNavFab()
            hideGpsFab()
            hideNavFab()
        }
    }

    //Method to run if there is a route calculation error
    fun afterRouteCalculationError() {
        hideLoader()
        if(prefs.getBoolean("navigating", false)) {
            stopNavigationMode()
        }
    }

    //Method to end just the navigation and not clear the routes
    fun endNavigationOnly() {
        mapFragmentView?.onNavigationEnded()
    }

    //This method is called after driver has reached his destination
    fun onDestinationReached() {
        stopNavigationMode()
    }

    //This method is called when navigation mode is started
    fun startNavigationMode() {
        prefs.edit().putBoolean("navigating", true).apply()
        mapFragmentView?.startNavigation()
        viewStopNavFab()
        hideStartNavFab()
        hideGpsFab()
        hideNavFab()
    }

    //This method is called when we need to continue current navigation if this view is destroyed and recovered back
    fun continueNavigationMode() {
        hideStartNavFab()
        hideGpsFab()
        hideNavFab()
        viewStopNavFab()
        mapFragmentView?.startNavigation()
    }

    //This method is called when navigation mode is stopped
    fun stopNavigationMode() {
        prefs.edit().putBoolean("navigating", false).apply()
        mapFragmentView?.onNavigationEnded()
        viewGpsFab()
        hideNavFab()
        hideStartNavFab()
        hideStopNavFab()
    }

    //Refer to android sdk for overridden method documentation
    override fun onPause() {
        super.onPause()
        mapFragmentView?.onPause()
    }

    //Refer to android sdk for overridden method documentation
    override fun onDestroy() {
        super.onDestroy()
    }

    //Refer to android sdk for overridden method documentation
    override fun onResume() {
        super.onResume()
        mapFragmentView = MapFragmentView.getInstance(this, binding.viewModel!!)
        mapFragmentView!!.initMapFragment()
    }

    //Functions to view or hide the gps buttons based on the state
    fun viewNavFab() {
        requireActivity().runOnUiThread {
            binding.navFab.visibility = View.VISIBLE
        }
    }

    fun viewGpsFab() {
        binding.gpsFab.visibility = View.VISIBLE
    }

    fun viewStartNavFab() {
        binding.startNavFab.visibility = View.VISIBLE
    }

    fun viewStopNavFab() {
        binding.stopNavFab.visibility = View.VISIBLE
        viewTurnByTurnLayout()
    }

    fun hideNavFab() {
        binding.navFab.visibility = View.GONE
    }

    fun hideGpsFab() {
        binding.gpsFab.visibility = View.GONE
    }

    fun hideStartNavFab() {
        binding.startNavFab.visibility = View.GONE
    }

    fun hideStopNavFab() {
        binding.stopNavFab.visibility = View.GONE
        hideTurnByTurnLayout()
    }

    fun viewTurnByTurnLayout() {
        binding.nextManeuverContainer.visibility = View.VISIBLE
    }

    fun hideTurnByTurnLayout() {
        binding.nextManeuverContainer.visibility = View.GONE
    }

    fun resetButtons() {
        viewGpsFab()
        hideNavFab()
        hideStartNavFab()
        hideStopNavFab()
    }

    //method to pause road view mode
    fun pauseRoadView(){
        if(prefs.getBoolean("navigating", false)){
            mapFragmentView?.pauseRoadView()
            viewNavFab()
        }
    }

    //method to resume road view mode
    fun resumeRoadView() {
        if(prefs.getBoolean("navigating", false)){
            mapFragmentView?.resumeRoadView()
            hideNavFab()
        }
    }

    //method to save bill of lading form, this method delegates action to the viewmodel
    fun saveForm(billOfLading: BillOfLading, bolBitmap: Bitmap?, signatureBitmap: Bitmap?, onSaveListener: OnSaveListener) {
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                viewModel.saveForm(billOfLading, bolBitmap, signatureBitmap, onSaveListener)
                withContext(Dispatchers.Main){
                    refreshRecyclerView()
                }
            }
        }
    }

    //method to handle ui when there is no current trip
    fun noCurrentTrip() {
        binding.currentTripRecyclerView.visibility = View.GONE
        sheetBehavior.isDraggable = false
        collapseSheet()
        binding.currentTripTitle.text = "You have not started any trips"
        binding.bottomSheetTitle.text = "You have not started any trips"
    }

    //method to collapse bottom drawer
    fun collapseSheet() {
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    //method to expand bottom drawer
    fun expandSheet() {
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    //method to get signature uri from view model
    suspend fun getSignatureUri(tripIdFk: Long, waypointSeqNum: Long, fileLoaderListener: FileLoaderListener) {
        viewModel.getSignatureUri(tripIdFk, waypointSeqNum, fileLoaderListener)
    }

    //method to get bill of lading picture uri from view model
    suspend fun getBolUri(tripIdFk: Long, waypointSeqNum: Long, fileLoaderListener: FileLoaderListener) {
        viewModel.getBolUri(tripIdFk, waypointSeqNum, fileLoaderListener)
    }

    fun routeCalculationOnProgress(i: Int) {
        showLoader()
    }

    //method to show loader
    fun showLoader(){
        if(!loader.isShowing){
            loader.show()
        }
    }

    //method to hide loader
    fun hideLoader(){
        Handler(Looper.getMainLooper()).postDelayed({
            loader.dismiss()
        }, 1000)
    }

    //method to send api request on trip event via view model
    fun onTripEvent(tripId: Long, tripStatusCode: TripStatusCode){
        viewModel.onTripEvent(tripId, tripStatusCode)
    }
}
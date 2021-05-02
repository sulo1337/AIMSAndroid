package com.example.aimsandroid.fragments.trips

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import colorGreen
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentTripsBinding
import com.example.aimsandroid.fragments.trips.dialogs.TripsDetailDialog
import com.example.aimsandroid.utils.FetchApiEventListener
import com.example.aimsandroid.utils.FileLoaderListener
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TripsFragment : Fragment() {

    private lateinit var viewModel: TripsViewModel
    private lateinit var binding: FragmentTripsBinding
    private lateinit var prefs: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = FragmentTripsBinding.inflate(inflater);
        val tripsViewModelFactory = TripsViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, tripsViewModelFactory).get(TripsViewModel::class.java)
        setupInternetListener();
        prefs = requireActivity().getSharedPreferences("com.example.aimsandroid", Context.MODE_PRIVATE)
        val fragmentTitle = binding.fragmentTitle
        fragmentTitle.setText(getString(R.string.trips_toolbar_title));
        binding.lifecycleOwner = this
        binding.tripsRecyclerView.adapter = TripsAdapter(TripsAdapter.TripsClickListener{
            lifecycleScope.launch{
                val dialog: DialogFragment = TripsDetailDialog.newInstance(it)
                dialog.show(childFragmentManager, "tripsDetailDialog")
            }
        }, prefs)
        viewModel.trips.observe(viewLifecycleOwner, Observer{
            val adapter = binding.tripsRecyclerView.adapter as TripsAdapter
            adapter.submitList(it)
        })
        viewModel.refreshing.observe(viewLifecycleOwner, Observer {
            binding.swipeRefreshContainer.isRefreshing = it
        })
        binding.swipeRefreshContainer.setOnRefreshListener {
            viewModel.refreshTrips(object : FetchApiEventListener{
                override fun onSuccess() {
                    Snackbar.make(requireView(), "Trips Up-to-date", Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                        Color.rgb(75,181,67)
                    ).show()
                }

                override fun onError(error: String) {
                    Snackbar.make(requireView(), "No internet connection", Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).setBackgroundTint(
                        Color.rgb(220,20,60)).show()
                }
            })
        }
        return binding.root;
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_filter, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.completedTrips -> submitCompletedTrips()
            R.id.pendingTrips -> submitPendingTrips()
            else -> submitAllTrips()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun submitCompletedTrips() {
        val adapter = binding.tripsRecyclerView.adapter as TripsAdapter
        val completedTrips = viewModel.trips.value?.filter{
            it.tripStatus?.complete == true
        }
        adapter.submitList(completedTrips)
        adapter.notifyDataSetChanged()
    }

    private fun submitPendingTrips() {
        val adapter = binding.tripsRecyclerView.adapter as TripsAdapter
        val completedTrips = viewModel.trips.value?.filter{
            it.tripStatus == null
        }
        adapter.submitList(completedTrips)
        adapter.notifyDataSetChanged()
    }

    private fun submitAllTrips() {
        val adapter = binding.tripsRecyclerView.adapter as TripsAdapter
        adapter.submitList(viewModel.trips.value)
        adapter.notifyDataSetChanged()
    }

    fun getViewModel(): TripsViewModel{
        return viewModel
    }

    fun setupInternetListener() {
        viewModel.online.observe(viewLifecycleOwner, Observer {
            if(it == true) {
                showOnlineStatus()
            } else if(it == false) {
                showOfflineStatus()
            }
        })
    }

    fun showOnlineStatus() {
        binding.onlineStatus.text = "Online"
        binding.onlineStatus.setTextColor(colorGreen)
    }

    fun showOfflineStatus() {
        binding.onlineStatus.text = "Offline"
        binding.onlineStatus.setTextColor(Color.RED)
    }

    suspend fun getSignatureUri(tripIdFk: Long, waypointSeqNum: Long, fileLoaderListener: FileLoaderListener) {
        viewModel.getSignatureUri(tripIdFk, waypointSeqNum, fileLoaderListener)
    }

    suspend fun getBolUri(tripIdFk: Long, waypointSeqNum: Long, fileLoaderListener: FileLoaderListener) {
        viewModel.getBolUri(tripIdFk, waypointSeqNum, fileLoaderListener)
    }
}
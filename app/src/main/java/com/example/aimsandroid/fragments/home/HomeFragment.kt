package com.example.aimsandroid.fragments.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentHomeBinding
import com.example.aimsandroid.utils.OnBottomSheetCallbacks
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var sheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater);
        val toolbarTitle = activity?.findViewById<TextView>(R.id.toolbarTitle) as TextView;
        toolbarTitle.setText(getString(R.string.current_trip_toolbar_title));
        val backdropHeader = binding.backdropHeader
        val contentLayout = binding.contentLayout
        sheetBehavior = BottomSheetBehavior.from(contentLayout)
        sheetBehavior.isFitToContents = false
        sheetBehavior.isHideable = false
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        backdropHeader.setOnClickListener { it ->
            toggleFilters()
        }
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun toggleFilters() {
        if(sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.filterIcon.setImageResource(R.drawable.ic_keyboard_arrow_up)
        } else {
            binding.filterIcon.setImageResource(R.drawable.ic_keyboard_arrow_down)
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }
}
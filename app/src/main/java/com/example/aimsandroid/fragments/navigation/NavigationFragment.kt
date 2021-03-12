package com.example.aimsandroid.fragments.navigation

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FragmentCurrentTripBinding
import com.example.aimsandroid.databinding.FragmentNavigationBinding

class NavigationFragment : Fragment() {

    companion object {
        fun newInstance() = NavigationFragment()
    }

    private lateinit var viewModel: NavigationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentNavigationBinding.inflate(inflater);
        val toolbarTitle = activity?.findViewById<TextView>(R.id.toolbarTitle) as TextView;
        toolbarTitle.setText(getString(R.string.navigation_toolbar_title));
        return binding.root;
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(NavigationViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
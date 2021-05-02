package com.example.aimsandroid.fragments.profile

import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aimsandroid.SplashActivity
import com.example.aimsandroid.databinding.FragmentProfileBinding
import com.jakewharton.processphoenix.ProcessPhoenix
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
        binding.logoutContainer.setOnClickListener {
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
        }
    }

    interface LogoutEventListener{
        fun onLogoutStarted()
        fun onLogoutComplete()
    }
}
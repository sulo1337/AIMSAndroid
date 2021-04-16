package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.aimsandroid.R
import com.example.aimsandroid.database.WayPoint
import com.example.aimsandroid.databinding.FormCaptureSignatureBinding

class CaptureSignatureDialog: DialogFragment() {

    private lateinit var binding: FormCaptureSignatureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FormCaptureSignatureBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //requireActivity().requestedOrientation  = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    companion object {
        fun newInstance(): CaptureSignatureDialog {
            return CaptureSignatureDialog()
        }
    }

    override fun onDestroyView() {
        if(dialog!=null && retainInstance) {
            dialog!!.setDismissMessage(null)
        }
        super.onDestroyView()
    }
}
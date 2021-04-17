package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FormCaptureSignatureBinding
import com.github.gcacace.signaturepad.views.SignaturePad

class CaptureSignatureDialog: DialogFragment() {

    private lateinit var binding: FormCaptureSignatureBinding
    private lateinit var signaturePad: SignaturePad

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.FullscreenDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FormCaptureSignatureBinding.inflate(inflater)
        signaturePad = binding.signaturePad
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableCaptureSignatureButton()
        disableClearSignatureButton()

        binding.captureSignature.setOnClickListener {
            val parentFragment = parentFragment as CaptureBolDialog
            parentFragment.saveSignature(signaturePad.signatureBitmap)
            dismiss()
        }

        binding.clearSignature.setOnClickListener {
            signaturePad.clear()
        }

        signaturePad.setOnSignedListener(object: SignaturePad.OnSignedListener{
            override fun onStartSigning() {
                enableCaptureSignatureButton()
                enableClearSignatureButton()
            }

            override fun onSigned() {
                enableCaptureSignatureButton()
                enableClearSignatureButton()
            }

            override fun onClear() {
                disableCaptureSignatureButton()
                disableClearSignatureButton()
            }
        })
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

    private fun enableClearSignatureButton() {
        binding.clearSignature.isEnabled = true
        binding.clearSignature.alpha = 1.0f
    }

    private fun disableClearSignatureButton() {
        binding.clearSignature.isEnabled = false
        binding.clearSignature.alpha = 0.5f
    }

    private fun enableCaptureSignatureButton() {
        binding.captureSignature.isEnabled = true
        binding.captureSignature.alpha = 1.0f
    }

    private fun disableCaptureSignatureButton() {
        binding.captureSignature.isEnabled = false
        binding.captureSignature.alpha = 0.5f
    }
}
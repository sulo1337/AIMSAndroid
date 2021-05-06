package com.example.aimsandroid.fragments.home.currenttrip.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import com.example.aimsandroid.R
import com.example.aimsandroid.databinding.FormCaptureSignatureBinding
import com.github.gcacace.signaturepad.views.SignaturePad

/*
* Creates Android's dialog fragment to capture the signature while pickup/delivery
* All the documentation for overridden methods can be found in Android library
* */
class CaptureSignatureDialog: DialogFragment() {

    private lateinit var binding: FormCaptureSignatureBinding
    private lateinit var signaturePad: SignaturePad

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        root.layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(root)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }

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

    /*
    * Static method to create a new instance of this dialog fragment
    * */
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

    /*
    * This method enables Clear button for signature pad
    * */
    private fun enableClearSignatureButton() {
        binding.clearSignature.isEnabled = true
        binding.clearSignature.alpha = 1.0f
    }

    /*
    * This method disables Clear button for signature pad
    * */
    private fun disableClearSignatureButton() {
        binding.clearSignature.isEnabled = false
        binding.clearSignature.alpha = 0.5f
    }

    /*
    * This method enables Capture Signature button for signature pad
    * */
    private fun enableCaptureSignatureButton() {
        binding.captureSignature.isEnabled = true
        binding.captureSignature.alpha = 1.0f
    }

    /*
    * This method disables Capture Signature button for signature pad
    * */
    private fun disableCaptureSignatureButton() {
        binding.captureSignature.isEnabled = false
        binding.captureSignature.alpha = 0.5f
    }
}
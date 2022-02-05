package com.gievenbeck.paple.fragment

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gievenbeck.paple.MainActivity
import com.gievenbeck.paple.R
import kotlinx.android.synthetic.main.fragment_dialog_alert.*

class SettingAlertDialogFragment : DialogFragment() {

    var listener : OnPermissionSettingConfirmedListener ?= null
    private var question : String ?= null
    private var confirm : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            confirm = it.getString("confirm")
            question =  it.getString("question")
        }
        onAttach(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_alert, container, false)
    }

    override fun onResume() {
        super.onResume()
        if(dialog != null) {
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_ask_alert.text = question
        tv_confirm_alert.text = confirm

        tv_cancel_alert.setOnClickListener {
            dismiss()
        }

        tv_confirm_alert.setOnClickListener {
            listener?.startSystemSetting()
            dismiss()
        }
    }


     override fun onAttach(Context: Context) {
         super.onAttach(Context)
         listener = Context as? OnPermissionSettingConfirmedListener
        if (listener == null) {
            throw ClassCastException("$Context must implement OnPermissionSettingConfirmedListener")
        }
    }

    interface OnPermissionSettingConfirmedListener {
        fun startSystemSetting()
    }

    companion object {


        const val TAG = "PermissionSettingAlertDialogFragment"

        @JvmStatic
        fun newInstance(question: String, confirm : String) =
            SettingAlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("question", question)
                    putString("confirm", confirm)
                }
            }
    }
}
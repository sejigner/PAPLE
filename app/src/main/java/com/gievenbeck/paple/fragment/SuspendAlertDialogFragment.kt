package com.sejigner.closest.fragment

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_dialog_alert.*
import kotlinx.android.synthetic.main.fragment_dialog_alert_suspend.*

class SuspendAlertDialogFragment : DialogFragment() {

    var listener : OnConfirmedListener ?= null
    private var question : String ?= null
    private var confirm : String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            confirm = it.getString("confirm")
            question =  it.getString("question")
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dialog_alert_suspend, container, false)
    }

    override fun onResume() {
        super.onResume()
//        dialog!!.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        if(dialog != null) {
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_ask_alert_suspend.text = question
        tv_suspend_confirm_alert.text = confirm

        tv_suspend_confirm_alert.setOnClickListener {
            listener?.finishApp()
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.finishApp()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        listener = context as? OnConfirmedListener
        if (listener == null) {
            throw ClassCastException("$context must implement OnConfirmedListener")
        }
    }

    interface OnConfirmedListener {
        fun finishApp()
    }

    companion object {


        const val TAG = "AlertDialog"

        @JvmStatic
        fun newInstance(question: String, confirm : String) =
            SuspendAlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("question", question)
                    putString("confirm", confirm)
                }
            }
    }
}
package com.gievenbeck.paple.fragment

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gievenbeck.paple.R
import kotlinx.android.synthetic.main.fragment_dialog_alert.*

class AlertDialogFragment : DialogFragment() {

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
            listener?.proceed()
            dismiss()
        }
    }


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        listener = context as? OnConfirmedListener
        if (listener == null) {
            throw ClassCastException("$context must implement OnConfirmedListener")
        }
    }

    interface OnConfirmedListener {
        fun proceed()
    }

    companion object {


        const val TAG = "AlertDialog"

        @JvmStatic
        fun newInstance(question: String, confirm : String) =
            AlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("question", question)
                    putString("confirm", confirm)
                }
            }
    }
}
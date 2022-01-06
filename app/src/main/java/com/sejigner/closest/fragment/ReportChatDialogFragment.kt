package com.sejigner.closest.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_report_chat.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [FirstDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportChatDialogFragment : DialogFragment() {

    private var callback : OnReportConfirmedListener ?= null

    interface OnReportConfirmedListener {
        fun reportMessagesFirebase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_report_chat, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnReportConfirmedListener) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + "must implement OnReportConfirmedListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tv_send_report_chat.setOnClickListener {
            callback?.reportMessagesFirebase()
            dismiss()
        }

        tv_cancel_report_chat.setOnClickListener {
            dismiss()
        }

    }

    override fun onResume() {
        super.onResume()
//        dialog!!.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        if(dialog != null) {
//            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDialog.
         */

        const val TAG = "ReportChatDialogFragment"

    }
}
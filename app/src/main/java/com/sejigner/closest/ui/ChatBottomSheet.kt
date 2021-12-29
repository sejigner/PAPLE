package com.sejigner.closest.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.dialog_bottom_sheet_chat.*

class ChatBottomSheet() : BottomSheetDialogFragment() {

    lateinit var callback : BottomSheetChatLogInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_bottom_sheet_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_cancel.setOnClickListener {
            dismiss()
        }
        setButton()
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        callback = context as BottomSheetChatLogInterface
    }

    private fun setButton() {
        tv_finish_bottom_sheet.setOnClickListener {
            callback.confirmChatLeave()
        }
        tv_report_bottom_sheet.setOnClickListener {
            callback.reportPartner()
        }
        tv_cancel.setOnClickListener {
            dismiss()
        }
    }

    interface BottomSheetChatLogInterface {
        fun reportPartner()
        fun confirmChatLeave()
    }

}


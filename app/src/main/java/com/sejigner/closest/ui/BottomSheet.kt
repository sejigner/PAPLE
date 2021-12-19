package com.sejigner.closest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.dialog_bottom_sheet.*

class BottomSheet(bottomSheetChatLogInterface : BottomSheetChatLogInterface) : BottomSheetDialogFragment() {

    private val mListener = bottomSheetChatLogInterface

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_cancel.setOnClickListener {
            dismiss()
        }
        setButton()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun setButton() {
        tv_finish_bottom_sheet.setOnClickListener {
            mListener.finishChat()
        }
        tv_report_bottom_sheet.setOnClickListener {
            mListener.reportPartner()
        }
        tv_cancel.setOnClickListener {
            dismiss()
        }
    }
}

interface BottomSheetChatLogInterface {
    fun reportPartner()
    fun finishChat()
}
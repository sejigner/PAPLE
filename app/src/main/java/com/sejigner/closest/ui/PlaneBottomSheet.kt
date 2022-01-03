package com.sejigner.closest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.dialog_bottom_sheet_plane.*

class PlaneBottomSheet() : BottomSheetDialogFragment() {

    lateinit var callback : OnMenuClickedListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_bottom_sheet_plane, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private fun setButtonOnClickListener() {
        tv_discard_paper_bottom_sheet.setOnClickListener {
            callback.confirmDiscardPaper()
        }
        tv_report_plane_bottom_sheet.setOnClickListener {
            callback.confirmReportPaper()
        }
        tv_cancel_plane_bottom_sheet.setOnClickListener {
            dismiss()
        }
    }

    interface OnMenuClickedListener {
        fun confirmDiscardPaper()
        fun confirmReportPaper()
    }

}


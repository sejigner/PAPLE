package com.gievenbeck.paple.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.gievenbeck.paple.MainActivity.Companion.isOnline
import com.gievenbeck.paple.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet_plane.*

class PlaneBottomSheet() : BottomSheetDialogFragment() {

    private lateinit var callback : OnMenuClickedListener

    interface OnMenuClickedListener {
        fun confirmDiscardPaper()
        fun confirmReportPaper()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onAttach(requireParentFragment())
    }

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

    fun onAttach(fragment: Fragment) {
        if (fragment is OnMenuClickedListener) {
            callback = fragment
        } else {
            throw RuntimeException(context.toString() + "must implement OnMenuClickedListener")
        }
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        if(context is OnMenuClickedListener) {
//            callback = context
//        } else {
//            throw RuntimeException(context.toString() + "must implement OnMenuClickedListener")
//        }
//    }


    private fun setButtonOnClickListener() {
        tv_discard_paper_bottom_sheet.setOnClickListener {
            callback.confirmDiscardPaper()
        }
        tv_report_plane_bottom_sheet.setOnClickListener {
            if(isOnline) {
                callback.confirmReportPaper()
            } else {
                Toast.makeText(requireActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show()
            }
        }
        tv_cancel_plane_bottom_sheet.setOnClickListener {
            dismiss()
        }
    }

}


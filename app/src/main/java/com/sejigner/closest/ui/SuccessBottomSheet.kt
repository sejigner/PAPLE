package com.sejigner.closest.ui

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.dialog_bottom_sheet_flight.*

class SuccessBottomSheet() : BottomSheetDialogFragment() {

    private val model : FragmentChatViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_bottom_sheet_flight, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.flightResult.observe(viewLifecycleOwner, {
            Log.d("flight result", "result : $it")
            if(it == "success") {
                notifySuccess()
            }
        })

        cl_confirm_flight_success.isEnabled = false
        cl_confirm_flight_success.setOnClickListener {
            dismiss()
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private fun notifySuccess() {
        title_bottom_sheet_success.text = getText(R.string.flight_success)
        tv_info_success.text = getText(R.string.flight_reply_result_untargeted)
        cl_confirm_flight_success.isEnabled = true
    }

    companion object {
        const val TAG = "SuccessBottomSheet"
    }


}


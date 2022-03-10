package com.gievenbeck.paple.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.gievenbeck.paple.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_sheet_flight.*

class SuccessBottomSheet() : BottomSheetDialogFragment() {

    private var callback : OnFlightSuccess ?= null

    private val model : FragmentChatViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.dialog_bottom_sheet_flight, container, false)
    }

    interface OnFlightSuccess {
        fun showInterstitial()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFlightSuccess) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + "must implement OnFlightSuccess")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.flightResult.observe(viewLifecycleOwner, {
            Log.d("flight result", "result : $it")
            if(it == "success") {
                notifySuccessFirst()
            } else if (it == "replied") {
                notifySuccessReply()
            }
        })

        cl_confirm_flight_success.isEnabled = false
        cl_confirm_flight_success.setOnClickListener {
            dismiss()
            callback?.showInterstitial()
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    private fun notifySuccessFirst() {
        title_bottom_sheet_success.text = getText(R.string.flight_success)
        tv_info_success.text = getText(R.string.flight_result_untargeted)
        cl_confirm_flight_success.isEnabled = true
    }

    private fun notifySuccessReply() {
        title_bottom_sheet_success.text = getText(R.string.flight_success)
        tv_info_success.text = getText(R.string.flight_reply_result_untargeted)
        cl_confirm_flight_success.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        model.setResult("")
    }

    companion object {
        const val TAG = "SuccessBottomSheet"
    }


}


package com.sejigner.closest.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_write.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.math.round

class FlySuccessFragment : DialogFragment() {
    // TODO: Rename and change types of parameters

    private var flightDistance: Double ?= null
    private var isReply : Boolean = false
    private var mCallbackMain: FlySuccessListenerMain? = null

    interface FlySuccessListenerMain {
        fun showInterstitial()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flightDistance = it.getDouble("flightDistance")
            isReply = it.getBoolean("isReply")
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_fly_success, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val flightResult  = view.findViewById<View>(R.id.tv_flight_result) as? TextView
        val btnClose = view.findViewById<View>(R.id.iv_close_flight_result) as? ImageView

        btnClose?.setOnClickListener {
            dismiss()
        }
        if(!isReply) {
            if(flightDistance!=null) {
                flightResult?.text = getString(R.string.flight_result, convertDistanceToString(flightDistance!!))
            } else {
                flightResult?.text = getString(R.string.flight_result_untargeted)
            }
        } else {
            if(flightDistance!=null) {
                flightResult?.text = getString(R.string.flight_reply_result, convertDistanceToString(flightDistance!!))
            } else {
                flightResult?.text = getString(R.string.flight_reply_result_untargeted)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCallbackMain?.showInterstitial()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FlySuccessListenerMain) {
            mCallbackMain = context
        } else {
            throw RuntimeException(context.toString() + "must implement FirstPlaneListenerMain")
        }
    }

    private fun convertDistanceToString(distance : Double) : String {
        return if(distance >= 1000) {
            (round((distance/1000)*100) /100).toString() + "km"
        } else distance.toString() + "m"
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

        const val TAG = "FlySuccessFragment"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(flightDistance: Double) =
            FlySuccessFragment().apply {
                arguments = Bundle().apply {
                    putDouble("flightDistance",flightDistance)
                }
            }
        @JvmStatic
        fun newInstance(isReply: Boolean, flightDistance: Double) =
            FlySuccessFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("isReply",isReply)
                    putDouble("flightDistance", flightDistance)
                }
            }
    }
}
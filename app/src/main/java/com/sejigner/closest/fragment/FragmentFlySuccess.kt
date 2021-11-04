package com.sejigner.closest.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.NewSignInActivity
import com.sejigner.closest.R
import com.sejigner.closest.models.PaperplaneMessage
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_write.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogFirst.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentFlySuccess : DialogFragment() {
    // TODO: Rename and change types of parameters

    private var flightDistance: Double ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            flightDistance = it.getDouble("flightDistance")
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
        if(flightDistance!=null) {
            flightResult?.text = getString(R.string.flight_result, convertDistanceToString(flightDistance!!))
        } else {
            flightResult?.text = getString(R.string.flight_result_untargeted)
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

        const val TAG = "FragmentFlySuccess"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(flightDistance: Double) =
            FragmentFlySuccess().apply {
                arguments = Bundle().apply {
                    putDouble("flightDistance",flightDistance)
                }
            }
    }
}
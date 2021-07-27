package com.sejigner.closest.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_dialog_second.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogSecond.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDialogSecond : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var message: String? = null
    private var distance: String? = null
    private var time: String? = null
    private var toId: String? = null
    private var fromId: String?= null
    private var isReplied: Boolean ?= null
    private var replyTime: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString("message")
            distance = it.getString("distance")
            time = it.getString("time")
            toId = it.getString("toId")
            fromId = it.getString("fromId")
            isReplied = it.getBoolean("isReplied")


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_reply, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_dialog_message_second.text = message
        tv_dialog_distance_second.text = distance
        tv_dialog_time_second.text = time
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(message: String, distance : String, time: String) =
            FragmentDialogSecond().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putString("distance", distance)
                    putString("time", time)
                    putString("toId", toId)
                    putString("fromId", fromId)
                    putBoolean("isReplied", isReplied!!)
                }
            }
    }
}
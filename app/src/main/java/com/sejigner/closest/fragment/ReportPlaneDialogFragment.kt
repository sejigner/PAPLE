package com.sejigner.closest.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_report_chat.*
import kotlinx.android.synthetic.main.fragment_dialog_report_plane.*
import kotlinx.android.synthetic.main.fragment_dialog_report_plane.iv_back_report
import kotlinx.android.synthetic.main.fragment_dialog_write.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogFirst.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReportPlaneDialogFragment : DialogFragment() {


    var message: String? = ""
    var time: Long? = null
    var isFirst: Boolean? = null
    private var firstPlaneCallback: FirstPlaneCallback? = null
    private var repliedPlaneCallback: RepliedPlaneCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString("message")
            time = it.getLong("time")
            isFirst = it.getBoolean("isFirst")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_report_plane, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val tvMessage = view.findViewById<View>(R.id.tv_dialog_message_report) as? TextView
        val tvTime = view.findViewById<View>(R.id.tv_dialog_time_report) as? TextView

        tvMessage?.text = message
        if (time != null) {
            tvTime?.text = setDateToTextView(time!!)
        }

        tv_send_report.setOnClickListener {
            if (isFirst!!) {
                (parentFragment as FragmentDialogFirst).reportPaper()
            } else {
                (parentFragment as FragmentDialogReplied).reportFirebase()
            }
            dismiss()
        }

        iv_back_report.setOnClickListener {
            dismiss()
        }

    }

    private fun setDateToTextView(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(timestamp * 1000L)
    }

    override fun onStart() {
        super.onStart()
        dialog!!.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    interface FirstPlaneCallback {
        fun reportPaper()
    }

    interface RepliedPlaneCallback {
        fun reportFirebase()
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

        const val TAG = "ReportPaperPlane"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstanceFirst(message: String, time: Long) =
            ReportPlaneDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putLong("time", time)
                    putBoolean("isFirst", true)

                }
            }

        fun newInstanceReplied(message: String, time: Long) =
            ReportPlaneDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putLong("time", time)
                    putBoolean("isFirst", false)
                }
            }

    }
}
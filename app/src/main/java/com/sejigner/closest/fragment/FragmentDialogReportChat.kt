package com.sejigner.closest.fragment

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
class FragmentDialogReportChat : DialogFragment() {

    private var chatRoomCallback: ChatRoomCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_report_chat, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        tv_send_report_chat.setOnClickListener {
            chatRoomCallback?.reportMessagesFirebase()
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
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    interface ChatRoomCallback {
        fun reportMessagesFirebase()
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

        const val TAG = "FragmentDialogReportChat"

    }
}
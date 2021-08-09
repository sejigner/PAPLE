package com.sejigner.closest.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_second.*
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogReplied.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDialogReplied : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var message: String? = null
    private var distance: String? = null
    private var time: Long? = null
    private var toId: String? = null
    private var fromId: String?= null
    private var isReplied: Boolean ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString("message")
            distance = it.getString("distance")
            time = it.getLong("time")
            toId = it.getString("toId")
            fromId = it.getString("fromId")
            isReplied = it.getBoolean("isReplied")


        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_dialog_message_second.text = message
        tv_dialog_distance_second.text = distance

        setDateToTextView(time!!)

        // 버리기 -> 파이어베이스 데이터 삭제
        tv_dialog_discard_second.setOnClickListener {
            removePaper()
            dismiss()
        }


        tv_dialog_start_chat.setOnClickListener {
            // 답장을 할 경우 메세지는 사라지고, 채팅으로 넘어가는 점 숙지시킬 것 (Dialog 이용)
            val intent = Intent(view.context,ChatLogActivity::class.java)
            intent.putExtra(FragmentChat.USER_KEY, fromId)
            startActivity(intent)
            removePaper()
            dismiss()
        }
    }

    private fun removePaper() {
        val paperPlaneReceiverReference =
            FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$toId")
        paperPlaneReceiverReference.removeValue()
        dismiss()
    }

    private fun setDateToTextView(timestamp: Long) {
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm")
        val date = sdf.format(timestamp*1000L)
        tv_dialog_time_first.text = date.toString()
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
        fun newInstance(message: String, distance : String, time: Long, toId : String, fromId : String, isReplied : Boolean) =
            FragmentDialogReplied().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putString("distance", distance)
                    putLong("time", time)
                    putString("toId", toId)
                    putString("fromId", fromId)
                    putBoolean("isReplied", isReplied)
                }
            }
    }
}
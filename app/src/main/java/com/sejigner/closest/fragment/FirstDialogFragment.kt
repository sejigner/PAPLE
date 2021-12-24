package com.sejigner.closest.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.models.ReportMessage
import com.sejigner.closest.room.*
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_write.*
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
class FragmentDialogFirst : DialogFragment(), ReportPlaneDialogFragment.FirstPlaneCallback {
    // TODO: Rename and change types of parameters
    private var message: String? = null
    private var distance: Double? = null
    private var time: Long? = null
    private var fromId: String? = null
    private var paper: FirstPaperPlanes? = null
    private var mContext: Context? = null
    private var mCallbackMain: FirstPlaneListenerMain? = null
    lateinit var repository : PaperPlaneRepository
    lateinit var factory : FragmentChatViewModelFactory
    lateinit var viewModel : FragmentChatViewModel

    interface FirstPlaneListenerMain {
        fun showReplySuccessFragment(isReply : Boolean, flightDistance: Double)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            message = it.getString("message")
            distance = it.getDouble("distance")
            time = it.getLong("time")
            fromId = it.getString("fromId")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(true)
        } else {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_first, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = PaperPlaneRepository(PaperPlaneDatabase.invoke(requireActivity()))
        factory = FragmentChatViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)


        val etReply = view.findViewById<View>(R.id.et_dialog_message_first) as? EditText
        var textEntered: String
        val btnReply = view.findViewById<View>(R.id.tv_dialog_send) as? TextView


        tv_dialog_time_first.text = setDateToTextView(time!!)
        tv_dialog_message_first.text = message
        tv_dialog_distance_first.text = getString(R.string.first_plane_dialog, convertDistanceToString(distance!!))


        tv_report_first.setOnClickListener {
            val dialog = ReportPlaneDialogFragment.newInstanceFirst(
                message!!,
                time!!
            )
            val fm = childFragmentManager
            dialog.show(fm, "report")
        }

        cl_discard_first.setOnClickListener {
            // Firebase 내 해당 데이터 삭제
            viewModel.delete(paper!!)
            dismiss()
        }


        btnReply?.setOnClickListener {
            textEntered = etReply?.text.toString()
            if (textEntered.isNotEmpty()) {
                val paperPlaneReceiverReference =
                    FirebaseDatabase.getInstance()
                        .getReference("/PaperPlanes/Receiver/$fromId/$UID")
                val paperplaneMessage = PaperplaneMessage(
                    paperPlaneReceiverReference.key!!,
                    textEntered,
                    UID,
                    fromId!!,
                    distance!!,
                    System.currentTimeMillis() / 1000L,
                    true
                )
                paperPlaneReceiverReference.setValue(paperplaneMessage).addOnFailureListener {
                    Log.d(TAG, "Reply 실패")
                }.addOnSuccessListener {
                    viewModel.delete(paper!!)
                    mCallbackMain?.showReplySuccessFragment(true, distance!!)
                    dismiss()
                }


            } else {
                Toast.makeText(requireActivity(), "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        tv_dialog_discard_first.setOnClickListener {
//            removePaper()
            viewModel.delete(paper!!)
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FragmentDialogFirst.FirstPlaneListenerMain) {
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

    private fun removePaper() {
        val paperPlaneReceiverReference =
            FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$UID/$fromId")
        paperPlaneReceiverReference.removeValue()
    }

    override fun onStart() {
        super.onStart()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun setDateToTextView(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(timestamp * 1000L)
    }

    override fun reportFirebase() {

        val fromId = fromId!!
        val message = message!!
        val uid = UID

        val ref =
            FirebaseDatabase.getInstance().getReference("/Reports/Plane/$uid/$fromId")

        val reportMessage = ReportMessage(
            uid,
            message,
            System.currentTimeMillis() / 1000L
        )

        ref.setValue(reportMessage).addOnFailureListener {
            // TODO : 파이어베이스에 데이터를 쓸 수 없을 경우 다른 신고 루트 필요
        }.addOnSuccessListener {
            Toast.makeText(requireActivity(),"정상적으로 신고되었습니다.",Toast.LENGTH_LONG).show()
            // 해당 플레인 DB에서 제거
            viewModel.delete(paper!!)
            dismiss()
        }
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

        const val TAG = "DialogFlightSuccess"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(paperPlane: FirstPaperPlanes) =
            FragmentDialogFirst().apply {
                arguments = Bundle().apply {
                    putString("message", paperPlane.message)
                    putDouble("distance", paperPlane.flightDistance)
                    putLong("time", paperPlane.timestamp)
                    putString("fromId", paperPlane.fromId)
                }
                paper = paperPlane
            }
    }


}
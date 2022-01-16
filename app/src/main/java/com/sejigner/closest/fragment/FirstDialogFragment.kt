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
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.App.Companion.prefs
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.MainActivity.Companion.isOnline
import com.sejigner.closest.R
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.models.ReportMessage
import com.sejigner.closest.room.*
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.ui.PlaneBottomSheet
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class FirstDialogFragment : DialogFragment(), ReportPlaneDialogFragment.OnConfirmedListener,
    PlaneBottomSheet.OnMenuClickedListener, AlertDialogChildFragment.OnConfirmedListener {
    // TODO: Rename and change types of parameters
    private var message: String? = null
    private var distance: Double? = null
    private var time: Long? = null
    private var fromId: String? = null
    private var paper: FirstPaperPlanes? = null
    private var callback: OnSuccessListener? = null
    lateinit var repository: PaperPlaneRepository
    lateinit var factory: FragmentChatViewModelFactory
    lateinit var viewModel: FragmentChatViewModel
    private var bottomSheet: PlaneBottomSheet? = null

    interface OnSuccessListener {
        fun showReplySuccessFragment(isReply: Boolean, flightDistance: Double)
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
        viewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        cl_message_first.setOnClickListener {
            cl_fragment_dialog_first.requestDisallowInterceptTouchEvent(true)
        }

        cl_content_reply_first.setOnClickListener {
            cl_fragment_dialog_first.requestDisallowInterceptTouchEvent(true)
        }

        cl_fragment_dialog_first.setOnClickListener {
            dismiss()
        }


        var textEntered: String


        tv_dialog_time_first.text = setDateToTextView(time!!)
        tv_dialog_message_first.text = message
        tv_dialog_distance_first.text =
            getString(R.string.first_plane_dialog, convertDistanceToString(distance!!))

        addOnClickListenerMenu()


        iv_send_first_paper?.setOnClickListener {
            if(isOnline) {
                textEntered = et_dialog_message_first?.text.toString()
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
                        Toast.makeText(
                            requireActivity(),
                            resources.getText(R.string.no_internet),
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnSuccessListener {
                        viewModel.delete(paper!!)
                        callback?.showReplySuccessFragment(true, distance!!)
                        dismiss()
                    }
                } else {
                    Toast.makeText(requireActivity(), "메세지를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireActivity(), R.string.no_internet,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addOnClickListenerMenu() {
        iv_menu_first_plane.setOnClickListener {
            bottomSheet = PlaneBottomSheet()
            if (bottomSheet != null) {
                bottomSheet!!.show(childFragmentManager, bottomSheet!!.tag)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSuccessListener) {
            callback = context
        } else {
            throw RuntimeException(context.toString() + "must implement FirstPlaneListenerMain")
        }
    }

    private fun convertDistanceToString(distance: Double): String {
        return if (distance >= 1000) {
            (round((distance / 1000) * 100) / 100).toString() + "km"
        } else distance.toString() + "m"
    }

    override fun onResume() {
        super.onResume()
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

    override fun reportPaper() {

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
            // 해당 플레인 DB에서 제거
            viewModel.delete(paper!!)
            dismiss()
            Toast.makeText(requireActivity(), "비행기를 신고했어요.", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Toast.makeText(
                requireActivity(),
                resources.getText(R.string.no_internet),
                Toast.LENGTH_SHORT
            ).show()
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
            FirstDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", paperPlane.message)
                    putDouble("distance", paperPlane.flightDistance)
                    putLong("time", paperPlane.timestamp)
                    putString("fromId", paperPlane.fromId)
                }
                paper = paperPlane
            }
    }

    override fun confirmDiscardPaper() {
        bottomSheet?.dismiss()
        val alertDialog = AlertDialogChildFragment.newInstance(
            "정말 비행기를 버리시겠어요? \n한번 버린 비행기는 복구가 안 돼요.", "버리기"
        )
        val fm = childFragmentManager
        alertDialog.show(fm, "confirmation")
    }

    override fun confirmReportPaper() {
        bottomSheet?.dismiss()
        val dialog = ReportPlaneDialogFragment.newInstanceFirst(
            message!!,
            time!!
        )
        val fm = childFragmentManager
        dialog.show(fm, "report")
    }

    private fun discardPaper() {
        viewModel.delete(paper!!)
        dismiss()
        Toast.makeText(requireActivity(), "비행기를 버렸어요.", Toast.LENGTH_SHORT).show()
    }

    // 비행기 버리기
    override fun proceed() {
        discardPaper()
    }

}
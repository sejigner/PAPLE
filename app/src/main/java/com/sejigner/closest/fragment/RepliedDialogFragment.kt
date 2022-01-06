package com.sejigner.closest.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseException
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.MainActivity
import com.sejigner.closest.MainActivity.Companion.MYNICKNAME
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.models.ReportMessage
import com.sejigner.closest.room.*
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.ui.PlaneBottomSheet
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_replied.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class RepliedDialogFragment : DialogFragment(), ReportPlaneDialogFragment.OnConfirmedListener,
    PlaneBottomSheet.OnMenuClickedListener, AlertDialogChildFragment.OnConfirmedListener {
    // TODO: Rename and change types of parameters
    private var partnerMessage: String? = null
    private var distance: Double? = null
    private var replyTime: Long? = null
    private var fromId: String? = null
    private var paper: RepliedPaperPlanes? = null
    private var userMessage: String? = null
    private var firstTime: Long? = null
    var partnerNickname: String? = null
    lateinit var mCallback: OnChatStartListener
    lateinit var repository: PaperPlaneRepository
    lateinit var factory: FragmentChatViewModelFactory
    lateinit var viewModel: FragmentChatViewModel
    private var bottomSheet : PlaneBottomSheet ?= null


    interface OnChatStartListener {
        fun startChatRoom(message: ChatMessages, partnerUid: String)
        fun showLoadingDialog()
        fun dismissLoadingDialog()
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChatStartListener) {
            mCallback = context
        } else {
            throw RuntimeException(context.toString() + "must implement FirstPlaneListenerMain")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            partnerMessage = it.getString("partnerMessage")
            distance = it.getDouble("distance")
            replyTime = it.getLong("replyTime")
            fromId = it.getString("fromId")
            userMessage = it.getString("userMessage")
            firstTime = it.getLong("firstTime")
        }
    }

    override fun onResume() {
        super.onResume()
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_replied, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = PaperPlaneRepository(PaperPlaneDatabase.invoke(requireActivity()))
        factory = FragmentChatViewModelFactory(repository)
        viewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        cl_message_replied.setOnClickListener {
            cl_fragment_dialog_replied.requestDisallowInterceptTouchEvent(true)
        }

        cl_start_chat_replied_paper.setOnClickListener {
            cl_fragment_dialog_replied.requestDisallowInterceptTouchEvent(true)
        }

        cl_fragment_dialog_replied.setOnClickListener {
            dismiss()
        }

        tv_dialog_my_message.text = userMessage
        tv_dialog_message_replied.text = partnerMessage
        tv_dialog_distance_replied.text =
            getString(R.string.replied_plane_dialog, convertDistanceToString(distance!!))
        tv_dialog_time_my_message.text = setDateToTextView(firstTime!!)
        tv_dialog_time_reply.text = setDateToTextView(replyTime!!)

        addOnClickListenerMenu()

        tv_chat_yes.setOnClickListener {
            // 답장을 할 경우 메세지는 사라지고, 채팅으로 넘어가는 점 숙지시킬 것 (Dialog 이용)

            if (fromId != null) {
                if(getPartnerNickname(fromId!!)) {
                    val timestamp = System.currentTimeMillis() / 1000
                    val chatRoom =
                        ChatRooms(fromId!!, partnerNickname, UID, "대화가 시작되었습니다.", timestamp, false)
                    // 두번째 비행기 기록 삭제
                    viewModel.insert(chatRoom)
                    viewModel.delete(paper!!)
                    initChat()
                }
            }
        }

        tv_chat_no.setOnClickListener {
            dismiss()
        }
    }

    private fun getPartnerNickname(fromId: String): Boolean {
        return try {
            var result = false
            val ref2 =
                FirebaseDatabase.getInstance().getReference("/Users/$fromId")
                    .child("nickname")
            ref2.get().addOnSuccessListener {
                partnerNickname = it.value.toString()
                result = true
            }.addOnFailureListener {
                Log.d(ChatLogActivity.TAG, it.toString())
                result = false
            }
            result
        } catch (e: FirebaseException) {
            Log.d(ChatLogActivity.TAG, e.toString())
            false
        }
    }

    private fun initChat() {
        mCallback.showLoadingDialog()
        val timestamp = System.currentTimeMillis() / 1000
        val noticeMessage =
            ChatMessages(null, fromId, UID, 3, getString(R.string.init_chat_log), timestamp)
        if (invitePartner()) {
            Log.d("FragmentDialogReplied", "시작 메세지 전송 성공")
            if (fromId != null) {
                mCallback.dismissLoadingDialog()
                mCallback.startChatRoom(noticeMessage, fromId!!)
                dismiss()
            } else {
                Toast.makeText(requireActivity(), "상대 계정의 문제로 채팅 시작이 불가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun invitePartner(): Boolean {
        return try {
            val myNickName = MYNICKNAME
            var result = false
            val timestamp = System.currentTimeMillis() / 1000
            val text = resources.getString(R.string.init_chat_log)
//            val toRef =
//                FirebaseDatabase.getInstance().getReference("/User-messages/$fromId/$UID")
//                    .push()
//            val chatMessage = ChatMessage(toRef.key!!, UID, text, UID, fromId!!, timestamp)
            val lastMessagesPartnerReference =
                FirebaseDatabase.getInstance().getReference("/Latest-messages/$fromId/$UID")
            val lastMessageToPartner = LatestChatMessage(fromId!!, myNickName, text, timestamp)
            lastMessagesPartnerReference.setValue(lastMessageToPartner).addOnSuccessListener {
                Log.d(ChatLogActivity.TAG, "sent your message: $fromId")
                result = true
            }.addOnFailureListener {
                Log.d(ChatLogActivity.TAG, it.toString())
                result = false
            }
            result
        } catch (e: FirebaseException) {
            Log.d(ChatLogActivity.TAG, e.toString())
            false
        }
    }

    private fun addOnClickListenerMenu() {
        iv_menu_replied_plane.setOnClickListener {
            bottomSheet = PlaneBottomSheet()
            if(bottomSheet!=null) {
                bottomSheet!!.show(childFragmentManager, bottomSheet!!.tag)
            }
        }
    }

    private fun convertDistanceToString(distance: Double): String {
        return if (distance >= 1000) {
            (round((distance / 1000) * 100) / 100).toString() + "km"
        } else distance.toString() + "m"
    }


    private fun setDateToTextView(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        return sdf.format(timestamp * 1000L)
    }

    override fun reportPaper() {
        val fromId = fromId!!
        val message = partnerMessage!!
        val uid = MainActivity.UID

        val ref =
            FirebaseDatabase.getInstance().getReference("/Reports/Plane/$uid/$fromId")

        val reportMessage = ReportMessage(
            uid,
            message,
            System.currentTimeMillis() / 1000L
        )

        ref.setValue(reportMessage).addOnFailureListener {
            Log.e("Report", "${it.message}")
            // TODO : 파이어베이스에 데이터를 쓸 수 없을 경우 다른 신고 루트 필요
        }.addOnSuccessListener {
            Toast.makeText(requireActivity(), "정상적으로 신고되었습니다.", Toast.LENGTH_LONG).show()
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(paperPlane: RepliedPaperPlanes) =
            RepliedDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("partnerMessage", paperPlane.partnerMessage)
                    putDouble("distance", paperPlane.flightDistance)
                    putLong("replyTime", paperPlane.replyTimestamp)
                    putLong("firstTime", paperPlane.firstTimestamp)
                    putString("fromId", paperPlane.fromId)
                    putString("userMessage", paperPlane.userMessage)
                }
                paper = paperPlane
            }
    }

    override fun confirmDiscardPaper() {
        bottomSheet?.dismiss()
        val alertDialog = AlertDialogChildFragment.newInstance(
            "이 비행기를 버리시겠어요? \n버린 비행기는 복구가 안 돼요!", "버리기"
        )
        val fm = childFragmentManager
        alertDialog.show(fm, "confirmation")
    }

    override fun confirmReportPaper() {
        bottomSheet?.dismiss()
        val dialog = ReportPlaneDialogFragment.newInstanceReplied(
            partnerMessage!!,
            replyTime!!
        )
        val fm = childFragmentManager
        dialog.show(fm, "report")
    }

    // 비행기 버리기
    override fun proceed() {
        discardPaper()
    }

    private fun discardPaper() {
        viewModel.delete(paper!!)
        dismiss()
        Toast.makeText(requireActivity(), "비행기를 버렸어요.", Toast.LENGTH_SHORT).show()
    }

}
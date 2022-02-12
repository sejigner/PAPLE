package com.gievenbeck.paple.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.gievenbeck.paple.ChatLogActivity
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.R
import com.gievenbeck.paple.models.LatestChatMessage
import com.gievenbeck.paple.models.ReportMessage
import com.gievenbeck.paple.room.*
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
import com.gievenbeck.paple.ui.PlaneBottomSheet
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
    lateinit var callback: OnChatStartListener
    lateinit var repository: PaperPlaneRepository
    lateinit var factory: FragmentChatViewModelFactory
    lateinit var viewModel: FragmentChatViewModel
    private var bottomSheet: PlaneBottomSheet? = null


    interface OnChatStartListener {
        fun startChatRoom(message: ChatMessages, partnerUid: String)
        fun showLoadingDialog()
        fun dismissLoadingDialog()
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChatStartListener) {
            callback = context
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

        cl_bottom.setOnClickListener {
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
            if (fromId != null) {
                getPartnerNicknameFromFirebase(object : PartnerNicknameCallback {
                    override fun onCallback(partnerNickname: String) {
                        if (partnerNickname.isNotEmpty()) {
                            val timestamp = System.currentTimeMillis() / 1000
                            val chatRoom =
                                ChatRooms(
                                    fromId!!,
                                    partnerNickname,
                                    UID,
                                    "대화가 시작되었습니다.",
                                    timestamp,
                                    false
                                )
                            viewModel.insert(chatRoom)
                            viewModel.delete(paper!!)
                            initChat()
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                "상대방 계정을 찾을 수 없습니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
            }
        }

        tv_dialog_message_replied.movementMethod= ScrollingMovementMethod()

        tv_chat_no.setOnClickListener {
            dismiss()
        }
    }

    interface PartnerNicknameCallback {
        fun onCallback(partnerNickname: String)
    }

    private fun getPartnerNicknameFromFirebase(firebaseCallback: PartnerNicknameCallback) {
        val refNickname =
            FirebaseDatabase.getInstance().getReference("/Users/$fromId")
                .child("nickname")
        refNickname.get().addOnSuccessListener {
            firebaseCallback.onCallback(it.value.toString())
        }.addOnFailureListener {
            Log.d(ChatLogActivity.TAG, it.toString())
            firebaseCallback.onCallback("")
        }
    }

    interface ChatStartCallback {
        fun onCallback()
    }

    private fun initChat() {
        callback.showLoadingDialog()

        invitePartner(object : ChatStartCallback {
            override fun onCallback() {
                Log.d("FragmentDialogReplied", "시작 메세지 전송 성공")

                val timestamp = System.currentTimeMillis() / 1000
                val noticeMessage =
                    ChatMessages(null, fromId, UID, 3, getString(R.string.init_chat_log), timestamp)

                if (fromId != null) {
                    callback.dismissLoadingDialog()
                    callback.startChatRoom(noticeMessage, fromId!!)
                    dismiss()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "상대 계정의 문제로 채팅 시작이 불가능합니다.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        })
    }

    private fun invitePartner(firebaseCallback: ChatStartCallback) {
        val timestamp = System.currentTimeMillis() / 1000
        val text = resources.getString(R.string.init_chat_log)
        val lastMessagesPartnerReference =
            FirebaseDatabase.getInstance().getReference("/Latest-messages/$fromId/$UID")
        val lastMessageToPartner = LatestChatMessage(fromId!!, text, timestamp)
        lastMessagesPartnerReference.setValue(lastMessageToPartner).addOnSuccessListener {
            Log.d(ChatLogActivity.TAG, "sent your message: $fromId")
            firebaseCallback.onCallback()
        }.addOnFailureListener {
            Toast.makeText(
                requireActivity(),
                resources.getText(R.string.no_internet),
                Toast.LENGTH_SHORT
            ).show()
            Log.e(ChatLogActivity.TAG, it.toString())
        }
    }

    private fun addOnClickListenerMenu() {
        iv_menu_replied_plane.setOnClickListener {
            bottomSheet = PlaneBottomSheet()
            if (bottomSheet != null) {
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
        val uid = UID

        val ref =
            FirebaseDatabase.getInstance().getReference("/Reports/Plane/$uid/$fromId")

        val reportMessage = ReportMessage(
            fromId,
            message,
            System.currentTimeMillis() / 1000L
        )

        ref.setValue(reportMessage).addOnSuccessListener {
            Toast.makeText(requireActivity(), R.string.success_report, Toast.LENGTH_LONG).show()
            // 해당 플레인 DB에서 제거
            viewModel.delete(paper!!)
            dismiss()
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
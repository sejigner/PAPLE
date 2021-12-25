package com.sejigner.closest.fragment

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
import androidx.fragment.app.Fragment
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
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_second.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogReplied.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDialogReplied : DialogFragment(), ReportPlaneDialogFragment.RepliedPlaneCallback {
    // TODO: Rename and change types of parameters
    private var partnerMessage: String? = null
    private var distance: Double? = null
    private var replyTime: Long? = null
    private var fromId: String? = null
    private var paper: RepliedPaperPlanes? = null
    private var userMessage: String? = null
    private var firstTime: Long? = null

    lateinit var repository: PaperPlaneRepository
    lateinit var factory: FragmentChatViewModelFactory
    lateinit var viewModel: FragmentChatViewModel


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

    override fun onStart() {
        super.onStart()
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
        return inflater.inflate(R.layout.fragment_dialog_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = PaperPlaneRepository(PaperPlaneDatabase.invoke(requireActivity()))
        factory = FragmentChatViewModelFactory(repository)
        viewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)


        tv_dialog_my_message.text = userMessage
        tv_dialog_message_replied.text = partnerMessage
        tv_dialog_distance_replied.text =
            getString(R.string.replied_plane_dialog, convertDistanceToString(distance!!))
        tv_dialog_time_my_message.text = setDateToTextView(firstTime!!)
        tv_dialog_time_reply.text = setDateToTextView(replyTime!!)

        tv_discard_replied_plane.setOnClickListener {
            viewModel.delete(paper!!)
            dismiss()
        }

        tv_report_replied_paper.setOnClickListener {
            val dialog = ReportPlaneDialogFragment.newInstanceReplied(
                partnerMessage!!,
                replyTime!!
            )
            val fm = childFragmentManager
            dialog.show(fm, "report")
        }

        addOnClickListenerMenu()

        tv_chat_yes.setOnClickListener {
            // 답장을 할 경우 메세지는 사라지고, 채팅으로 넘어가는 점 숙지시킬 것 (Dialog 이용)
            var partnerNickname: String? = null
            val ref2 =
                FirebaseDatabase.getInstance().getReference("/Users/$fromId")
                    .child("nickname")
            ref2.get().addOnSuccessListener {
                partnerNickname = it.value.toString()
                val timestamp = System.currentTimeMillis() / 1000
                val chatRoom = ChatRooms(fromId!!, partnerNickname, UID, "대화가 시작되었습니다.", timestamp,false)
                // 두번째 비행기 기록 삭제
                viewModel.insert(chatRoom)
                viewModel.delete(paper!!)
                initChatLog()
                dismiss()
            }.addOnFailureListener {
                Toast.makeText(requireActivity(), "상대방의 계정을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initChatLog() {
        val timestamp = System.currentTimeMillis() / 1000
//        val text = resources.getString(R.string.init_chat_log)
//        val toRef =
//            FirebaseDatabase.getInstance().getReference("/User-messages/$partnerUid/$UID").push()
//        val chatMessage = ChatMessage(toRef.key!!, UID, text, UID, partnerUid!!, timestamp)
//        toRef.setValue(chatMessage).addOnSuccessListener {
//            Log.d(TAG, "sent your message: ${toRef.key}")
        val noticeMessage =
            ChatMessages(null, fromId, UID, 3, getString(R.string.init_chat_log), timestamp)
        invitePartner()
        Log.d("FragmentDialogReplied", "시작 메세지 전송 성공")


        viewModel.insert(noticeMessage)
        activity?.let {
            val intent = Intent(context, ChatLogActivity::class.java)
            intent.putExtra(FragmentChat.USER_KEY, fromId)
            startActivity(intent)
            dismiss()
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
        iv_menu_first_plane.setOnClickListener {
            if(cl_start_chat_replied_paper.visibility == View.VISIBLE) {
                cl_start_chat_replied_paper.visibility = View.GONE
                cl_menu_replied_plane.visibility = View.VISIBLE
            } else {
                cl_start_chat_replied_paper.visibility = View.VISIBLE
                cl_menu_replied_plane.visibility = View.GONE
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

    override fun reportFirebase() {
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
            FragmentDialogReplied().apply {
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


}
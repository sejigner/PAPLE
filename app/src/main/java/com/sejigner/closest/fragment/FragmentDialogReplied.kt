package com.sejigner.closest.fragment

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.MainActivity
import com.sejigner.closest.R
import com.sejigner.closest.models.ReportMessage
import com.sejigner.closest.room.ChatRooms
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.room.RepliedPaperPlanes
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_second.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
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
class FragmentDialogReplied : DialogFragment(), FragmentDialogReport.RepliedPlaneCallback {
    // TODO: Rename and change types of parameters
    private var partnerMessage: String? = null
    private var distance: Double? = null
    private var replyTime: Long? = null
    private var fromId: String?= null
    private var paper : RepliedPaperPlanes?= null
    private var userMessage : String? = null
    private var firstTime : Long?= null
    private var mCallback: RepliedPaperListener? = null

    lateinit var repository : PaperPlaneRepository
    lateinit var factory : FragmentChatViewModelFactory
    lateinit var viewModel : FragmentChatViewModel


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

    interface RepliedPaperListener {
        fun initChatLog()
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
        viewModel = ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)


        tv_dialog_my_message.text = userMessage
        tv_dialog_message_replied.text = partnerMessage
        tv_dialog_distance_replied.text = getString(R.string.replied_plane_dialog,convertDistanceToString(distance!!))
        tv_dialog_time_my_message.text = setDateToTextView(firstTime!!)
        tv_dialog_time_reply.text = setDateToTextView(replyTime!!)

        // 버리기 -> 파이어베이스 데이터 삭제
        tv_dialog_discard_replied.setOnClickListener {
            viewModel.delete(paper!!)
            dismiss()
        }

        tv_report_replied.setOnClickListener {
            val dialog = FragmentDialogReport.newInstanceReplied(
                partnerMessage!!,
                replyTime!!
            )
            val fm = childFragmentManager
            dialog.show(fm, "report")
        }


        tv_chat_yes.setOnClickListener {
            // 답장을 할 경우 메세지는 사라지고, 채팅으로 넘어가는 점 숙지시킬 것 (Dialog 이용)
            var partnerNickname : String
            val ref2 =
                FirebaseDatabase.getInstance().getReference("/Users/$fromId")
                    .child("strNickname")
            ref2.get().addOnSuccessListener {
                partnerNickname = it.value.toString()
                val chatRoom = ChatRooms(fromId!!, partnerNickname, "", -1)
                CoroutineScope(IO).launch {
                    viewModel.insert(chatRoom).join()
                    val intent = Intent(view.context,ChatLogActivity::class.java)
                    intent.putExtra(FragmentChat.USER_KEY, fromId)
                    startActivity(intent)

                    mCallback?.initChatLog()

                    // 첫번째 비행기 기록 삭제
                    val firstPaperPlaneRecord = viewModel.getWithId(fromId!!).await()
                    if(firstPaperPlaneRecord != null) {
                        viewModel.delete(firstPaperPlaneRecord)

                    // 두번째 비행기 기록 삭제
                    viewModel.delete(paper!!)

                    }
                    dismiss()
                }
            }.addOnFailureListener {
                Toast.makeText(requireActivity(),"상대방의 계정을 찾을 수 없습니다.",Toast.LENGTH_LONG).show()
            }



        }
    }

    private fun convertDistanceToString(distance : Double) : String {
        return if(distance >= 1000) {
            (round((distance/1000)*100) /100).toString() + "km"
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
            fromId,
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
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(paperPlane : RepliedPaperPlanes) =
            FragmentDialogReplied().apply {
                arguments = Bundle().apply {
                    putString("partnerMessage", paperPlane.partnerMessage)
                    putDouble("distance", paperPlane.flightDistance)
                    putLong("replyTime", paperPlane.replyTimestamp)
                    putLong("firstTime",paperPlane.firstTimestamp)
                    putString("fromId", paperPlane.fromId)
                    putString("userMessage", paperPlane.userMessage)
                }
                paper = paperPlane
            }
    }


}
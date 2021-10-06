package com.sejigner.closest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.Adapter.ChatLogAdapter
import com.sejigner.closest.MainActivity.Companion.MYNICKNAME
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.fragment.FragmentChat
import com.sejigner.closest.fragment.FragmentDialogFirst
import com.sejigner.closest.fragment.FragmentDialogReplied
import com.sejigner.closest.fragment.FragmentDialogReportChat
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.room.ChatMessages
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity(), FragmentDialogReplied.RepliedPaperListener {

    companion object {
        const val TAG = "ChatLog"
    }

    private var fbDatabase: FirebaseDatabase? = null
    var partnerUid: String? = null
    lateinit var ViewModel: FragmentChatViewModel
    lateinit var chatLogAdapter: ChatLogAdapter
    lateinit var partnerNickname : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)
        ViewModel = ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]

        fbDatabase = FirebaseDatabase.getInstance()
        partnerUid = intent.getStringExtra(FragmentChat.USER_KEY)
        chatLogAdapter = ChatLogAdapter(listOf(), ViewModel)
        rv_chat_log.adapter = chatLogAdapter


        val mLayoutManagerMessages = LinearLayoutManager(this)
        mLayoutManagerMessages.orientation = LinearLayoutManager.VERTICAL

        rv_chat_log.layoutManager = mLayoutManagerMessages
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        ViewModel.allChatMessages(partnerUid!!).observe(this, {
            chatLogAdapter.list = it
            chatLogAdapter.notifyDataSetChanged()
        })

        CoroutineScope(IO).launch {
            if (!partnerUid.isNullOrBlank()) {
                val chatRoom = ViewModel.getChatRoom(partnerUid!!).await()
                tv_partner_nickname_chat_log.text = chatRoom.partnerNickname
                partnerNickname = chatRoom.partnerNickname!!
            }
        }


//        val ref = fbDatabase?.reference?.child("Users")?.child(partnerUid!!)?.child("strNickname")
//            ref?.get()?.addOnSuccessListener {
//            supportActionBar?.title = it.value.toString()
//        }

        listenForMessages()

        btn_send_chat_log.setOnClickListener {
            performSendMessage()
        }

        iv_back_chat_log.setOnClickListener {
            super.onBackPressed()
        }

        btn_leave_menu_chat_log.setOnClickListener {
            CoroutineScope(IO).launch {
                val chatroom = ViewModel.getChatRoom(partnerUid!!).await()
                ViewModel.delete(chatroom)
            }
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        iv_menu_chat_log.setOnClickListener {
            menuToggle()
        }

        btn_report_menu_chat_log.setOnClickListener {
            val dialog = FragmentDialogReportChat()
            val fm = supportFragmentManager
            dialog.show(fm, "reportChatMessage")
        }


    }

    private fun menuToggle() {
        if (expandable_menu_chat_log.visibility == View.VISIBLE) {
            expandable_menu_chat_log.visibility  = View.GONE
        } else {
            expandable_menu_chat_log.visibility = View.VISIBLE
        }
    }

    private fun listenForMessages() {

            val ref = FirebaseDatabase.getInstance().getReference("/User-messages/$UID/$partnerUid")

            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatMessage = snapshot.getValue(ChatMessage::class.java)
                    val currentMessageDate = getDateTime(chatMessage!!.timestamp)
                    val partnerUid: String = setPartnerId(chatMessage.fromId, chatMessage)
                    val isPartner = setSender(chatMessage.fromId)


                    val chatMessages = ChatMessages(
                        null,
                        partnerUid,
                        isPartner,
                        chatMessage.message,
                        chatMessage.timestamp
                    )


                    CoroutineScope(IO).launch {
                        var lastMessageTimeStamp : Long? = 0L
                        var lastMessageDate : String?

                        lastMessageTimeStamp = ViewModel.getChatRoomsTimestamp(partnerUid).await()
                        lastMessageDate = getDateTime(lastMessageTimeStamp!!)

                        if (!lastMessageDate.equals(currentMessageDate)) {
                            lastMessageDate = currentMessageDate
                            val dateMessage = ChatMessages(null,partnerUid,2,lastMessageDate,0L)
                            ViewModel.insert(dateMessage).join()
                        }

                        ViewModel.insert(chatMessages)
                        ref.child(snapshot.key!!).removeValue()

                    }
                    rv_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun setPartnerId(fromId: String, chatMessage: ChatMessage): String {
        if (fromId == UID) {
            return chatMessage.toId
        } else return chatMessage.fromId
    }

    private fun setSender(partnerId: String): Int {
        return if(partnerId != UID) 1
        else 0
    }

    private fun getDateTime(time: Long): String? {
        try {
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val netDate = Date(time * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
            return e.toString()
        }
    }


    private fun performSendMessage() {
        val text = et_message_chat_log.text.toString()
        val fromId = UID
        val toId = partnerUid
        val timestamp = System.currentTimeMillis() / 1000
        val currentMessageDate = getDateTime(timestamp)

        et_message_chat_log.text.clear()
        rv_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)

        val toRef =
            FirebaseDatabase.getInstance().getReference("/User-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(toRef.key!!, text, fromId, toId!!, timestamp)
        toRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "sent your message: ${toRef.key}")
        }

        val lastMessagesUserReference =
            FirebaseDatabase.getInstance().getReference("/Last-messages/$UID/$toId")
        val lastMessageToMe = LatestChatMessage(partnerNickname,text,timestamp)
        lastMessagesUserReference.setValue(lastMessageToMe)

        val lastMessagesPartnerReference =
            FirebaseDatabase.getInstance().getReference("/Last-messages/$toId/$UID")
        val lastMessageToPartner = LatestChatMessage(MYNICKNAME,text,timestamp)
        lastMessagesPartnerReference.setValue(lastMessageToPartner)


        val chatMessages = ChatMessages(null, toId, 0, text, timestamp)

        CoroutineScope(IO).launch {
            var lastMessageTimeStamp: Long? = 0L
            var lastMessageDate: String?

            lastMessageTimeStamp = ViewModel.getChatRoomsTimestamp(partnerUid!!).await()
            lastMessageDate = getDateTime(lastMessageTimeStamp!!)

            if (!lastMessageDate.equals(currentMessageDate)) {
                lastMessageDate = currentMessageDate
                val dateMessage = ChatMessages(null, partnerUid, 2, lastMessageDate, 0L)
                ViewModel.insert(dateMessage).join()
            }
            ViewModel.insert(chatMessages)
        }

    }

    override fun initChatLog() {
        val timestamp = System.currentTimeMillis() / 1000

        val noticeMessage = ChatMessages(null, partnerUid, 2, getString(R.string.init_chat_log),0L)
        ViewModel.insert(noticeMessage)

        val lastMessagesUserReference =
            FirebaseDatabase.getInstance().getReference("/Last-messages/$UID/$partnerUid")
        val lastMessageToMe = LatestChatMessage(partnerNickname,getString(R.string.init_chat_log),timestamp)
        lastMessagesUserReference.setValue(lastMessageToMe)

        val lastMessagesPartnerReference =
            FirebaseDatabase.getInstance().getReference("/Last-messages/$partnerUid/$UID")
        val lastMessageToPartner = LatestChatMessage(MYNICKNAME,getString(R.string.init_chat_log),timestamp)
        lastMessagesPartnerReference.setValue(lastMessageToPartner)

    }

    fun reportMessagesFirebase() {
        // TODO : 신고 시 List<ChatMessages> -> Firebase 업로드
        CoroutineScope(IO).launch {
            val messageList = ViewModel.chatRoomAndAllMessages(partnerUid!!).await()
            val reportRef =
                FirebaseDatabase.getInstance().getReference("/Reports/Chat/$UID/$partnerUid")
            reportRef.setValue(messageList).addOnFailureListener {
                Log.d("ReportChatLog", "Report 실패")
            }.addOnSuccessListener {
                Log.d("Report","신고가 접수되었어요")
                ViewModel.deleteAllMessages(partnerUid!!)
                ViewModel.deleteChatRoom(partnerUid!!)
                startActivity(Intent(this@ChatLogActivity,MainActivity::class.java))
            }
        }

//        ViewModel.allChatMessages(partnerUid!!).observe(this, {
//            val reportRef =
//                FirebaseDatabase.getInstance().getReference("/ChatReport/$UID")
//            reportRef.setValue(it)
//        })
    }
}

//class ChatFromItem(val text: String, val time: Long) : Item<GroupieViewHolder>() {
//
//    private var lastMessageTimeMe: String? = null
//
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        viewHolder.itemView.tv_message_me.text = text
//        viewHolder.itemView.tv_time_me.text = setTime(time)
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.chat_me_row
//    }
//
//    private fun setTime(timestamp: Long): String {
//        val sdf = SimpleDateFormat("a hh:mm")
//        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
//        val date = sdf.format(timestamp * 1000L)
//        return date.toString()
//    }
//
//
//}
//
//class ChatToItem(val text: String, val time: Long) : Item<GroupieViewHolder>() {
//
//    private var lastMessageTimePartner: String? = null
//
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        viewHolder.itemView.tv_message_partner.text = text
//        viewHolder.itemView.tv_time_partner.text = setTime(time)
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.chat_partner_row
//    }
//
//    private fun setTime(timestamp: Long): String {
//        val sdf = SimpleDateFormat("a hh:mm")
//        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
//        val date = sdf.format(timestamp * 1000L)
//        return date.toString()
//    }
//
//}
//
//class ChatDate(private val lastDate: String) : Item<GroupieViewHolder>() {
//    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//        viewHolder.itemView.tv_chat_date.text = lastDate
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.chat_date
//    }
//
//}


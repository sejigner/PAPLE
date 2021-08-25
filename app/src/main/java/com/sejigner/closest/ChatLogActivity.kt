package com.sejigner.closest

import android.os.Bundle
import android.util.Log
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
import com.sejigner.closest.fragment.FragmentHome
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.room.ChatMessages
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_date.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {

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

        ViewModel.allChatMessages().observe(this, {
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

        btn_exit.setOnClickListener {
            CoroutineScope(IO).launch {
                val chatroom = ViewModel.getChatRoom(partnerUid!!).await()
                ViewModel.delete(chatroom)
            }
            super.onBackPressed()
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
        ViewModel.insert(chatMessages)

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


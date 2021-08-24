package com.sejigner.closest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.type.Date
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.UI.FragmentChatViewModelFactory
import com.sejigner.closest.fragment.FragmentChat
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_date.view.*
import kotlinx.android.synthetic.main.chat_from_row.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ChatLog"
    }

    private var fbDatabase: FirebaseDatabase? = null

    val adapter = GroupAdapter<GroupieViewHolder>()
    var partnerUid: String? = null
    lateinit var ViewModel: FragmentChatViewModel


    private var lastMessageDate : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        rv_chat_log.adapter = adapter
        fbDatabase = FirebaseDatabase.getInstance()
        partnerUid = intent.getStringExtra(FragmentChat.USER_KEY)

        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)


        ViewModel = ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]

        val ref = fbDatabase?.reference?.child("Users")?.child(partnerUid!!)?.child("strNickname")
        ref?.get()?.addOnSuccessListener {
            tv_partner_nickname_chat_log.text = it.value.toString()
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

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.message)


                        if(lastMessageDate != currentMessageDate) {
                            lastMessageDate = currentMessageDate
                            adapter.add(ChatDate(lastMessageDate!!))
                        }

                    if (chatMessage.fromId == UID) {
                        adapter.add(ChatFromItem(chatMessage.message, chatMessage.timestamp))
                    } else {
                        adapter.add(ChatToItem(chatMessage.message, chatMessage.timestamp))
                    }
                }

                rv_chat_log.scrollToPosition(adapter.itemCount - 1)

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

        val fromRef =
            FirebaseDatabase.getInstance().getReference("/User-messages/$fromId/$toId").push()
        val toRef =
            FirebaseDatabase.getInstance().getReference("/User-messages/$toId/$fromId").push()
        val chatMessage =
            ChatMessage(fromRef.key!!, text, fromId, toId!!, System.currentTimeMillis() / 1000)
        fromRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "sent your message: ${fromRef.key}")
            et_message_chat_log.text.clear()
            rv_chat_log.scrollToPosition(adapter.itemCount - 1)
        }
        toRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "sent your message: ${fromRef.key}")
        }
    }
}

class ChatFromItem(val text: String, val time: Long) : Item<GroupieViewHolder>() {

    private var lastMessageTimeMe : String? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_message_me.text = text
        viewHolder.itemView.tv_time_me.text = setTime(time)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    private fun setTime(timestamp: Long) : String {
        val sdf = SimpleDateFormat("a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = sdf.format(timestamp*1000L)
        return date.toString()
    }


}

class ChatToItem(val text: String, val time: Long) : Item<GroupieViewHolder>() {

    private var lastMessageTimePartner : String? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_message_partner.text = text
        viewHolder.itemView.tv_time_partner.text = setTime(time)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    private fun setTime(timestamp: Long) : String {
        val sdf = SimpleDateFormat("a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = sdf.format(timestamp*1000L)
        return date.toString()
    }

}

class ChatDate(private val lastDate: String) : Item<GroupieViewHolder>()  {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_date.text= lastDate
    }

    override fun getLayout(): Int {
        return R.layout.chat_date
    }

}


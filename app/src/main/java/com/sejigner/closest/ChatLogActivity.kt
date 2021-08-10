package com.sejigner.closest

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.type.Date
import com.sejigner.closest.fragment.FragmentChat
import com.sejigner.closest.models.ChatMessage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_date.view.*
import kotlinx.android.synthetic.main.chat_from_row.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ChatLog"
    }

    private var fbDatabase: FirebaseDatabase? = null

    val adapter = GroupAdapter<GroupieViewHolder>()
    var partnerUid: String? = null


    private var lastMessageDate : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        rv_chat_log.adapter = adapter
        fbDatabase = FirebaseDatabase.getInstance()
        partnerUid = intent.getStringExtra(FragmentChat.USER_KEY)

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


    }

    private fun listenForMessages() {


        val fromId = FirebaseAuth.getInstance().uid
        val toId = partnerUid
        val ref = FirebaseDatabase.getInstance().getReference("/User-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                val currentMessageDate = getDateTime(chatMessage!!.timestamp)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)


                        if(lastMessageDate != currentMessageDate) {
                            lastMessageDate = currentMessageDate
                            adapter.add(ChatDate(lastMessageDate!!))
                        }

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text, chatMessage.timestamp))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text, chatMessage.timestamp))
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
        val fromId = FirebaseAuth.getInstance().uid
        val toId = partnerUid




        if (fromId == null) return
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
        toRef.setValue(chatMessage)

        val latestMessageRef =
            FirebaseDatabase.getInstance().getReference("/Latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef =
            FirebaseDatabase.getInstance().getReference("/Latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
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
        val sdf = SimpleDateFormat("hh:mm")
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
        val sdf = SimpleDateFormat("hh:mm")
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


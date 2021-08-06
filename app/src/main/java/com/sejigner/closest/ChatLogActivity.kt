package com.sejigner.closest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.fragment.FragmentChat
import com.sejigner.closest.fragment.FragmentDialogReplied
import com.sejigner.closest.models.ChatMessage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()
    var partnerUid : String? = null

    private var fbDatabase: FirebaseDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        rv_chat_log.adapter = adapter
        fbDatabase = FirebaseDatabase.getInstance()
        partnerUid = intent.getStringExtra(FragmentChat.USER_KEY)




        val ref = fbDatabase?.reference?.child("Users")?.child(partnerUid!!)?.child("strNickname")
            ref?.get()?.addOnSuccessListener {
            supportActionBar?.title = it.value.toString()
        }

        listenForMessages()

        btn_send_chat_log.setOnClickListener {
            performSendMessage()
        }


    }

    private fun listenForMessages() {


        val fromId = FirebaseAuth.getInstance().uid
        val toId = partnerUid
        val ref = FirebaseDatabase.getInstance().getReference("/User-messages/$fromId/$toId")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatFromItem(chatMessage.text))
                    } else {
                        adapter.add(ChatToItem(chatMessage.text))
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



    private fun performSendMessage() {
        val text = et_message_chat_log.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = partnerUid




        if(fromId == null) return
        val fromRef = FirebaseDatabase.getInstance().getReference("/User-messages/$fromId/$toId").push()
        val toRef = FirebaseDatabase.getInstance().getReference("/User-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(fromRef.key!!, text, fromId, toId!!, System.currentTimeMillis() / 1000 )
        fromRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "sent your message: ${fromRef.key}")
            et_message_chat_log.text.clear()
            rv_chat_log.scrollToPosition(adapter.itemCount - 1)
        }
        toRef.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/Latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/Latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_from_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}

class ChatToItem(val text: String): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_to_row.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}
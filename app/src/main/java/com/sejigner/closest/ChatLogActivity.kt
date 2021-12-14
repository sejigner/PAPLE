package com.sejigner.closest

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseException
import com.google.firebase.database.*
import com.sejigner.closest.adapter.ChatLogAdapter
import com.sejigner.closest.MainActivity.Companion.MYNICKNAME
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.fragment.FragmentChat
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import android.widget.RelativeLayout
import com.sejigner.closest.SoftKeyboard.SoftKeyboardChanged

// TODO : 채팅방 나가기 기능 구현 - EditText 잠그기, 보내기 버튼 색상 변경
class ChatLogActivity : AppCompatActivity() {

    companion object {
        const val TAG = "ChatLog"
    }

    private var fbDatabase: FirebaseDatabase? = null
    private var partnerUid: String? = null
    private var partnerFcmToken: String? = null
    lateinit var viewModel: FragmentChatViewModel
    private lateinit var chatLogAdapter: ChatLogAdapter
    lateinit var partnerNickname: String
    lateinit var mRef: DatabaseReference
    lateinit var mListener: ChildEventListener
    lateinit var layout: RelativeLayout
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var softKeyboard: SoftKeyboard
    private var isOver: Boolean = false
    lateinit var mListenerFinish: ChildEventListener
    lateinit var mRefFinish: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        layout = layout_chat_log
        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]

        fbDatabase = FirebaseDatabase.getInstance()

        updatePartnersToken()
        partnerUid = intent.getStringExtra(FragmentChat.USER_KEY)
        chatLogAdapter = ChatLogAdapter(listOf(), viewModel)
        rv_chat_log.adapter = chatLogAdapter

        val mLayoutManagerMessages = LinearLayoutManager(this)
        mLayoutManagerMessages.orientation = LinearLayoutManager.VERTICAL
        mLayoutManagerMessages.stackFromEnd = true


        rv_chat_log.layoutManager = mLayoutManagerMessages



        viewModel.allChatMessages(UID, partnerUid!!).observe(this, {
            chatLogAdapter.list = it
            chatLogAdapter.notifyDataSetChanged()
            rv_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)
        })

//        ViewModel.allChatMessages(partnerUid!!).observe(this, {
//            chatLogAdapter.list = it
//            chatLogAdapter.notifyDataSetChanged()
//        })




//        val ref = fbDatabase?.reference?.child("Users")?.child(partnerUid!!)?.child("strNickname")
//            ref?.get()?.addOnSuccessListener {
//            supportActionBar?.title = it.value.toString()
//        }


        // 보내기 버튼 초기상태 false / 입력시 활성화
        watchEditText()

        iv_menu_chat_log.setOnClickListener {
            menuToggle()
        }

        iv_send_chat_log.setOnClickListener {
            performSendMessage()
            et_message_chat_log.text.clear()
        }

        iv_back_chat_log.setOnClickListener {
            finish()
        }

        btn_leave_menu_chat_log.setOnClickListener {
            CoroutineScope(IO).launch {
                val chatroom = viewModel.getChatRoom(UID, partnerUid!!).await()
                viewModel.delete(chatroom)
                finishChat()
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }



        btn_report_menu_chat_log.setOnClickListener {
            val dialog = FragmentDialogReportChat()
            val fm = supportFragmentManager
            dialog.show(fm, "reportChatMessage")
        }

        rv_chat_log.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                rv_chat_log.postDelayed(Runnable {
                    rv_chat_log.smoothScrollToPosition(chatLogAdapter.itemCount - 1)
                }, 100)
            }
        })

        checkChatOver()
    }

    private fun watchEditText() {
        iv_send_chat_log.isEnabled = false
        et_message_chat_log.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim { it <= ' ' }.isEmpty()) {
                    iv_send_chat_log.isEnabled = false
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim { it <= ' ' }.isNotEmpty()) {
                    iv_send_chat_log.isEnabled = true
                }
            }
        })
    }

    private fun preventSend() {
        et_message_chat_log.isEnabled = false
        iv_send_chat_log.isEnabled = false
    }


    private fun checkChatOver() {
        CoroutineScope(IO).launch {
            isOver = viewModel.isOver(UID, partnerUid!!).await()
            if (!partnerUid.isNullOrBlank()) {
                val chatRoom = viewModel.getChatRoom(UID, partnerUid!!).await()
                tv_partner_nickname_chat_log.text = chatRoom.partnerNickname
                partnerNickname = chatRoom.partnerNickname!!
            }
            if (isOver) {
                preventSend()
            }
        }
    }

    private fun listenForFinishedChat() {
        mListenerFinish = mRefFinish.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if(snapshot.value.toString()=="true") {
                    CoroutineScope(IO).launch {
                        val partnerUid = snapshot.key.toString()
                        val timestamp = System.currentTimeMillis() / 1000
                        val noticeFinish = getString(R.string.finish_chat_log)
                        val chatMessages = ChatMessages(null,
                            partnerUid,
                            UID,
                            2,
                            noticeFinish,
                            timestamp
                        )
                        viewModel.updateChatRoom(UID, partnerUid,true).join()
                        viewModel.insert(chatMessages)
                        viewModel.updateLastMessages(
                            UID,
                            partnerUid,
                            noticeFinish,
                            timestamp
                        )
                        mRefFinish.child(partnerUid).removeValue()
                        TODO("Lock the editText and update UI")
                    }
                }

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

    private fun menuToggle() {
        if (expandable_menu_chat_log.visibility == View.GONE) {
            expandable_menu_chat_log.visibility = View.VISIBLE
        } else {
            expandable_menu_chat_log.visibility = View.GONE
        }
    }


    private fun setLayoutMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.setDecorFitsSystemWindows(true)
        } else {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }


    override fun onStart() {
        super.onStart()

//        setLayoutMode()

        inputMethodManager =
            getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        softKeyboard = SoftKeyboard(layout, inputMethodManager)
        softKeyboard.setSoftKeyboardCallback(object : SoftKeyboardChanged {
            override fun onSoftKeyboardHide() {
                rv_chat_log.post(Runnable {
                    rv_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)
                })
//                Handler(Looper.getMainLooper()).post {
//                    rv_chat_log.smoothScrollToPosition(chatLogAdapter.itemCount - 1)
//                }
            }

            override fun onSoftKeyboardShow() {
//                Handler(Looper.getMainLooper()).post {
//                    rv_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)
//                }
            }
        })

        mRef = FirebaseDatabase.getInstance().getReference("/User-messages/$UID/$partnerUid")
        listenForMessages()
        mRefFinish = FirebaseDatabase.getInstance().getReference("/Latest-messages/$UID/isOver")
        listenForFinishedChat()
    }


    override fun onStop() {
        super.onStop()
        mRef.removeEventListener(mListener)
        mRefFinish.removeEventListener(mListenerFinish)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        softKeyboard.unRegisterSoftKeyboardCallback()
    }

    private fun setPartnersFcmToken() {
        val ref =
            FirebaseDatabase.getInstance().getReference("/Users/$partnerUid/registrationToken")
        ref.get()
            .addOnSuccessListener { it ->
                partnerFcmToken = it.value.toString()
            }.addOnFailureListener {
                Log.d("ChatLogActivity", it.message!!)
            }
    }


    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun updatePartnersToken() {
        val ref =
            FirebaseDatabase.getInstance().getReference("/Users/$partnerUid/registrationToken")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                partnerFcmToken = snapshot.value.toString()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                partnerFcmToken = snapshot.value.toString()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun listenForMessages() {

        mListener = mRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                CoroutineScope(IO).launch {
                    launch {
                        val isPartner = 1
                        val chatMessage: ChatMessage? = snapshot.getValue(ChatMessage::class.java)
                        val currentMessageDate: String? = getDateTime(chatMessage!!.timestamp)
                        var lastMessageTimeStamp: Long? = 0L
                        var lastMessageDate: String?
                        val chatMessages = ChatMessages(
                            null,
                            partnerUid,
                            UID,
                            isPartner,
                            chatMessage.message,
                            chatMessage.timestamp
                        )


                        lastMessageTimeStamp =
                            viewModel.getLatestTimestamp(UID, partnerUid!!).await()
                        lastMessageDate = getDateTime(lastMessageTimeStamp!!)

                        if (!lastMessageDate.equals(currentMessageDate)) {
                            lastMessageDate = currentMessageDate
                            val dateMessage =
                                ChatMessages(
                                    null,
                                    partnerUid,
                                    UID,
                                    2,
                                    lastMessageDate,
                                    chatMessage.timestamp
                                )
                            viewModel.insert(dateMessage).join()
                        }

                        val job = viewModel.insert(chatMessages)
                        if (job.isCompleted) {
                            rv_chat_log.scrollToPosition(chatLogAdapter.itemCount - 1)
                        }
                    }.join()
                    mRef.child(snapshot.key!!).removeValue()
                }
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

//    private fun setPartnerId(fromId: String, toId: String): String {
//        if (fromId == UID) {
//            return toId
//        } else return fromId
//    }
//
//    private fun setSender(partnerId: String): Int {
//        return if (partnerId != UID) 1
//        else 0
//    }

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


        val toRef =
            FirebaseDatabase.getInstance().getReference("/User-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(toRef.key!!, text, fromId, toId!!, timestamp)
        toRef.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "sent your message: ${toRef.key}")
        }

        // TODO : 필요성 고민(보내자마자 updateLastMessages 실행해서 업데이트 하는 걸로 충분하지 않나?)
        val lastMessagesUserReference =
            FirebaseDatabase.getInstance().getReference("/Latest-messages/$UID/$toId")
        val lastMessageToMe = LatestChatMessage(toId, partnerNickname, text, timestamp)
        lastMessagesUserReference.setValue(lastMessageToMe)

        val lastMessagesPartnerReference =
            FirebaseDatabase.getInstance().getReference("/Latest-messages/$toId/$UID")
        val lastMessageToPartner = LatestChatMessage(toId,MYNICKNAME, text, timestamp)
        lastMessagesPartnerReference.setValue(lastMessageToPartner)


        val chatMessages = ChatMessages(null, toId, UID, 0, text, timestamp)

        CoroutineScope(IO).launch {
            var lastMessageTimeStamp: Long? = 0L
            var lastMessageDate: String?

            lastMessageTimeStamp = viewModel.getLatestTimestamp(UID, partnerUid!!).await()
            lastMessageDate = getDateTime(lastMessageTimeStamp!!)

            if (!lastMessageDate.equals(currentMessageDate)) {
                lastMessageDate = currentMessageDate
                val dateMessage = ChatMessages(null, partnerUid, UID, 2, lastMessageDate, timestamp)
                viewModel.insert(dateMessage).join()
            }
            viewModel.insert(chatMessages)
            viewModel.updateLastMessages(
                UID,
                partnerUid!!,
                text,
                timestamp
            ).join()
        }
    }

    private fun finishChat(): Boolean {
        return try {
            var result = false

            val lastMessagesPartnerReference = FirebaseDatabase.getInstance()
                    .getReference("/Latest-messages/$partnerUid/isOver/$UID")
            FirebaseDatabase.getInstance().getReference("/Latest-messages/$partnerUid/isOver/$UID")
            lastMessagesPartnerReference.setValue("true").addOnSuccessListener {
                Log.d(ChatLogActivity.TAG, "finished the chat: $partnerUid")
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

    fun initChatLog() {
        val timestamp = System.currentTimeMillis() / 1000
//        val text = resources.getString(R.string.init_chat_log)
//        val toRef =
//            FirebaseDatabase.getInstance().getReference("/User-messages/$partnerUid/$UID").push()
//        val chatMessage = ChatMessage(toRef.key!!, UID, text, UID, partnerUid!!, timestamp)
//        toRef.setValue(chatMessage).addOnSuccessListener {
//            Log.d(TAG, "sent your message: ${toRef.key}")
        val noticeMessage =
            ChatMessages(null, partnerUid, UID, 3, getString(R.string.init_chat_log), timestamp)
        CoroutineScope(IO).launch {
            var result: Boolean
            runBlocking {
                result = sendMessage()
            }
            if (result) {
                viewModel.insert(noticeMessage)
            } else {
                Toast.makeText(this@ChatLogActivity, "상대방과의 연결에 실패하였습니다.", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

//        val lastMessagesUserReference =
//            FirebaseDatabase.getInstance().getReference("/Last-messages/$UID/$partnerUid")
//        val lastMessageToMe = LatestChatMessage(partnerNickname,getString(R.string.init_chat_log),timestamp)
//        lastMessagesUserReference.setValue(lastMessageToMe)
//
//        val lastMessagesPartnerReference =
//            FirebaseDatabase.getInstance().getReference("/Last-messages/$partnerUid/$UID")
//        val lastMessageToPartner = LatestChatMessage(MYNICKNAME,getString(R.string.init_chat_log),timestamp)
//        lastMessagesPartnerReference.setValue(lastMessageToPartner)


    private suspend fun sendMessage(): Boolean {
        return try {
            var result = false
            val timestamp = System.currentTimeMillis() / 1000
            val text = resources.getString(R.string.init_chat_log)
            val toRef =
                FirebaseDatabase.getInstance().getReference("/User-messages/$partnerUid/$UID")
                    .push()
            val chatMessage = ChatMessage(toRef.key!!, text, UID, partnerUid!!, timestamp)
            toRef.setValue(chatMessage).addOnSuccessListener {
                Log.d(TAG, "sent your message: ${toRef.key}")
                result = true
            }.addOnFailureListener {
                Log.d(TAG, it.toString())
                result = false
            }.await()
            result
        } catch (e: FirebaseException) {
            Log.d(TAG, e.toString())
            false
        }
    }

    fun reportMessagesFirebase() {
        // TODO : 신고 시 List<ChatMessages> -> Firebase 업로드
        CoroutineScope(IO).launch {
            // TODO : chatRoomAndAllMessages 중첩된 관계 정의 (https://developer.android.com/training/data-storage/room/relationships)
            val messageList = viewModel.chatRoomAndAllMessages(UID, partnerUid!!).await()
            val reportRef =
                FirebaseDatabase.getInstance().getReference("/Reports/Chat/$UID/$partnerUid")
            reportRef.setValue(messageList).addOnFailureListener {
                Log.d("ReportChatLog", "Report 실패")
            }.addOnSuccessListener {
                Log.d("Report", "신고가 접수되었어요")
                viewModel.deleteAllMessages(UID, partnerUid!!)
                viewModel.deleteChatRoom(UID, partnerUid!!)
                startActivity(Intent(this@ChatLogActivity, MainActivity::class.java))
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


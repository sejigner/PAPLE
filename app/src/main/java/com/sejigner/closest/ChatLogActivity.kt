package com.sejigner.closest

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.FirebaseException
import com.google.firebase.database.*
import com.sejigner.closest.App.Companion.prefs
import com.sejigner.closest.MainActivity.Companion.MYNICKNAME
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.adapter.ChatLogAdapter
import com.sejigner.closest.fragment.AlertDialogFragment
import com.sejigner.closest.fragment.FragmentChat
import com.sejigner.closest.fragment.ReportChatDialogFragment
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.room.ChatMessages
import com.sejigner.closest.room.FinishedChat
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.ui.ChatBottomSheet
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.ui.SoftKeyboard
import com.sejigner.closest.ui.SoftKeyboard.SoftKeyboardChanged
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

// TODO : 채팅방 나가기 기능 구현 - EditText 잠그기, 보내기 버튼 색상 변경
class ChatLogActivity : AppCompatActivity(), ChatBottomSheet.BottomSheetChatLogInterface, AlertDialogFragment.OnConfirmedListener, ReportChatDialogFragment.OnReportConfirmedListener {

    companion object {
        const val TAG = "ChatLog"
    }

    private var fbDatabase: FirebaseDatabase? = null
    private var partnerUid: String? = null
    private var partnerFcmToken: String? = null
    lateinit var viewModel: FragmentChatViewModel
    private lateinit var chatLogAdapter: ChatLogAdapter
    lateinit var partnerNickname: String
    lateinit var mMessageRef: DatabaseReference
    lateinit var mMessageListener: ChildEventListener
    lateinit var layout: RelativeLayout
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var softKeyboard: SoftKeyboard
    lateinit var mFinishListener: ChildEventListener
    lateinit var mFinishRef: DatabaseReference
    lateinit var mPartnersTokenRef : DatabaseReference
    lateinit var mPartnersTokenListener : ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        layout = layout_chat_log
        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]

        fbDatabase = FirebaseDatabase.getInstance()

        partnerUid = intent.getStringExtra(FragmentChat.USER_KEY)
        chatLogAdapter = ChatLogAdapter(listOf(), viewModel)
        rv_chat_log.adapter = chatLogAdapter

        CoroutineScope(IO).launch {
            partnerNickname = viewModel.getChatRoom(UID,partnerUid!!).await().partnerNickname!!
            tv_partner_nickname_chat_log.text = partnerNickname
        }

        val mLayoutManagerMessages = LinearLayoutManager(this)
        mLayoutManagerMessages.orientation = LinearLayoutManager.VERTICAL
        mLayoutManagerMessages.reverseLayout = true
        mLayoutManagerMessages.stackFromEnd = true

        rv_chat_log.layoutManager = mLayoutManagerMessages

        viewModel.allChatMessages(UID, partnerUid!!).observe(this, {
            chatLogAdapter.differ.submitList(it)
            rv_chat_log.scrollToPosition(0)
        })

        // 보내기 버튼 초기상태 false / 입력시 활성화
        watchEditText()

        iv_menu_chat_log.setOnClickListener {
            val bottomSheet = ChatBottomSheet()
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

        iv_send_chat_log.setOnClickListener {
            performSendMessage()

        }

        iv_back_chat_log.setOnClickListener {
            finish()
        }

        rv_chat_log.addOnLayoutChangeListener(View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                rv_chat_log.postDelayed(Runnable {
                    rv_chat_log.smoothScrollToPosition(0)
                }, 100)
            }
        })

        checkChatOver()
        setPartnerToPrefs()
    }

    private fun setPartnerToPrefs() {
        if(partnerUid!=null) {
            prefs.setString("partner", partnerUid!!)
        }
    }

    private fun removePartnerFromPrefs() {
        prefs.setString("partner", "")
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
            if (viewModel.isOver(UID, partnerUid!!).await()) {
                preventSend()
            }
        }
    }

    private fun listenForFinishedChat() {
        mFinishListener = mFinishRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if(snapshot.value==true) {
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
                        mFinishRef.child(partnerUid).removeValue()
                        preventSend()
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


    override fun onStart() {
        super.onStart()

        mMessageRef = FirebaseDatabase.getInstance().getReference("/User-messages/$UID/$partnerUid")
        listenForMessages()
        mFinishRef = FirebaseDatabase.getInstance().getReference("/Latest-messages/$UID/isOver")
        listenForFinishedChat()
        mPartnersTokenRef =
            FirebaseDatabase.getInstance().getReference("/Users/$partnerUid/registrationToken")
        listenForPartnersToken()

        inputMethodManager =
            getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        softKeyboard =
            SoftKeyboard(layout, inputMethodManager)
        softKeyboard.setSoftKeyboardCallback(object : SoftKeyboardChanged {
            override fun onSoftKeyboardHide() {
                rv_chat_log.post(Runnable {
                    rv_chat_log.scrollToPosition(0)
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
    }

    override fun onStop() {
        super.onStop()
        mMessageRef.removeEventListener(mMessageListener)
        mFinishRef.removeEventListener(mFinishListener)
        mPartnersTokenRef.removeEventListener(mPartnersTokenListener)
        removePartnerFromPrefs()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        softKeyboard.unRegisterSoftKeyboardCallback()
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


    private fun listenForPartnersToken() {


        mPartnersTokenListener = mPartnersTokenRef.addChildEventListener(object : ChildEventListener {
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

        mMessageListener = mMessageRef.addChildEventListener(object : ChildEventListener {

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
                    mMessageRef.child(snapshot.key!!).removeValue()
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
        lastMessagesPartnerReference.setValue(lastMessageToPartner).addOnSuccessListener {
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
                et_message_chat_log.text.clear()
            }
        }.addOnFailureListener {
            Toast.makeText(
                this,
                resources.getText(R.string.no_internet),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun confirmChatLeave() {
        val alertDialog = AlertDialogFragment.newInstance(
            "정말 채팅방을 나가시겠어요?", "나가기"
        )
        val fm = supportFragmentManager
        alertDialog.show(fm, "confirmation")
    }

    private fun leaveChatRoom() {
        sendFinishSignalToFirebase()
        val intent = Intent(this, MainActivity::class.java)
        CoroutineScope(IO).launch {
            val chatroom = viewModel.getChatRoom(UID, partnerUid!!).await()
            viewModel.delete(chatroom)
            confirmChatLeave()
            startActivity(intent)
            finish()
        }
    }

    override fun reportPartner() {
        val dialog = ReportChatDialogFragment()
        val fm = supportFragmentManager
        dialog.show(fm, "reportChatMessage")
    }

    private fun sendFinishSignalToFirebase(): Boolean {
        return try {
            var result = false

            val lastMessagesPartnerReference = FirebaseDatabase.getInstance()
                    .getReference("/Finished-chat/$partnerUid/isOver/$UID")
            lastMessagesPartnerReference.setValue(true).addOnSuccessListener {
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

    override fun reportMessagesFirebase() {
        // TODO : 신고 시 List<ChatMessages> -> Firebase 업로드
        CoroutineScope(IO).launch {
            // TODO : chatRoomAndAllMessages 중첩된 관계 정의 (https://developer.android.com/training/data-storage/room/relationships)
            val messageList = viewModel.chatRoomAndAllMessages(UID, partnerUid!!).await()
            val reportRef =
                FirebaseDatabase.getInstance().getReference("/Reports/Chat/$UID/$partnerUid")
            reportRef.setValue(messageList).addOnFailureListener {
                Log.d("ReportChatLog", "Report 실패")
                Toast.makeText(
                    this@ChatLogActivity,
                    resources.getText(R.string.no_internet),
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnSuccessListener {
                Log.d("Report", "신고가 접수되었어요")
                leaveChatRoom()
            }
        }

//        ViewModel.allChatMessages(partnerUid!!).observe(this, {
//            val reportRef =
//                FirebaseDatabase.getInstance().getReference("/ChatReport/$UID")
//            reportRef.setValue(it)
//        })
    }

    override fun proceed() {
        leaveChatRoom()
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


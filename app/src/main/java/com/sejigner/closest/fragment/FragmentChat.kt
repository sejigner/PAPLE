package com.sejigner.closest.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.google.firebase.database.*
import com.sejigner.closest.adapter.FirstPaperPlaneAdapter
import com.sejigner.closest.adapter.LatestMessageAdapter
import com.sejigner.closest.adapter.RepliedPaperPlaneAdapter
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.MainActivity
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.interfaces.FirstPlaneListener
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.models.Users
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.room.*
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.latest_chat_row.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.text.SimpleDateFormat
import java.util.*

// TODO : ChatLogActivity 내 chatRoomAndAllMessages 참고하여 일대다관계 데이터 활용
//  chatRoomAndAllMessages ( UID - ChatRoomId - Message 구조)
//  planes (UID - Planes)
//  ※ 수정하기 전, 각 리사이클러뷰 Adapter의 자료형 참고할 것
//  Ex) FirstPaperPlane -> Uid -> Uid를 필드 값으로 갖는 LiveData 형태의 FirstPlanesWithUid 리스트 반환 -> observe
class FragmentChat : Fragment(), FirstPlaneListener {

    companion object {
        const val TAG = "FragmentChat"
        const val USER_KEY = "USER_KEY"
    }

//    private val adapterHorizontalFirst = GroupAdapter<GroupieViewHolder>()
//    private val adapterHorizontalReplied = GroupAdapter<GroupieViewHolder>()
//    private val adapterVertical = GroupAdapter<GroupieViewHolder>()
//    private val firstPlaneKeyList = ArrayList<String>()
//    private val repliedPlaneKeyList = ArrayList<String>()
//    private val messageKeyList = ArrayList<String>()
//    private var db: PaperPlaneDatabase? = null
    lateinit var ViewModel: FragmentChatViewModel
    lateinit var list: List<FirstPaperPlanes>
    lateinit var mRefPlane : DatabaseReference
    lateinit var mRefMessages : DatabaseReference
    lateinit var mRefFinish : DatabaseReference
    lateinit var mListenerPlane : ChildEventListener
    lateinit var mListenerMessages : ChildEventListener
    lateinit var mListenerFinish : ChildEventListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid = MainActivity.getUid()
        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)
        // initialized View Model
        ViewModel =
            ViewModelProvider(this, factory).get(FragmentChatViewModel::class.java)
        val firstPlaneAdapter = FirstPaperPlaneAdapter(listOf(), ViewModel) { FirstPaperPlanes ->

            val dialog = FragmentDialogFirst.newInstance(
                FirstPaperPlanes
            )
            val fm = childFragmentManager
            dialog.show(fm, "first paper")
        }

        val repliedPlaneAdapter =
            RepliedPaperPlaneAdapter(listOf(), ViewModel) { RepliedPaperPlanes ->
                val dialog = FragmentDialogReplied.newInstance(
                    RepliedPaperPlanes
                )
                val fm = childFragmentManager
                dialog.show(fm, "replied paper")
            }

        val latestMessageAdapter =
            LatestMessageAdapter(listOf(), ViewModel) { LatestMessages ->
                val messageItem = LatestMessages

                val intent = Intent(requireActivity(), ChatLogActivity::class.java)
                val chatPartnerId = messageItem.partnerId
                intent.putExtra(USER_KEY, chatPartnerId)
                startActivity(intent)
            }


        rv_chat.adapter = latestMessageAdapter
        rv_paperplane_first.adapter = firstPlaneAdapter
        rv_paperplane_replied.adapter = repliedPlaneAdapter

        // 역순 정렬
        val mLayoutManagerFirst = LinearLayoutManager(requireActivity())
        val mLayoutManagerReplied = LinearLayoutManager(requireActivity())
        val mLayoutManagerMessages = LinearLayoutManager(requireActivity())
        mLayoutManagerFirst.reverseLayout = true
        mLayoutManagerFirst.stackFromEnd = true
        mLayoutManagerFirst.orientation = HORIZONTAL
        mLayoutManagerReplied.reverseLayout = true
        mLayoutManagerReplied.stackFromEnd = true
        mLayoutManagerReplied.orientation = HORIZONTAL
        mLayoutManagerMessages.orientation = VERTICAL

        rv_chat.layoutManager = mLayoutManagerMessages
        rv_paperplane_first.layoutManager = mLayoutManagerFirst
        rv_paperplane_replied.layoutManager = mLayoutManagerReplied

        ViewModel.allFirstPaperPlanes(UID).observe(viewLifecycleOwner, Observer {
            firstPlaneAdapter.list = it
            firstPlaneAdapter.notifyDataSetChanged()
            tv_count_first.text = firstPlaneAdapter.list.size.toString()
        })
        ViewModel.allRepliedPaperPlanes(UID).observe(viewLifecycleOwner, Observer {
            repliedPlaneAdapter.list = it
            repliedPlaneAdapter.notifyDataSetChanged()
            tv_count_replied.text = repliedPlaneAdapter.list.size.toString()
        })

        ViewModel.allChatRooms(UID).observe(viewLifecycleOwner, {
            latestMessageAdapter.list = it
            latestMessageAdapter.notifyDataSetChanged()
        })



    }
    // val messagesMap = HashMap<String, ChatMessage>()


//    private fun refreshRecyclerViewMessages() {
//        adapterVertical.clear()
//        messagesMap.values.forEach {
//            adapterVertical.add(LatestMessages(it))
//        }
//        adapterVertical.setOnItemClickListener { item, view ->
//            // ChatLogActivity 연결
//
//            val messageItem = item as LatestMessages
//
//            val intent = Intent(requireActivity(), ChatLogActivity::class.java)
//            val chatPartnerId: String = if (messageItem.latestChatMessage.fromId == UID) {
//                messageItem.latestChatMessage.toId
//            } else {
//                messageItem.latestChatMessage.fromId
//            }
//            intent.putExtra(USER_KEY, chatPartnerId)
//            startActivity(intent)
//        }
//        rv_chat.adapter = adapterVertical
//    }

    override fun onStart() {
        super.onStart()
        // listeners for planes and messages
        mRefPlane = FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$UID")
        mRefMessages =  FirebaseDatabase.getInstance().getReference("/Latest-messages/$UID/")
        mRefFinish = FirebaseDatabase.getInstance().getReference("/Finished-chat/$UID/isOver")
        listenForPlanes()
        listenForMessages()
        listenForFinishedChat()
    }

    override fun onStop() {
        super.onStop()
        mRefPlane.removeEventListener(mListenerPlane)
        mRefMessages.removeEventListener(mListenerMessages)
        mRefFinish.removeEventListener(mListenerFinish)
    }

    private fun listenForFinishedChat() {
        mListenerFinish = mRefFinish.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "Detect the end signal")
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
                        ViewModel.updateChatRoom(UID, partnerUid,true).join()
                        ViewModel.insert(chatMessages)
                        ViewModel.updateLastMessages(
                            UID,
                            partnerUid,
                            noticeFinish,
                            timestamp
                        )
                        mRefFinish.child(partnerUid).removeValue()
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

    private fun listenForPlanes() {
        mListenerPlane = mRefPlane.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return
                if (paperplane.id.isNotEmpty()) {
                    CoroutineScope(IO).launch {
                        if (!paperplane.isReplied) { // 상대가 날린 첫 비행기

                            val item = FirstPaperPlanes(
                                paperplane.fromId,
                                UID,
                                paperplane.text,
                                paperplane.flightDistance,
                                paperplane.timestamp
                            )
                            ViewModel.insert(item)
                            // immediate delete on setting data to local database
                            mRefPlane.child(paperplane.fromId).removeValue()
                            val acquaintances = Acquaintances(paperplane.fromId,UID)
                            ViewModel.insert(acquaintances)
                        } else { // 상대가 날린 답장 비행기
                            setRepliedPaperPlane(paperplane)
                            mRefPlane.child(paperplane.fromId).removeValue()
                        }


                        // immediate delete on setting data to local databasae


//                        repliedPlaneMap[snapshot.key!!] = paperplane
//                        repliedPlaneKeyList.add(snapshot.key!!)
//                        refreshRecyclerViewPlanesReplied()
                    }
                }


                Log.d(TAG, "Child added successfully")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
//                    // 데이터를 받은 순서대로 리스트에 저장될 것이고 정렬순을 바꾸지 않으므로 인덱스 저장 위치를 신경쓰지 않아도 됨
//                    val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return
//                    if (!paperplane.isReplied) {
//                        val index: Int = firstPlaneKeyList.indexOf(snapshot.key)
//                        adapterHorizontalFirst.removeGroupAtAdapterPosition(index)
//                        firstPlaneKeyList.removeAt(index)
//                    } else {
//                        val index: Int = repliedPlaneKeyList.indexOf(snapshot.key)
//                        adapterHorizontalReplied.removeGroupAtAdapterPosition(index)
//                        repliedPlaneKeyList.removeAt(index)
//                    }

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    suspend fun setRepliedPaperPlane(paperPlane: PaperplaneMessage) {

        CoroutineScope(IO).launch {
            val myPaperPlaneRecord = ViewModel.getWithId(UID, paperPlane.fromId).await()
            if(myPaperPlaneRecord!=null) {
                val item = RepliedPaperPlanes(
                    myPaperPlaneRecord.partnerId,
                    UID,
                    myPaperPlaneRecord.userMessage,
                    paperPlane.text,
                    paperPlane.flightDistance,
                    myPaperPlaneRecord.firstTimestamp,
                    paperPlane.timestamp
                )
                ViewModel.insert(item)
                ViewModel.delete(myPaperPlaneRecord)
            }
        }.join()
    }
    // TODO: unread messages indicator
    //  1. chatRoom data class 필드에 unreadMessages 변수를 추가
    //  2. Listener가 메시지 받으면 받은 갯수 만큼 unreadMessages 플러스
    //  3. unreadMessages에 Observer를 달아서 0이 아니면 UI에 표시
    //  4. 리사이클러뷰 아이템을 클릭하면 unreadMessage 0 대입
    //  

    private fun listenForMessages() {
        mListenerMessages= mRefMessages.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(LatestChatMessage::class.java) ?: return
                val partnerId = snapshot.key!!
                val isPartner = setSender(partnerId)

                CoroutineScope(IO).launch {
                    val isPartnerId = ViewModel.exists(UID, partnerId).await()
                    // 아직 채팅이 시작되지 않아서 채팅방 생성 필요
                    if (!isPartnerId) {
                        var partnerNickname : String
                        if(latestChatMessage.nickname.isNotBlank()) {
                            partnerNickname = latestChatMessage.nickname
                            val chatRoom = ChatRooms(
                                partnerId,
                                partnerNickname,
                                UID,
                                latestChatMessage.message,
                                latestChatMessage.time,
                                false
                            )
                            ViewModel.insert(chatRoom)
                            mRefMessages.child(snapshot.key!!).removeValue()
                        } else {
                            val ref2 =
                                FirebaseDatabase.getInstance().getReference("/Users/$partnerId")
                                    .child("nickname")
                            ref2.get().addOnSuccessListener {
                                partnerNickname = it.value.toString()
                                val chatRoom = ChatRooms(
                                    partnerId,
                                    partnerNickname,
                                    UID,
                                    latestChatMessage.message,
                                    latestChatMessage.time,
                                    false
                                )
                                ViewModel.insert(chatRoom)
                                mRefMessages.child(snapshot.key!!).removeValue()

                            }.addOnFailureListener {
                                Toast.makeText(requireActivity(), "없는 유저입니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }


                    } else { // 이미 시작된 채팅
                        ViewModel.updateLastMessages(
                            UID,
                            partnerId,
                            latestChatMessage.message,
                            latestChatMessage.time
                        ).join()
                        mRefMessages.child(snapshot.key!!).removeValue()
                    }
                }
                Log.d(TAG, "Child added successfully")
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

//    // Latest-Messages 노드 대신 User-Messages 활용 방식
//    private fun listenForMessages() {
//        val ref = FirebaseDatabase.getInstance().getReference("/User-messages/$UID/")
//
//        ref.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                val latestChatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
//                val partnerId = latestChatMessage.fromId
//                // TODO : 필요성 재고 (어차피 상대방에게 보낸 메세지는 로컬에 바로 저장, 상대가 보낸 메세지만 서버를 거쳐 전달됨)
//                val isPartner = setSender(latestChatMessage.fromId)
//
//                CoroutineScope(IO).launch {
//                    val isPartnerId = ViewModel.exists(UID, partnerId).await()
//                    // 아직 채팅이 시작되지 않아서 채팅방 생성 필요
//                    if (!isPartnerId) {
//                        val ref2 =
//                            FirebaseDatabase.getInstance().getReference("/Users/$partnerId")
//                                .child("nickname")
//                        ref2.get().addOnSuccessListener {
//                            val partnerNickname = it.value.toString()
//
//                            val chatRoom = ChatRooms(
//                                partnerId,
//                                partnerNickname,
//                                UID,
//                                latestChatMessage.message,
//                                latestChatMessage.timestamp
//                            )
//                            val chatMessages = ChatMessages(
//                                null,
//                                partnerId,
//                                UID,
//                                3,
//                                latestChatMessage.message,
//                                latestChatMessage.timestamp
//                            )
//                            ViewModel.insert(chatRoom)
//                            ViewModel.insert(chatMessages)
//                            Log.d("FbTest", ref.child(snapshot.key!!).toString())
//                            // ref.child(snapshot.key!!).removeValue()
//
//                        }.addOnFailureListener {
//                            Toast.makeText(requireActivity(), "없는 유저입니다.", Toast.LENGTH_SHORT)
//                                .show()
//                        }
//
//                    } else { // 이미 시작된 채팅
//                        ViewModel.updateLastMessages(
//                            UID,
//                            partnerId,
//                            latestChatMessage.message,
//                            latestChatMessage.timestamp
//                        ).join()
//                        val chatMessages = ChatMessages(
//                            null,
//                            partnerId,
//                            UID,
//                            isPartner,
//                            latestChatMessage.message,
//                            latestChatMessage.timestamp
//                        )
//                        ViewModel.insert(chatMessages).join()
//                        // ref.child(snapshot.key!!).removeValue()
//                    }
//
//                }
//                Log.d(TAG, "Child added successfully")
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })
//    }

    private fun setSender(partnerId: String): Int {
        return if (partnerId != UID) 1
        else 0
    }

    override fun onPaperClicked(item: FirstPaperPlanes) {
        Log.d("FirstPlane", "clicked")
        val dialog = FragmentDialogFirst.newInstance(
            item
        )
        val fm = childFragmentManager
        dialog.show(fm, "first paper")
    }

    private fun setDateToTextView(timestamp: Long): String {
        var sdf: SimpleDateFormat

        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = timestamp

        val now = Calendar.getInstance()
        if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)) {
            sdf = SimpleDateFormat("a hh:mm")
        } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1) {
            return "어제"
        } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
            sdf = SimpleDateFormat("MM월 dd일")
        } else {
            sdf = SimpleDateFormat("yyyy.MM.dd")
        }

        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return sdf.format(timestamp * 1000L)
    }


}

//    class PaperPlanesFirst(val paperplaneMessage: PaperplaneMessage) :
//        Item<GroupieViewHolder>() {
//        override fun getLayout(): Int {
//            return R.layout.column_paperplane_first
//        }
//
//        @SuppressLint("SetTextI18n")
//        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//            viewHolder.itemView.tv_paperplane_distance_first.text =
//                paperplaneMessage.flightDistance.toString() + "m"
//            viewHolder.itemView.tv_paperplane_time_first.text = setDateToTextView(paperplaneMessage.timestamp)
//        }
//
//        private fun calDaysBetween(time : Long) : Long {
//            val currentTimestamp = System.currentTimeMillis()
//            val timeDiff = currentTimestamp - time
//            return TimeUnit.MILLISECONDS.toDays(timeDiff)
//        }
//
//        private fun setDateToTextView(timestamp: Long): String {
//
//            var sdf: SimpleDateFormat
//
//            val messageTime = Calendar.getInstance()
//            messageTime.timeInMillis = timestamp
//
//            val now = Calendar.getInstance()
//            if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE) ) {
//                sdf = SimpleDateFormat("a hh:mm")
//            } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1  ){
//                return "어제"
//            } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
//                sdf = SimpleDateFormat("MM월 dd일")
//            } else {
//                sdf = SimpleDateFormat("yyyy.MM.dd")
//            }
//
//            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
//
//            return sdf.format(paperplaneMessage.timestamp * 1000L)
//        }
//
//    }
//
//    class PaperPlanesReplied(val paperplaneMessage: PaperplaneMessage) :
//        Item<GroupieViewHolder>() {
//        override fun getLayout(): Int {
//            return R.layout.column_paperplane_replied
//        }
//
//        @SuppressLint("SetTextI18n")
//        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
//            viewHolder.itemView.tv_paperplane_distance_replied.text =
//                paperplaneMessage.flightDistance.toString() + "m"
//            viewHolder.itemView.tv_paperplane_time_replied.text =
//                setDateToTextView(paperplaneMessage.timestamp)
//        }
//
//        private fun setDateToTextView(timestamp: Long) : String {
//            val sdf = SimpleDateFormat("yyyy-MM-dd a hh:mm")
//            val date = sdf.format(paperplaneMessage.timestamp*1000L)
//            return date.toString()
//        }
//    }

class LatestMessages(val latestChatMessage: ChatMessage) :
    Item<GroupieViewHolder>() {
    var chatPartnerUser: Users? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_message.text = latestChatMessage.message
        viewHolder.itemView.tv_chat_time.text = setDateToTextView(latestChatMessage.timestamp)

        val chatPartnerId: String
        if (latestChatMessage.fromId == UID) {
            chatPartnerId = latestChatMessage.toId
        } else {
            chatPartnerId = latestChatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/Users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(Users::class.java)
                viewHolder.itemView.tv_chat_nickname.text = chatPartnerUser?.nickname
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_chat_row
    }

    private fun setDateToTextView(timestamp: Long): String {
        var sdf: SimpleDateFormat

        val messageTime = Calendar.getInstance()
        messageTime.timeInMillis = timestamp

        val now = Calendar.getInstance()
        if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)) {
            sdf = SimpleDateFormat("a hh:mm")
        } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1) {
            return "어제"
        } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
            sdf = SimpleDateFormat("MM월 dd일")
        } else {
            sdf = SimpleDateFormat("yyyy.MM.dd")
        }

        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return sdf.format(timestamp * 1000L)
    }
}

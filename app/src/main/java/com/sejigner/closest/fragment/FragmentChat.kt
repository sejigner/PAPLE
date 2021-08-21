package com.sejigner.closest.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import com.google.firebase.database.*
import com.sejigner.closest.Adapter.FirstPaperPlaneAdapter
import com.sejigner.closest.Adapter.LatestMessageAdapter
import com.sejigner.closest.Adapter.RepliedPaperPlaneAdapter
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.UI.FirstPlaneListener
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.UI.FragmentChatViewModelFactory
import com.sejigner.closest.Users
import com.sejigner.closest.models.ChatMessage
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.room.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.latest_chat_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class FragmentChat : Fragment(), FirstPlaneListener {

    companion object {
        const val TAG = "FragmentChat"
        val USER_KEY = "USER_KEY"
    }

    private val adapterHorizontalFirst = GroupAdapter<GroupieViewHolder>()
    private val adapterHorizontalReplied = GroupAdapter<GroupieViewHolder>()
    private val adapterVertical = GroupAdapter<GroupieViewHolder>()
    private val firstPlaneKeyList = ArrayList<String>()
    private val repliedPlaneKeyList = ArrayList<String>()
    private val messageKeyList = ArrayList<String>()
    private var db: PaperPlaneDatabase? = null
    lateinit var ViewModel: FragmentChatViewModel
    lateinit var list: List<FirstPaperPlanes>


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


        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)

        // initialised View Model
        ViewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)
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
                val chatPartnerId = messageItem.room.partnerId
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
        mLayoutManagerMessages.reverseLayout = true
        mLayoutManagerMessages.stackFromEnd = true
        mLayoutManagerMessages.orientation = VERTICAL

        rv_chat.layoutManager = mLayoutManagerMessages
        rv_paperplane_first.layoutManager = mLayoutManagerFirst
        rv_paperplane_replied.layoutManager = mLayoutManagerReplied

        ViewModel.allFirstPaperPlanes().observe(requireActivity(), Observer {
            firstPlaneAdapter.list = it
            firstPlaneAdapter.notifyDataSetChanged()
        })
        ViewModel.allRepliedPaperPlanes().observe(requireActivity(), Observer {
            repliedPlaneAdapter.list = it
            repliedPlaneAdapter.notifyDataSetChanged()
        })

        ViewModel.allChatRooms().observe(requireActivity(), Observer {
            latestMessageAdapter.list = it
            latestMessageAdapter.notifyDataSetChanged()
        })


        // fetchPapers()
        listenForPlanes()
        listenForMessages()

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

    private fun listenForPlanes() {
        val ref = FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$UID")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {


                val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return
                if (!paperplane.isReplied) {
                    val item = FirstPaperPlanes(
                        null,
                        paperplane.fromId,
                        paperplane.text,
                        paperplane.flightDistance,
                        paperplane.timestamp
                    )
                    ViewModel.insert(item)
                    // immediate delete on setting data to local databasae
                    ref.child(paperplane.fromId).removeValue()
                } else {
                    val myPaperPlaneRecord = ViewModel.getWithId(paperplane.fromId)
                    val item =RepliedPaperPlanes(
                        null, myPaperPlaneRecord.userMessage,
                        paperplane.fromId,
                        paperplane.text,
                        paperplane.flightDistance,
                        myPaperPlaneRecord.firstTimestamp,
                        paperplane.timestamp
                    )
                    ViewModel.insert(item)
                    ViewModel.delete(myPaperPlaneRecord)
                    // immediate delete on setting data to local databasae
                    ref.child(paperplane.fromId).removeValue()

//                        repliedPlaneMap[snapshot.key!!] = paperplane
//                        repliedPlaneKeyList.add(snapshot.key!!)
//                        refreshRecyclerViewPlanesReplied()
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

    private fun listenForMessages() {
        val ref = FirebaseDatabase.getInstance().getReference("/Latest-messages/$UID")

        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                val partnerId = latestChatMessage.fromId
                if(latestChatMessage.fromId == UID) {
                    val partnerId = latestChatMessage.toId
                    if(ViewModel.exists(partnerId)) {
                        val ref = FirebaseDatabase.getInstance().getReference("/Users/$partnerId").child("strNickname")
                        ref.get().addOnSuccessListener {
                            val partnerNickname = it.value.toString()
                            val chatMessage = ChatMessages(null,latestChatMessage.toId, false,latestChatMessage.text, latestChatMessage.timestamp)
                            val chatRoom = ChatRooms(partnerId, partnerNickname)

                            ViewModel.insert(chatRoom)
                            ViewModel.insert(chatMessage)
                        }.addOnFailureListener {
                            Toast.makeText(requireActivity(),"없는 유저입니다.", Toast.LENGTH_SHORT).show()
                        }


                    } else {
                        val chatMessage = ChatMessages(null,latestChatMessage.toId, false,latestChatMessage.text, latestChatMessage.timestamp)
                        ViewModel.insert(chatMessage)
                    }


                } else {
                    val partnerId = latestChatMessage.fromId
                    val chatMessage = ChatMessages(null,latestChatMessage.fromId, true,latestChatMessage.text, latestChatMessage.timestamp)
                    if(ViewModel.exists(partnerId)) {
                        val ref = FirebaseDatabase.getInstance().getReference("/Users/$partnerId").child("strNickname")
                        ref.get().addOnSuccessListener {
                            val partnerNickname = it.value.toString()
                        }
                        val chatMessage = ChatMessages(null,latestChatMessage.toId, false,latestChatMessage.text, latestChatMessage.timestamp)
                        ViewModel.insert(chatMessage)
                    } else {

                    }
                }


                Log.d(TAG, "Child added successfully")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // 데이터를 받은 순서대로 리스트에 저장될 것이고 정렬순을 바꾸지 않으므로 인덱스 저장 위치를 신경쓰지 않아도 됨
                val index: Int = messageKeyList.indexOf(snapshot.key)
                adapterVertical.removeGroupAtAdapterPosition(index)
                messageKeyList.removeAt(index)
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
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
        viewHolder.itemView.tv_chat_message.text = latestChatMessage.text
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
                viewHolder.itemView.tv_chat_nickname.text = chatPartnerUser?.strNickname
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

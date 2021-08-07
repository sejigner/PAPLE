 package com.sejigner.closest.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.R
import com.sejigner.closest.Users
import com.sejigner.closest.models.ChatMessage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.column_paperplane_first.view.*
import kotlinx.android.synthetic.main.column_paperplane_replied.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.latest_chat_row.view.*

class FragmentChat : Fragment() {

    companion object {
        const val TAG = "FragmentChat"
        val USER_KEY = "USER_KEY"
    }

    private val adapterHorizontalFirst = GroupAdapter<GroupieViewHolder>()
    private val adapterHorizontalReplied = GroupAdapter<GroupieViewHolder>()
    private val adapterVertical = GroupAdapter<GroupieViewHolder>()
    private val uid = FirebaseAuth.getInstance().uid
    private val firstPlaneKeyList = ArrayList<String>()
    private val repliedPlaneKeyList = ArrayList<String>()
    private val messageKeyList = ArrayList<String>()


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

        rv_paperplane_first.adapter = adapterHorizontalFirst
        rv_paperplane_replied.adapter = adapterHorizontalReplied
        rv_chat.adapter = adapterVertical


        // fetchPapers()
        listenForPlanes()
        listenForLatestMessages()

    }

    val firstPlaneMap = HashMap<String, PaperplaneMessage>()
    val repliedPlaneMap = HashMap<String, PaperplaneMessage>()
    val messagesMap = HashMap<String, ChatMessage>()

    private fun refreshRecyclerViewPlanesFirst() {
        adapterHorizontalFirst.clear()
        firstPlaneMap.values.forEach {
            adapterHorizontalFirst.add(PaperPlanesFirst(it))

            adapterHorizontalFirst.setOnItemClickListener { item, view ->

                val paperPlanes = item as PaperPlanesFirst
                val message = paperPlanes.paperplaneMessage.text
                val distance = paperPlanes.paperplaneMessage.flightDistance.toString()
                val time = paperPlanes.paperplaneMessage.timestamp.toString()
                val toId = paperPlanes.paperplaneMessage.toId
                val fromId = paperPlanes.paperplaneMessage.fromId
                var isReplied = paperPlanes.paperplaneMessage.isReplied


                val dialog = FragmentDialogFirst.newInstance(
                    message,
                    distance,
                    time,
                    toId,
                    fromId,
                    isReplied
                )
                val fm = childFragmentManager
                dialog.show(fm, "first paper")
            }
        }
    }

    private fun refreshRecyclerViewPlanesReplied() {
        adapterHorizontalReplied.clear()
        repliedPlaneMap.values.forEach {
            adapterHorizontalReplied.add(PaperPlanesReplied(it))

            adapterHorizontalReplied.setOnItemClickListener { item, view ->

                val paperPlanes = item as PaperPlanesReplied
                val message = paperPlanes.paperplaneMessage.text
                val distance = paperPlanes.paperplaneMessage.flightDistance.toString()
                val time = paperPlanes.paperplaneMessage.timestamp.toString()
                val toId = paperPlanes.paperplaneMessage.toId
                val fromId = paperPlanes.paperplaneMessage.fromId
                var isReplied = paperPlanes.paperplaneMessage.isReplied


                val dialog = FragmentDialogReplied.newInstance(
                    message,
                    distance,
                    time,
                    toId,
                    fromId,
                    isReplied
                )
                val fm = childFragmentManager
                dialog.show(fm, "second paper")
            }

        }
    }

        private fun refreshRecyclerViewMessages() {
            adapterVertical.clear()
            messagesMap.values.forEach {
                adapterVertical.add(LatestMessages(it))
            }
            adapterVertical.setOnItemClickListener { item, view ->
                // ChatLogActivity 연결

                val messageItem = item as LatestMessages

                val intent = Intent(requireActivity(), ChatLogActivity::class.java)
                val chatPartnerId : String = if (messageItem.latestChatMessage.fromId == FirebaseAuth.getInstance().uid) {
                    messageItem.latestChatMessage.toId
                } else {
                    messageItem.latestChatMessage.fromId
                }
                intent.putExtra(USER_KEY, chatPartnerId)
                startActivity(intent)
            }
            rv_chat.adapter = adapterVertical
        }


        private fun listenForPlanes() {
            val ref = FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$uid")

            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {


                    val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return
                    if (!paperplane.isReplied) {
                        firstPlaneMap[snapshot.key!!] = paperplane
                        firstPlaneKeyList.add(snapshot.key!!)
                        refreshRecyclerViewPlanesFirst()
                    } else {
                        repliedPlaneMap[snapshot.key!!] = paperplane
                        repliedPlaneKeyList.add(snapshot.key!!)
                        refreshRecyclerViewPlanesReplied()
                    }
                    Log.d(TAG, "Child added successfully")
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return

                    if (!paperplane.isReplied) {
                        firstPlaneMap[snapshot.key!!] = paperplane
                        refreshRecyclerViewPlanesFirst()
                    } else {
                        repliedPlaneMap[snapshot.key!!] = paperplane
                        refreshRecyclerViewPlanesReplied()
                    }



                    Log.d(TAG, "Child changed detected")
                }
                // int diaryIndex = mDiaryID.indexOf(dataSnapshot.getKey());

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // 데이터를 받은 순서대로 리스트에 저장될 것이고 정렬순을 바꾸지 않으므로 인덱스 저장 위치를 신경쓰지 않아도 됨
                    val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return
                    if (!paperplane.isReplied) {
                        val index: Int = firstPlaneKeyList.indexOf(snapshot.key)
                        adapterHorizontalFirst.removeGroupAtAdapterPosition(index)
                        firstPlaneKeyList.removeAt(index)
                    } else {
                        val index: Int = repliedPlaneKeyList.indexOf(snapshot.key)
                        adapterHorizontalReplied.removeGroupAtAdapterPosition(index)
                        repliedPlaneKeyList.removeAt(index)
                    }

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        private fun listenForLatestMessages() {
            val fromId = uid
            val ref = FirebaseDatabase.getInstance().getReference("/Latest-messages/$fromId")

            ref.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {


                    val latestChatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                    messagesMap[snapshot.key!!] = latestChatMessage
                    messageKeyList.add(snapshot.key!!)
                    refreshRecyclerViewMessages()

                    Log.d(TAG, "Child added successfully")
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val latestChatMessage = snapshot.getValue(ChatMessage::class.java) ?: return

                    messagesMap[snapshot.key!!] = latestChatMessage
                    refreshRecyclerViewMessages()

                    Log.d(TAG, "Child changed detected")
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


    }

    class PaperPlanesFirst(val paperplaneMessage: PaperplaneMessage) :
        Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.column_paperplane_first
        }

        @SuppressLint("SetTextI18n")
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.tv_paperplane_distance.text =
                paperplaneMessage.flightDistance.toString() + "m"
            viewHolder.itemView.tv_paperplane_time.text = paperplaneMessage.timestamp.toString()
        }
    }

    class PaperPlanesReplied(val paperplaneMessage: PaperplaneMessage) :
        Item<GroupieViewHolder>() {
        override fun getLayout(): Int {
            return R.layout.column_paperplane_replied
        }

        @SuppressLint("SetTextI18n")
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.tv_paperplane_distance_replied.text =
                paperplaneMessage.flightDistance.toString() + "m"
            viewHolder.itemView.tv_paperplane_time_replied.text =
                paperplaneMessage.timestamp.toString()
        }
    }

    class LatestMessages(val latestChatMessage: ChatMessage) :
        Item<GroupieViewHolder>() {
        var chatPartnerUser: Users? = null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.tv_chat_message.text = latestChatMessage.text
            viewHolder.itemView.tv_chat_time.text = latestChatMessage.timestamp.toString()

            val chatPartnerId: String
            if (latestChatMessage.fromId == FirebaseAuth.getInstance().uid) {
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
    }

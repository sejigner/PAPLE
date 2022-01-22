package com.gievenbeck.paple.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gievenbeck.paple.R
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.room.ChatRooms
import kotlinx.android.synthetic.main.latest_chat_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class LatestMessageAdapter(var list : List<ChatRooms>, val viewModel : FragmentChatViewModel, val itemClick: (ChatRooms) -> Unit) : RecyclerView.Adapter<LatestMessageAdapter.ChatRoomsViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatRoomsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.latest_chat_row, parent, false)
        return ChatRoomsViewHolder(view, itemClick)
    }

    private val differCallback = object : DiffUtil.ItemCallback<ChatRooms>() {
        override fun areItemsTheSame(
            oldItem: ChatRooms,
            newItem: ChatRooms
        ): Boolean {
            return oldItem.partnerId == newItem.partnerId
        }

        override fun areContentsTheSame(
            oldItem: ChatRooms,
            newItem: ChatRooms
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onBindViewHolder(
        holder: ChatRoomsViewHolder,
        position: Int
    ) {
        val currentPosition = differ.currentList[position]
        holder.itemView.setOnClickListener { itemClick(currentPosition) }
        holder.bind(currentPosition)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun setDateToTextView(timestamp: Long) : String {
        var sdf: SimpleDateFormat
        val date = Date(timestamp*1000)
        val messageTime = Calendar.getInstance()
        messageTime.time = date

        val now = Calendar.getInstance()
        sdf = if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE) ) {
            SimpleDateFormat("a hh:mm")
        } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1  ){
            return "어제"
        } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
            SimpleDateFormat("MM월 dd일")
        } else {
            SimpleDateFormat("yyyy.MM.dd")
        }

        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return sdf.format(timestamp * 1000L)
    }

    inner class ChatRoomsViewHolder(itemView : View, itemClick: (ChatRooms) -> Unit) : RecyclerView.ViewHolder(itemView) {
        fun bind(chatRoom: ChatRooms?) {
            //check for null
            chatRoom?.let {
                itemView.tv_chat_nickname.text = it.partnerNickname
                itemView.tv_chat_time.text = setDateToTextView(it.lastMessageTimestamp!!)
                itemView.tv_chat_message.text = it.lastMessage
            }
        }
    }
}


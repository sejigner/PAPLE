package com.sejigner.closest.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.UI.FirstPlaneListener
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.room.ChatRoomsWithMessages
import com.sejigner.closest.room.FirstPaperPlanes
import com.sejigner.closest.room.RepliedPaperPlanes
import kotlinx.android.synthetic.main.column_paperplane_first.view.*
import kotlinx.android.synthetic.main.latest_chat_row.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LatestMessageAdapter(var list : List<ChatRoomsWithMessages>, val viewModel : FragmentChatViewModel, val itemClick: (ChatRoomsWithMessages) -> Unit) : RecyclerView.Adapter<LatestMessageAdapter.ChatRoomsWithMessagesViewHolder>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatRoomsWithMessagesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.latest_chat_row, parent, false)
        return ChatRoomsWithMessagesViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(
        holder: ChatRoomsWithMessagesViewHolder,
        position: Int
    ) {
        var currentPosition = list[position]
        val latestChatMessage = viewModel.getLatestMessage(currentPosition.room.partnerId)
        holder.itemView.tv_chat_nickname.text = currentPosition.room.partnerNickname
        holder.itemView.tv_chat_time.text = setDateToTextView(latestChatMessage.timestamp)
        holder.itemView.tv_chat_message.text = latestChatMessage.message
        holder.itemView.setOnClickListener{ itemClick(currentPosition) }
    }

    override fun getItemCount(): Int {
        return list.size
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

    inner class ChatRoomsWithMessagesViewHolder(itemView : View, itemClick: (ChatRoomsWithMessages) -> Unit) : RecyclerView.ViewHolder(itemView) {

    }
}


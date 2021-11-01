package com.sejigner.closest.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.R
import com.sejigner.closest.room.ChatMessages
import com.sejigner.closest.ui.*
import java.text.SimpleDateFormat
import java.util.*


class ChatLogAdapter(var list: List<ChatMessages>, val viewModel: FragmentChatViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return list[position].meOrPartner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View?
        return when (viewType) {
            messageFromMe -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_me_row,
                    parent,
                    false
                )
                MessageFromMeViewHolder(view)
            }
            messageFromPartner -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_partner_row,
                    parent,
                    false
                )
                MessageFromPartnerViewHolder(view)
            }
            messageNewDate -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_date_row,
                    parent,
                    false
                )
                DateViewHolder(view)
            }
            messageNotice -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_notice_row,
                    parent,
                    false
                )
                NoticeViewHolder(view)
            }

            else ->  throw RuntimeException("알 수 없는 뷰 타입 에러")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (list[position].meOrPartner) {
            0 -> {
                (holder as MessageFromMeViewHolder).bind(list[position])
                holder.setIsRecyclable(false)
            }
            1 -> {
                (holder as MessageFromPartnerViewHolder).bind(list[position])
                holder.setIsRecyclable(false)
            }
            2 -> {
                (holder as DateViewHolder).bind(list[position])
                holder.setIsRecyclable(false)
            }
            3 -> {
                (holder as NoticeViewHolder).bind(list[position])
                holder.setIsRecyclable(false)
            }
        }

//        val currentPosition = list[position]
//        holder.itemView.tv_chat_nickname.text = currentPosition.partnerNickname
//        holder.itemView.tv_chat_time.text = setDateToTextView(currentPosition.lastMessageTimestamp!!)
//        holder.itemView.tv_chat_message.text = currentPosition.lastMessage
//        holder.itemView.setOnClickListener{ itemClick(currentPosition) }
    }

    override fun getItemCount(): Int = list.size

    inner class MessageFromMeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.tv_time_me)
        private val message: TextView = itemView.findViewById(R.id.tv_message_me)

        fun bind(item: ChatMessages) {
            time.text = setDateToTextView(item.timestamp!!)
            message.text = item.message
        }
    }

    inner class MessageFromPartnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.tv_time_partner)
        private val message: TextView = itemView.findViewById(R.id.tv_message_partner)

        fun bind(item: ChatMessages) {
            time.text = setDateToTextView(item.timestamp!!)
            message.text = item.message
        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val date: TextView = itemView.findViewById(R.id.tv_chat_date)

        fun bind(item: ChatMessages) {
            date.text = item.message
        }
    }

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val date: TextView = itemView.findViewById(R.id.tv_chat_notice)

        fun bind(item: ChatMessages) {
            date.text = item.message
        }
    }

    private fun setDateToTextView(timestamp: Long): String {
        val sdf = SimpleDateFormat("a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = sdf.format(timestamp * 1000L)
        return date.toString()
    }

    private fun getDateTime(time: Long): String? {
        try {
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
            sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
            val netDate = Date(time * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            Log.d(ChatLogActivity.TAG, e.toString())
            return e.toString()
        }
    }
}


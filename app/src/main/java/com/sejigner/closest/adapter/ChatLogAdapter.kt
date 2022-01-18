package com.sejigner.closest.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.ChatLogActivity
import com.sejigner.closest.R
import com.sejigner.closest.room.ChatMessages
import com.sejigner.closest.room.FirstPaperPlanes
import com.sejigner.closest.ui.*
import java.text.SimpleDateFormat
import java.util.*


class ChatLogAdapter(var list: List<ChatMessages>, val viewModel: FragmentChatViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].meOrPartner
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

            else -> throw RuntimeException("알 수 없는 뷰 타입 에러")
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<ChatMessages>() {
        override fun areItemsTheSame(
            oldItem: ChatMessages,
            newItem: ChatMessages
        ): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(
            oldItem: ChatMessages,
            newItem: ChatMessages
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (differ.currentList[position].meOrPartner) {
            0 -> {
                (holder as MessageFromMeViewHolder).bind(differ.currentList[position])
                holder.setIsRecyclable(false)
            }
            1 -> {
                (holder as MessageFromPartnerViewHolder).bind(differ.currentList[position])
                holder.setIsRecyclable(false)
            }
            2 -> {
                (holder as DateViewHolder).bind(differ.currentList[position])
                holder.setIsRecyclable(false)
            }
            3 -> {
                (holder as NoticeViewHolder).bind(differ.currentList[position])
                holder.setIsRecyclable(false)
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    inner class MessageFromMeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.tv_time_me)
        private val message: TextView = itemView.findViewById(R.id.tv_message_me)

        fun bind(item: ChatMessages?) {
            item?.let {
                time.text = setDateToTextView(it.timestamp!!)
                message.text = it.message
            }

        }
    }

    inner class MessageFromPartnerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.tv_time_partner)
        private val message: TextView = itemView.findViewById(R.id.tv_message_partner)

        fun bind(item: ChatMessages?) {
            item?.let {
                time.text = setDateToTextView(it.timestamp!!)
                message.text = it.message
            }

        }
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val date: TextView = itemView.findViewById(R.id.tv_chat_date)
        fun bind(item: ChatMessages?) {
            item?.let {
                date.text = it.message
            }
        }
    }

    inner class NoticeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notice: TextView = itemView.findViewById(R.id.tv_chat_notice)

        fun bind(item: ChatMessages?) {
            item?.let {
                notice.text = it.message
            }
        }
    }

    private fun setDateToTextView(timestamp: Long): String {
        val sdf = SimpleDateFormat("a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = sdf.format(timestamp * 1000L)
        return date.toString()
    }
}


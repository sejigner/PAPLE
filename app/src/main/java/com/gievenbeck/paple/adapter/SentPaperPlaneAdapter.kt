package com.gievenbeck.paple.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gievenbeck.paple.R
import com.gievenbeck.paple.room.MyPaper
import com.gievenbeck.paple.ui.FragmentChatViewModel
import kotlinx.android.synthetic.main.column_sent_paper.view.*
import java.text.SimpleDateFormat
import java.util.*

class SentPaperPlaneAdapter(
    var list: List<MyPaper>,
    val viewModel: FragmentChatViewModel,
    val itemClick: (MyPaper) -> Unit
) : RecyclerView.Adapter<SentPaperPlaneAdapter.SentPaperPlaneViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SentPaperPlaneViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.column_sent_paper, parent, false)
        return SentPaperPlaneViewHolder(view, itemClick)
    }

    private val differCallback = object : DiffUtil.ItemCallback<MyPaper>() {
        override fun areItemsTheSame(
            oldItem: MyPaper,
            newItem: MyPaper
        ): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(
            oldItem: MyPaper,
            newItem: MyPaper
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onBindViewHolder(
        holder: SentPaperPlaneViewHolder,
        position: Int
    ) {
        val currentPosition = differ.currentList[position]
        holder.itemView.setOnClickListener { itemClick(currentPosition) }
        holder.bind(currentPosition)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun setDateToTextView(timestamp: Long): String {
        var sdf: SimpleDateFormat
        val date = Date(timestamp * 1000)
        val messageTime = Calendar.getInstance()
        messageTime.time = date

        val now = Calendar.getInstance()
        sdf = if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)) {
            SimpleDateFormat("a hh:mm")
        } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1) {
            return "어제"
        } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
            SimpleDateFormat("MM월 dd일")
        } else {
            SimpleDateFormat("yyyy.MM.dd")
        }

        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")

        return sdf.format(timestamp * 1000L)
    }

    inner class SentPaperPlaneViewHolder(itemView: View, itemClick: (MyPaper) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(paper: MyPaper?) {
            paper?.let {
                itemView.tv_sent_paper_message.text = it.text
                itemView.tv_sent_paper_time.text = setDateToTextView(it.timestamp)
            }
        }

    }
}


package com.sejigner.closest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.room.MyPaper
import com.sejigner.closest.room.MyPaperPlaneRecord
import com.sejigner.closest.ui.FragmentChatViewModel
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

    override fun onBindViewHolder(
        holder: SentPaperPlaneViewHolder,
        position: Int
    ) {
        var currentPosition = list[position]
        holder.itemView.tv_sent_paper_message.text = currentPosition.text
        holder.itemView.tv_sent_paper_time.text = setDateToTextView(currentPosition.timestamp)
        holder.itemView.setOnClickListener { itemClick(currentPosition) }
    }

    override fun getItemCount(): Int {
        return list.size
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
    }
}


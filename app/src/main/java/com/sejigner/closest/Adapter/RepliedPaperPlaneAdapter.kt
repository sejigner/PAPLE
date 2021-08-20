package com.sejigner.closest.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.UI.FirstPlaneListener
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.room.FirstPaperPlanes
import com.sejigner.closest.room.RepliedPaperPlanes
import kotlinx.android.synthetic.main.column_paperplane_first.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class RepliedPaperPlaneAdapter(var list : List<RepliedPaperPlanes>, val viewModel : FragmentChatViewModel, val itemClick: (RepliedPaperPlanes) -> Unit) : RecyclerView.Adapter<RepliedPaperPlaneAdapter.RepliedPaperPlaneViewHolder>() {

    val mListener : FirstPlaneListener ?= null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepliedPaperPlaneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.column_paperplane_replied, parent, false)
        return RepliedPaperPlaneViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(
        holder: RepliedPaperPlaneViewHolder,
        position: Int
    ) {
        var currentPosition = list[position]
        holder.itemView.tv_paperplane_message_first.text = currentPosition.partnerMessage
        holder.itemView.tv_paperplane_distance_first.text = currentPosition.flightDistance.toString()+"m"
        holder.itemView.tv_paperplane_time_first.text = setDateToTextView(currentPosition.replyTimestamp)
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

    inner class RepliedPaperPlaneViewHolder(itemView : View, itemClick: (RepliedPaperPlanes) -> Unit) : RecyclerView.ViewHolder(itemView) {

    }
}


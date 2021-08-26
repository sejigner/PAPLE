package com.sejigner.closest.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.ui.FirstPlaneListener
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.room.FirstPaperPlanes
import kotlinx.android.synthetic.main.column_paperplane_first.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class FirstPaperPlaneAdapter(var list : List<FirstPaperPlanes>, val viewModel : FragmentChatViewModel, val itemClick: (FirstPaperPlanes) -> Unit) : RecyclerView.Adapter<FirstPaperPlaneAdapter.FirstPaperPlaneViewHolder>() {

    val mListener : FirstPlaneListener ?= null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FirstPaperPlaneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.column_paperplane_first, parent, false)
        return FirstPaperPlaneViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(
        holder: FirstPaperPlaneViewHolder,
        position: Int
    ) {
        var currentPosition = list[position]
        holder.itemView.tv_paperplane_message_first.text = currentPosition.message
        holder.itemView.tv_paperplane_distance_first.text = convertDistanceToString(currentPosition.flightDistance)
        holder.itemView.tv_paperplane_time_first.text = setDateToTextView(currentPosition.timestamp)
        holder.itemView.setOnClickListener{ itemClick(currentPosition) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun convertDistanceToString(distance : Double) : String {
        return if(distance >= 1000) {
            (round((distance/1000)*100)/100).toString() + "km"
        } else distance.toString() + "m"
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

    inner class FirstPaperPlaneViewHolder(itemView : View, itemClick: (FirstPaperPlanes) -> Unit) : RecyclerView.ViewHolder(itemView) {

    }
}


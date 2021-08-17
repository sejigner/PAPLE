package com.sejigner.closest.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.UI.FirstPlaneListener
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.room.FirstPaperPlanes
import kotlinx.android.synthetic.main.column_paperplane_first.view.*
import kotlinx.android.synthetic.main.fragment_dialog_first.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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
        holder.itemView.tv_paperplane_distance.text = currentPosition.flightDistance.toString()+"m"
        holder.itemView.tv_paperplane_time.text = setDateToTextView(currentPosition.timestamp)
        holder.itemView.setOnClickListener{ itemClick(currentPosition) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun setDateToTextView(timestamp: Long) : String {
        val sdf = SimpleDateFormat("yyyy-MM-dd a hh:mm")
        sdf.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        val date = sdf.format(timestamp*1000L)
        return date.toString()
    }

    private fun calDaysBetween(time : Long) : Long {
        val currentTimestamp = System.currentTimeMillis()
        val timeDiff = currentTimestamp
        return TimeUnit.MILLISECONDS.toDays(timeDiff)
    }

    inner class FirstPaperPlaneViewHolder(itemView : View, itemClick: (FirstPaperPlanes) -> Unit) : RecyclerView.ViewHolder(itemView) {

    }
}


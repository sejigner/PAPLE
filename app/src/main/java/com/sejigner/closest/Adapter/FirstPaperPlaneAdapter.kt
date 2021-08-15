package com.sejigner.closest.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.UI.FragmentChatViewModel
import com.sejigner.closest.room.FirstPaperPlanes
import kotlinx.android.synthetic.main.column_paperplane_first.view.*

class FirstPaperPlaneAdapter(var list : List<FirstPaperPlanes>, val viewModel : FragmentChatViewModel) : RecyclerView.Adapter<FirstPaperPlaneAdapter.FirstPaperPlaneViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FirstPaperPlaneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.column_paperplane_first, parent, false)
        return FirstPaperPlaneViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FirstPaperPlaneViewHolder,
        position: Int
    ) {
        var currentPosition = list[position]
        holder.itemView.tv_paperplane_distance.text = currentPosition.flightDistance.toString()
        holder.itemView.tv_paperplane_time.text = currentPosition.timestamp.toString()
        holder.itemView.setOnClickListener {
            // 클릭 시 다이얼로그 실행
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
    inner class FirstPaperPlaneViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView)

}


package com.sejigner.closest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sejigner.closest.R
import com.sejigner.closest.room.FirstPaperPlanes
import com.sejigner.closest.ui.FragmentChatViewModel
import kotlinx.android.synthetic.main.column_paperplane.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


class FirstPaperPlaneAdapter(
    var list: List<FirstPaperPlanes>,
    val viewModel: FragmentChatViewModel,
    val itemClick: (FirstPaperPlanes) -> Unit
) : RecyclerView.Adapter<FirstPaperPlaneAdapter.FirstPaperPlaneViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FirstPaperPlaneViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.column_paperplane, parent, false)

        return FirstPaperPlaneViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(
        holder: FirstPaperPlaneViewHolder,
        position: Int
    ) {
        val currentPosition = differ.currentList[position]
        holder.itemView.setOnClickListener { itemClick(currentPosition) }
        holder.bind(currentPosition)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val differCallback = object : DiffUtil.ItemCallback<FirstPaperPlanes>() {
        override fun areItemsTheSame(
            oldItem: FirstPaperPlanes,
            newItem: FirstPaperPlanes
        ): Boolean {
            return oldItem.fromId == newItem.fromId
        }

        override fun areContentsTheSame(
            oldItem: FirstPaperPlanes,
            newItem: FirstPaperPlanes
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    private fun convertDistanceToString(distance: Double): String {
        return if (distance >= 1000) {
            (round((distance / 1000) * 100) / 100).toString() + "km"
        } else distance.toString() + "m"
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

    inner class FirstPaperPlaneViewHolder(itemView: View, itemClick: (FirstPaperPlanes) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        fun bind(plane: FirstPaperPlanes?) {
            //check for null
            plane?.let {
                itemView.tv_paperplane_message.text = it.message
                itemView.tv_paperplane_distance.text = convertDistanceToString(it.flightDistance)
                itemView.tv_paperplane_time.text = setDateToTextView(it.timestamp)
            }
        }
    }
}


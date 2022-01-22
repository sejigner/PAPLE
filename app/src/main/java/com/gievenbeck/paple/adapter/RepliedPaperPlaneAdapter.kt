package com.gievenbeck.paple.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gievenbeck.paple.R
import com.gievenbeck.paple.room.RepliedPaperPlanes
import com.gievenbeck.paple.ui.FragmentChatViewModel
import kotlinx.android.synthetic.main.column_paperplane.view.*
import java.text.SimpleDateFormat
import java.util.*

class RepliedPaperPlaneAdapter(
    var list: List<RepliedPaperPlanes>,
    val viewModel: FragmentChatViewModel,
    val itemClick: (RepliedPaperPlanes) -> Unit
) : RecyclerView.Adapter<RepliedPaperPlaneAdapter.RepliedPaperPlaneViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RepliedPaperPlaneViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.column_paperplane, parent, false)
        return RepliedPaperPlaneViewHolder(view, itemClick)
    }

    private val differCallback = object : DiffUtil.ItemCallback<RepliedPaperPlanes>() {
        override fun areItemsTheSame(
            oldItem: RepliedPaperPlanes,
            newItem: RepliedPaperPlanes
        ): Boolean {
            return oldItem.fromId == newItem.fromId
        }

        override fun areContentsTheSame(
            oldItem: RepliedPaperPlanes,
            newItem: RepliedPaperPlanes
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onBindViewHolder(
        holder: RepliedPaperPlaneViewHolder,
        position: Int
    ) {
        val currentPosition = differ.currentList[position]
        holder.itemView.setOnClickListener { itemClick(currentPosition) }
        holder.bind(currentPosition)
    }

    private fun convertDistanceToString(distance: Double): String {
        return if (distance >= 1000) {
            (((distance / 1000) * 100) / 100).toString() + "km"
        } else distance.toString() + "m"
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

    inner class RepliedPaperPlaneViewHolder(
        itemView: View,
        itemClick: (RepliedPaperPlanes) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        fun bind(plane: RepliedPaperPlanes?) {
            //check for null
            plane?.let {
                itemView.tv_paperplane_message.text = it.partnerMessage
                itemView.tv_paperplane_distance.text = convertDistanceToString(it.flightDistance)
                itemView.tv_paperplane_time.text = setDateToTextView(it.replyTimestamp)
            }
        }
    }
}


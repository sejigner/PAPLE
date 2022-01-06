package com.sejigner.closest.util

import androidx.recyclerview.widget.DiffUtil
import com.sejigner.closest.room.ChatRooms

class LatestMessageDiffUtil(private val oldList : List<ChatRooms>, private val currentList: List<ChatRooms>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = currentList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].partnerId==currentList[newItemPosition].partnerId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]==currentList[newItemPosition]
    }



}
package com.gievenbeck.paple.util

import androidx.recyclerview.widget.DiffUtil
import com.gievenbeck.paple.room.ChatMessages

class ChatMessageDiffUtil(private val oldList : List<ChatMessages>, private val currentList: List<ChatMessages>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = currentList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].messageId==currentList[newItemPosition].messageId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]==currentList[newItemPosition]
    }



}
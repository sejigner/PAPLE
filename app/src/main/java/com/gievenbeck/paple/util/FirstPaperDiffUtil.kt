package com.sejigner.closest.util

import androidx.recyclerview.widget.DiffUtil
import com.sejigner.closest.room.FirstPaperPlanes

class FirstPaperDiffUtil(private val oldList : List<FirstPaperPlanes>, private val currentList: List<FirstPaperPlanes>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = currentList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].fromId==currentList[newItemPosition].fromId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]==currentList[newItemPosition]
    }



}
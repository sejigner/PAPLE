package com.gievenbeck.paple.util

import androidx.recyclerview.widget.DiffUtil
import com.gievenbeck.paple.room.MyPaper

class MyPaperDiffUtil(private val oldList : List<MyPaper>, private val currentList: List<MyPaper>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = currentList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].messageId==currentList[newItemPosition].messageId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]==currentList[newItemPosition]
    }



}
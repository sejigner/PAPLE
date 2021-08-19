package com.sejigner.closest.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sejigner.closest.room.FirstPaperPlanes
import com.sejigner.closest.room.MyPaperPlaneRecord
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.room.RepliedPaperPlanes
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FragmentChatViewModel(private val repository: PaperPlaneRepository) : ViewModel() {
    // In coroutines thread insert item in insert function.
    fun insert(item: FirstPaperPlanes) = GlobalScope.launch {
        repository.insert(item)
    }

    fun insert(item: RepliedPaperPlanes) = GlobalScope.launch {
        repository.insert(item)
    }

    // In coroutines thread delete item in delete function.
    fun delete(item: FirstPaperPlanes) = GlobalScope.launch {
        repository.delete(item)
    }

    fun delete(item: RepliedPaperPlanes) = GlobalScope.launch {
        repository.delete(item)
    }

    fun delete(item: MyPaperPlaneRecord) = GlobalScope.launch {
        repository.delete(item)
    }

    fun getWithId(fromId: String): MyPaperPlaneRecord {
        return repository.getWithId(fromId)
    }

    // Here we initialized allPaperPlanes function with repository
    fun allFirstPaperPlanes() = repository.allFirstPaperPlanes()
    fun allRepliedPaperPlanes() = repository.allRepliedPaperPlanes()
}
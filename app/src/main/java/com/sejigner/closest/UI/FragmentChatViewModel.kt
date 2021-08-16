package com.sejigner.closest.UI

import androidx.lifecycle.ViewModel
import com.sejigner.closest.room.FirstPaperPlanes
import com.sejigner.closest.room.PaperPlaneRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FragmentChatViewModel(private val repository: PaperPlaneRepository) : ViewModel() {
    // In coroutines thread insert item in insert function.
    fun insert(item: FirstPaperPlanes) = GlobalScope.launch {
        repository.insert(item)
    }

    // In coroutines thread delete item in delete function.
    fun delete(item: FirstPaperPlanes) = GlobalScope.launch {
        repository.delete(item)
    }

    // Here we initialized allPaperPlanes function with repository
    fun allFirstPaperPlanes() = repository.allFirstPaperPlanes()
}
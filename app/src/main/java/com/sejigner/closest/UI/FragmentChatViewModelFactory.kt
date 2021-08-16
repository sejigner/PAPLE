package com.sejigner.closest.UI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sejigner.closest.room.PaperPlaneRepository

class FragmentChatViewModelFactory(private val repository: PaperPlaneRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FragmentChatViewModel(repository) as T
    }
}
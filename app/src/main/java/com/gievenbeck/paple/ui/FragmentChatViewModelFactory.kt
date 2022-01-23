package com.gievenbeck.paple.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gievenbeck.paple.room.PaperPlaneRepository

class FragmentChatViewModelFactory(private val repository: PaperPlaneRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FragmentChatViewModel(repository) as T
    }
}
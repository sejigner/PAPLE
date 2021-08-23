package com.sejigner.closest.UI

import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sejigner.closest.room.*
import kotlinx.coroutines.*


class FragmentChatViewModel(private val repository: PaperPlaneRepository) : ViewModel() {

    // In coroutines thread insert item in insert function.
    fun insert(item: FirstPaperPlanes) = viewModelScope.launch {
        repository.insert(item)
    }

    fun insert(item: RepliedPaperPlanes) = viewModelScope.launch {
        repository.insert(item)
    }

    fun insert(item: List<ChatMessages>) = viewModelScope.launch {
        repository.insert(item)
    }

    fun insert(item: ChatMessages) = viewModelScope.launch {
        repository.insert(item)
    }

    fun insert(rooms: ChatRooms) = viewModelScope.launch {
        repository.insert(rooms)
    }

    // In coroutines thread delete item in delete function.
    fun delete(item: FirstPaperPlanes) = viewModelScope.launch {
        repository.delete(item)
    }

    fun delete(item: RepliedPaperPlanes) = viewModelScope.launch {
        repository.delete(item)
    }

    fun delete(item: MyPaperPlaneRecord) = viewModelScope.launch {
        repository.delete(item)
    }

    fun delete(item:ChatRooms) = viewModelScope.launch {
        repository.delete(item)
    }

    fun getWithId(fromId: String) = CoroutineScope(Dispatchers.IO).async{
        repository.getWithId(fromId)
    }

    fun exists(partnerId: String) = viewModelScope.launch {
        repository.exists(partnerId)
    }

    fun update(messageList: List<ChatMessages>) = viewModelScope.launch {
        repository.update(messageList)
    }

    fun insertOrUpdate(rooms : List<ChatMessages>) = viewModelScope.launch {
        repository.insertOrUpdate(rooms)
    }

    fun insertOrUpdate(message: ChatMessages) = viewModelScope.launch {
        repository.insertOrUpdate(message)
    }

    // Here we initialized allPaperPlanes function with repository
    fun allFirstPaperPlanes() = repository.allFirstPaperPlanes()
    fun allRepliedPaperPlanes() = repository.allRepliedPaperPlanes()
    fun allChatMessages() = repository.allChatMessages()
    fun allChatRooms() = repository.allChatRoomsWithMessages()
}
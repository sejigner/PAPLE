package com.sejigner.closest.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sejigner.closest.room.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO


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

    fun getChatRoom(partnerId: String) = CoroutineScope(IO).async {
        repository.getChatRoom(partnerId)
    }

    fun getWithId(fromId: String) = CoroutineScope(IO).async{
        repository.getWithId(fromId)
    }
    fun insert(record: MyPaperPlaneRecord) = CoroutineScope(IO).launch {
        repository.insert(record)
    }

    fun exists(partnerId: String) = CoroutineScope(IO).async {
        repository.exists(partnerId)
    }

    fun updateLastMessages(partnerId: String, message : String, messageTimestamp : Long) = CoroutineScope(IO).launch {
        repository.updateLastMessages(partnerId, message, messageTimestamp)
    }

    fun getChatRoomsTimestamp(partnerId: String) = CoroutineScope(IO).async {
        repository.getChatRoomsTimestamp(partnerId)
    }

    fun haveMet(uid : String) = CoroutineScope(IO).async {
        repository.haveMet(uid)
    }

    fun insert(acquaintance : String) = CoroutineScope(IO).launch {
        repository.insert(acquaintance)
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
    fun allChatMessages(partnerId: String) = repository.allChatMessages(partnerId)
    fun allChatRooms() = repository.allChatRooms    ()
}
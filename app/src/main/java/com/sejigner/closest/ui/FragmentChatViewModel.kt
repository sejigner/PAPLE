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

    fun delete(item: RepliedPaperPlanes) = CoroutineScope(IO).launch {
        repository.delete(item)
    }

    fun delete(item : MyPaperPlaneRecord) = CoroutineScope(IO).launch {
        repository.delete(item)
    }

    fun delete(item:ChatRooms) = CoroutineScope(IO).launch {
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

    fun haveMet(uid : String, partnerId: String) = CoroutineScope(IO).async {
        repository.haveMet(uid, partnerId)
    }

    fun insert(acquaintance : Acquaintances) = CoroutineScope(IO).launch {
        repository.insert(acquaintance)
    }

    fun deleteChatRoom(partnerId: String) = CoroutineScope(IO).launch {
        repository.deleteChatRoom(partnerId)
    }

    fun deleteAllMessages(partnerId: String) = CoroutineScope(IO).launch {
        repository.deleteAllMessages(partnerId)
    }


    fun insertOrUpdate(rooms : List<ChatMessages>) = viewModelScope.launch {
        repository.insertOrUpdate(rooms)
    }

    fun insertOrUpdate(message: ChatMessages) = viewModelScope.launch {
        repository.insertOrUpdate(message)
    }

    fun chatRoomAndAllMessages(partnerId: String) = CoroutineScope(IO).async {
        repository.getChatRoomsAndAllMessages(partnerId)
    }

    // Here we initialized allPaperPlanes function with repository
    fun allFirstPaperPlanes() = repository.allFirstPaperPlanes()
    fun allRepliedPaperPlanes() = repository.allRepliedPaperPlanes()
    fun allChatMessages(uid: String, partnerId: String) = repository.allChatMessages(uid, partnerId)
    fun allChatRooms(uid: String) = repository.allChatRooms(uid)
}
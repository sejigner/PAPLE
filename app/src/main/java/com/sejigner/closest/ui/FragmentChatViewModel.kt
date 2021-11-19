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

    fun getChatRoom(uid: String, partnerId: String) = CoroutineScope(IO).async {
        repository.getChatRoom(uid, partnerId)
    }

    fun getWithId(uid: String, fromId: String) = CoroutineScope(IO).async{
        repository.getWithId(uid, fromId)
    }
    fun insert(record: MyPaperPlaneRecord) = CoroutineScope(IO).launch {
        repository.insert(record)
    }

    fun exists(uid: String, partnerId: String) = CoroutineScope(IO).async {
        repository.exists(uid, partnerId)
    }

    fun updateLastMessages(uid: String, partnerId: String, message : String, messageTimestamp : Long) = CoroutineScope(IO).launch {
        repository.updateLastMessages(uid, partnerId, message, messageTimestamp)
    }

    fun getChatRoomsTimestamp(uid: String, partnerId: String) = CoroutineScope(IO).async {
        repository.getChatRoomsTimestamp(uid, partnerId)
    }

    fun getLatestTimestamp(uid: String, partnerId: String) = CoroutineScope(IO).async {
        repository.getLatestTimestamp(uid, partnerId)
    }

    fun haveMet(uid : String, partnerId: String) = CoroutineScope(IO).async {
        repository.haveMet(uid, partnerId)
    }

    fun insert(acquaintance : Acquaintances) = CoroutineScope(IO).launch {
        repository.insert(acquaintance)
    }

    fun deleteChatRoom(uid: String, partnerId: String) = CoroutineScope(IO).launch {
        repository.deleteChatRoom(uid, partnerId)
    }

    fun deleteAllMessages(uid: String, partnerId: String) = CoroutineScope(IO).launch {
        repository.deleteAllMessages(uid, partnerId)
    }


    fun insertOrUpdate(rooms : List<ChatMessages>) = viewModelScope.launch {
        repository.insertOrUpdate(rooms)
    }

    fun insertOrUpdate(message: ChatMessages) = viewModelScope.launch {
        repository.insertOrUpdate(message)
    }

    fun chatRoomAndAllMessages(uid: String, partnerId: String) = CoroutineScope(IO).async {
        repository.getChatRoomsAndAllMessages(uid, partnerId)
    }

    // Here we initialized allPaperPlanes function with repository
    fun allFirstPaperPlanes(uid: String) = repository.allFirstPaperPlanes(uid)
    fun allRepliedPaperPlanes(uid: String) = repository.allRepliedPaperPlanes(uid)
    fun allChatMessages(uid: String, partnerId: String) = repository.allChatMessages(uid, partnerId)
    fun allChatRooms(uid: String) = repository.allChatRooms(uid)
}
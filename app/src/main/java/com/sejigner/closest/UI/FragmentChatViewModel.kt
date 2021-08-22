package com.sejigner.closest.UI

import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sejigner.closest.room.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus


class FragmentChatViewModel(private val repository: PaperPlaneRepository) : ViewModel() {

    // In coroutines thread insert item in insert function.
    fun insert(item: FirstPaperPlanes) = GlobalScope.launch {
        repository.insert(item)
    }

    fun insert(item: RepliedPaperPlanes) = GlobalScope.launch {
        repository.insert(item)
    }

    fun insert(item: List<ChatMessages>) = GlobalScope.launch {
        repository.insert(item)
    }

    fun insert(item: ChatMessages) = GlobalScope.launch {
        repository.insert(item)
    }

    fun insert(rooms: ChatRooms) = GlobalScope.launch {
        repository.insert(rooms)
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

    fun delete(item:ChatRooms) = GlobalScope.launch {
        repository.delete(item)
    }

    fun getWithId(fromId: String) = GlobalScope.launch{
        repository.getWithId(fromId)
    }

    fun getLatestMessage(chatRoomId : String) : LiveData<ChatMessages> {
        return repository.getLatestMessage(chatRoomId)
    }

    fun exists(partnerId: String) = GlobalScope.launch {
        repository.exists(partnerId)
    }

    fun update(messageList: List<ChatMessages>) = GlobalScope.launch {
        repository.update(messageList)
    }

    fun insertOrUpdate(rooms : List<ChatMessages>) = GlobalScope.launch {
        repository.insertOrUpdate(rooms)
    }

    fun insertOrUpdate(message: ChatMessages) = GlobalScope.launch {
        repository.insertOrUpdate(message)
    }

    // Here we initialized allPaperPlanes function with repository
    fun allFirstPaperPlanes() = repository.allFirstPaperPlanes()
    fun allRepliedPaperPlanes() = repository.allRepliedPaperPlanes()
    fun allChatMessages() = repository.allChatMessages()
    fun allChatRooms() = repository.allChatRoomsWithMessages()
}
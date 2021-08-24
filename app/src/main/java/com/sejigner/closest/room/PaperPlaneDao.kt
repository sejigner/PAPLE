package com.sejigner.closest.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FirstPaperPlaneDao {
    @Query("SELECT * FROM first_paper_planes")
    fun getAllFirstPlanes(): LiveData<List<FirstPaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: FirstPaperPlanes)

    @Update
    fun update(paperPlane: FirstPaperPlanes)

    @Delete
    suspend fun delete(paperPlane: FirstPaperPlanes)

    @Query("DELETE FROM first_paper_planes")
    suspend fun deleteAll()
}

@Dao
interface MyPaperPlaneRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MyPaperPlaneRecord)

    @Query("SELECT * FROM my_message_record WHERE partnerId = :partnerId LIMIT 1" )
    suspend fun getWithId(partnerId: String) : MyPaperPlaneRecord?
    @Delete
    suspend fun delete(record: MyPaperPlaneRecord)
}


@Dao
interface RepliedPaperPlaneDao {
    @Query("SELECT * FROM replied_paper_planes")
    fun getAllRepliedPlanes(): LiveData<List<RepliedPaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: RepliedPaperPlanes)

    @Update
    fun update(paperPlane: RepliedPaperPlanes)

    @Delete
    suspend fun delete(paperPlane: RepliedPaperPlanes)

    @Query("DELETE FROM replied_paper_planes")
    suspend fun deleteAll()
}

@Dao
interface ChatRoomsDao {
    // Room and Messages
    @Query("SELECT * FROM chat_rooms")
    fun getAllChatRooms(): LiveData<List<ChatRooms>>

    @Transaction
    @Query("SELECT EXISTS (SELECT 1 FROM chat_rooms WHERE partnerId = :partnerId)")
    suspend fun exists(partnerId: String): Boolean

    @Update
    suspend fun update(messageList: List<ChatMessages>)

    @Transaction
    suspend fun insertOrUpdate(messageList : List<ChatMessages>) {
        val insertResult = insert(messageList)
        val updateList = mutableListOf<ChatMessages>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) updateList.add(messageList[i])
        }

        if(updateList.isNotEmpty()) update(updateList)
    }

    @Delete
    suspend fun delete(chatRoom: ChatRooms)

    @Query("SELECT * FROM chat_rooms  WHERE partnerId = :partnerId")
    suspend fun getChatRoom(partnerId: String) : ChatRooms

    // Chatroom
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: ChatRooms)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageList: List<ChatMessages>) : List<Long>

    @Query("UPDATE chat_rooms SET lastMessage = :lastMessage, lastMessageTimestamp = :lastMessageTimestamp WHERE partnerId = :partnerId")
    suspend fun updateLastMessages(partnerId: String, lastMessage: String, lastMessageTimestamp: Long)


    // Message

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessages) : Long

    @Update
    suspend fun update(message: ChatMessages)

    @Transaction
    suspend fun insertOrUpdate(message: ChatMessages) {
        val id = insert(message)
        if (id == -1L) update(message)
    }

}

@Dao
interface ChatMessagesDao {
    @Query("SELECT * FROM chat_messages")
    fun getAllChatMessages(): LiveData<List<ChatMessages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessages)

}

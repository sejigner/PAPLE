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

    @Query("SELECT * FROM my_message_record where fromId = :fromId")
    fun getWithId(fromId: String) : MyPaperPlaneRecord
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
    @Transaction
    @Query("SELECT * FROM chat_rooms")
    fun getAllChatRoomsWithMessages(): LiveData<List<ChatRoomsWithMessages>>

    @Transaction
    @Query("SELECT * FROM chat_rooms WHERE partnerId = :partnerId")
    fun getWithId(partnerId: String) : ChatRoomsWithMessages

    @Transaction
    @Query("SELECT EXISTS (SELECT 1 FROM chat_rooms WHERE partnerId = :partnerId)")
    fun exists(partnerId: String): Boolean

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

    @Transaction
    @Query("SELECT * FROM chat_messages WHERE chatRoomId = :chatRoomId  ORDER BY timestamp DESC LIMIT 1 ")
    fun getLatestMessage(chatRoomId : String) : ChatMessages

    // Chatroom
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: ChatRooms)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageList: List<ChatMessages>) : List<Long>

    @Update
    suspend fun update(messageList: List<ChatMessages>)


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

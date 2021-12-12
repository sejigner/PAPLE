package com.sejigner.closest.room

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface AcquaintancesDao {
    @Query("SELECT EXISTS (SELECT 1 FROM acquaintances WHERE uid = :uid and partnerId = :partnerId)")
    suspend fun haveMet(uid:String, partnerId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(acquaintance: Acquaintances)
}

@Dao
interface SentPaperPlaneDao {
    @Query("SELECT * FROM sent_paper_planes WHERE uid = :uid")
    fun getAllSentPlanes(uid: String): LiveData<List<SentPaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: SentPaperPlanes)

    @Delete
    suspend fun delete(paperPlane: SentPaperPlanes)
}

@Dao
interface FirstPaperPlaneDao {
    @Query("SELECT * FROM first_paper_planes WHERE uid = :uid")
    fun getAllFirstPlanes(uid: String): LiveData<List<FirstPaperPlanes>>

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

    @Query("SELECT * FROM my_message_record WHERE uid = :uid and partnerId = :partnerId LIMIT 1")
    suspend fun getWithId(uid: String, partnerId: String): MyPaperPlaneRecord?

    @Delete
    suspend fun delete(record: MyPaperPlaneRecord)
}


@Dao
interface RepliedPaperPlaneDao {
    @Query("SELECT * FROM replied_paper_planes WHERE uid = :uid")
    fun getAllRepliedPlanes(uid: String): LiveData<List<RepliedPaperPlanes>>


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
    @Query("SELECT * FROM chat_rooms WHERE uid = :uid ORDER BY lastMessageTimestamp DESC")
    fun getAllChatRooms(uid: String): LiveData<List<ChatRooms>>

    @Transaction
    @Query("SELECT EXISTS (SELECT 1 FROM chat_rooms WHERE uid = :uid and partnerId = :partnerId)")
    suspend fun exists(uid: String, partnerId: String): Boolean

    @Query("SELECT isOver FROM chat_rooms  WHERE uid = :uid and partnerId = :partnerId")
    suspend fun isOver(uid: String, partnerId: String): Boolean

    @Update
    suspend fun update(messageList: List<ChatMessages>)

    @Query("UPDATE chat_rooms  SET isOver = :isOver WHERE uid = :uid and partnerId = :partnerId ")
    suspend fun updateChatroom(
        uid : String,
        partnerId: String,
        isOver : Boolean
    )

    @Transaction
    suspend fun insertOrUpdate(messageList: List<ChatMessages>) {
        val insertResult = insert(messageList)
        val updateList = mutableListOf<ChatMessages>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) updateList.add(messageList[i])
        }

        if (updateList.isNotEmpty()) update(updateList)
    }

    @Delete
    suspend fun delete(chatRoom: ChatRooms)

    @Query("SELECT * FROM chat_rooms  WHERE uid = :uid and partnerId = :partnerId")
    suspend fun getChatRoom(uid: String, partnerId: String): ChatRooms

    @Query("SELECT lastMessageTimestamp FROM chat_rooms  WHERE uid = :uid and partnerId = :partnerId")
    suspend fun getChatRoomsTimestamp(uid: String, partnerId: String): Long?

    // Chatroom

    @Query("DELETE FROM chat_rooms WHERE uid = :uid and partnerId = :partnerId")
    fun deleteChatRoom(uid : String, partnerId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: ChatRooms)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messageList: List<ChatMessages>): List<Long>

    @Query("UPDATE chat_rooms  SET lastMessage = :lastMessage, lastMessageTimestamp = :lastMessageTimestamp WHERE uid = :uid and partnerId = :partnerId ")
    suspend fun updateLastMessages(
        uid: String,
        partnerId: String,
        lastMessage: String,
        lastMessageTimestamp: Long
    )

    // Message

    @Query("SELECT lastMessageTimestamp FROM chat_rooms   WHERE uid = :uid and partnerId = :partnerId ORDER BY lastMessageTimestamp DESC LIMIT 1")
    suspend fun getLatestTimestamp(uid: String, partnerId: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessages): Long

    @Update
    suspend fun update(message: ChatMessages)

    @Transaction
    suspend fun insertOrUpdate(message: ChatMessages) {
        val id = insert(message)
        if (id == -1L) update(message)
    }

}

@Dao
interface ChatRoomsAndMessagesDao {
    @Transaction
    @Query("SELECT * FROM chat_rooms WHERE uid = :uid and partnerId = :partnerId")
    suspend fun getChatRoomAndMessages(uid: String, partnerId: String) : List<ChatRoomsAndMessages>

}

@Dao
interface ChatMessagesDao {
    @Query("SELECT * FROM chat_messages WHERE uid = :uid and chatRoomId = :chatRoomId ORDER BY timestamp ASC ")
    fun getAllChatMessages(uid: String, chatRoomId: String): LiveData<List<ChatMessages>>

    @Query("DELETE FROM chat_messages WHERE uid = :uid and chatRoomId = :partnerId")
    fun deleteAllMessages(uid: String, partnerId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessages)

}


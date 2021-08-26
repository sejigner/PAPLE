package com.sejigner.closest.room

import androidx.lifecycle.LiveData


class PaperPlaneRepository(private val db: PaperPlaneDatabase) {

    private val rooms: LiveData<List<ChatRooms>> = db.getChatRoomsDao().getAllChatRooms()

    // 첫 비행기
    suspend fun insert(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().delete(paperPlane)
    // 답장 비행기
    suspend fun insert(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().delete(paperPlane)

    // 내가 보낸 비행기
    suspend fun getWithId(id: String): MyPaperPlaneRecord? = db.getMyPaperPlaneRecordDao().getWithId(id)
    suspend fun delete(record: MyPaperPlaneRecord) = db.getMyPaperPlaneRecordDao().delete(record)
    suspend fun insert(record: MyPaperPlaneRecord) = db.getMyPaperPlaneRecordDao().insert(record)

    // 채팅방
    suspend fun insert(messageList: List<ChatMessages>) = db.getChatRoomsDao().insert(messageList)
    suspend fun insert(rooms: ChatRooms) = db.getChatRoomsDao().insert(rooms)
    suspend fun delete(rooms: ChatRooms) = db.getChatRoomsDao().delete(rooms)
    suspend fun insertOrUpdate(messageList : List<ChatMessages>) = db.getChatRoomsDao().insertOrUpdate(messageList)
    suspend fun updateLastMessages(partnerId: String, lastMessage: String, lastMessageTimestamp: Long) = db.getChatRoomsDao().updateLastMessages(partnerId,lastMessage,lastMessageTimestamp)
    suspend fun exists(partnerId: String): Boolean = db.getChatRoomsDao().exists(partnerId)
    suspend fun getChatRoom(partnerId: String) : ChatRooms = db.getChatRoomsDao().getChatRoom(partnerId)
    suspend fun getChatRoomsTimestamp(partnerId: String) : Long? = db.getChatRoomsDao().getChatRoomsTimestamp(partnerId)

    // 메세지
    suspend fun insert(messages: ChatMessages) = db.getChatMessagesDao().insert(messages)
    suspend fun insertOrUpdate(message: ChatMessages) = db.getChatRoomsDao().insertOrUpdate(message)


    fun allFirstPaperPlanes() = db.getFirstPaperPlaneDao().getAllFirstPlanes()
    fun allRepliedPaperPlanes() = db.getRepliedPaperPlaneDao().getAllRepliedPlanes()
    fun allChatMessages(partnerId: String) = db.getChatMessagesDao().getAllChatMessages(partnerId)
    fun allChatRooms() : LiveData<List<ChatRooms>> {
        return rooms
    }

}
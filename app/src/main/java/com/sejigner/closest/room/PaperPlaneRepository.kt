package com.sejigner.closest.room

import androidx.lifecycle.LiveData




class PaperPlaneRepository(private val db: PaperPlaneDatabase) {
    // 첫 비행기
    suspend fun insert(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().delete(paperPlane)
    // 답장 비행기
    suspend fun insert(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().delete(paperPlane)

    // 내가 보낸 비행기
    suspend fun getWithId(id: String): MyPaperPlaneRecord? = db.getMyPaperPlaneRecordDao().getWithId(id)
    suspend fun delete(record: MyPaperPlaneRecord) = db.getMyPaperPlaneRecordDao().delete(record)

    // 채팅방
    suspend fun insert(messageList: List<ChatMessages>) = db.getChatRoomsDao().insert(messageList)
    suspend fun insert(rooms: ChatRooms) = db.getChatRoomsDao().insert(rooms)
    suspend fun delete(rooms: ChatRooms) = db.getChatRoomsDao().delete(rooms)
    suspend fun insertOrUpdate(messageList : List<ChatMessages>) = db.getChatRoomsDao().insertOrUpdate(messageList)
    suspend fun update(messageList: List<ChatMessages>) = db.getChatRoomsDao().update(messageList)
    suspend fun exists(partnerId: String): Boolean = db.getChatRoomsDao().exists(partnerId)

    // 메세지
    suspend fun insert(messages: ChatMessages) = db.getChatMessagesDao().insert(messages)
    fun getLatestMessage(chatRoomId : String) : LiveData<ChatMessages> = db.getChatRoomsDao().getLatestMessage(chatRoomId)
    suspend fun insertOrUpdate(message: ChatMessages) = db.getChatRoomsDao().insertOrUpdate(message)


    fun allFirstPaperPlanes() = db.getFirstPaperPlaneDao().getAllFirstPlanes()
    fun allRepliedPaperPlanes() = db.getRepliedPaperPlaneDao().getAllRepliedPlanes()
    fun allChatMessages() = db.getChatMessagesDao().getAllChatMessages()
    fun allChatRoomsWithMessages() = db.getChatRoomsDao().getAllChatRoomsWithMessages()

}
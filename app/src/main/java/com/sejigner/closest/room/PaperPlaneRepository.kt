package com.sejigner.closest.room

import androidx.lifecycle.LiveData


class PaperPlaneRepository(private val db: PaperPlaneDatabase) {

//    private val rooms: LiveData<List<ChatRooms>> = db.getChatRoomsDao().getAllChatRooms()

    // 첫 비행기
    suspend fun insert(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().delete(paperPlane)
    // 답장 비행기
    suspend fun insert(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().delete(paperPlane)

    // 내가 보낸 비행기
    suspend fun getWithId(uid: String, fromId: String): MyPaperPlaneRecord? = db.getMyPaperPlaneRecordDao().getWithId(uid, fromId)
    suspend fun delete(record: MyPaperPlaneRecord) = db.getMyPaperPlaneRecordDao().delete(record)
    suspend fun insert(record: MyPaperPlaneRecord) = db.getMyPaperPlaneRecordDao().insert(record)

    // 채팅방
    suspend fun insert(messageList: List<ChatMessages>) = db.getChatRoomsDao().insert(messageList)
    suspend fun insert(rooms: ChatRooms) = db.getChatRoomsDao().insert(rooms)
    suspend fun delete(rooms: ChatRooms) = db.getChatRoomsDao().delete(rooms)
    suspend fun insertOrUpdate(messageList : List<ChatMessages>) = db.getChatRoomsDao().insertOrUpdate(messageList)
    suspend fun updateLastMessages(uid: String, partnerId: String, lastMessage: String, lastMessageTimestamp: Long) = db.getChatRoomsDao().updateLastMessages(uid, partnerId,lastMessage,lastMessageTimestamp)
    suspend fun exists(uid :String, partnerId: String): Boolean = db.getChatRoomsDao().exists(uid, partnerId)
    suspend fun getChatRoom(uid: String, partnerId: String) : ChatRooms = db.getChatRoomsDao().getChatRoom(uid, partnerId)
    suspend fun getChatRoomsTimestamp(uid: String, partnerId: String) : Long? = db.getChatRoomsDao().getChatRoomsTimestamp(uid, partnerId)
    suspend fun deleteChatRoom(uid: String, partnerId: String) = db.getChatRoomsDao().deleteChatRoom(uid, partnerId)

    // 메세지
    suspend fun insert(messages: ChatMessages) = db.getChatMessagesDao().insert(messages)
    suspend fun insertOrUpdate(message: ChatMessages) = db.getChatRoomsDao().insertOrUpdate(message)
    suspend fun deleteAllMessages(uid: String, partnerId: String) = db.getChatMessagesDao().deleteAllMessages(uid, partnerId)

    // 만난 유저 체크
    suspend fun haveMet(uid: String, partnerId: String) : Boolean = db.getAcquaintancesDao().haveMet(uid, partnerId)
    suspend fun insert(acquaintance: Acquaintances) = db.getAcquaintancesDao().insert(acquaintance)

    suspend fun getChatRoomsAndAllMessages(uid: String, partnerId: String) : List<ChatRoomsAndMessages> = db.getChatRoomAndMessageDao().getChatRoomAndMessages(uid, partnerId)

    fun allFirstPaperPlanes(uid: String) = db.getFirstPaperPlaneDao().getAllFirstPlanes(uid)
    fun allRepliedPaperPlanes(uid: String) = db.getRepliedPaperPlaneDao().getAllRepliedPlanes(uid)
    fun allChatMessages(uid: String, partnerId: String) = db.getChatMessagesDao().getAllChatMessages(uid, partnerId)
//    fun allChatRooms(uid: String) : LiveData<List<ChatRooms>> {
//        return rooms
//    }
    fun allChatRooms(uid: String) = db.getChatRoomsDao().getAllChatRooms(uid)

}
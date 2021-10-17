package com.sejigner.closest.room

import androidx.room.*


@Entity(tableName = "acquaintances")
data class Acquaintances(
    @PrimaryKey @ColumnInfo(name = "partnerId")
    val partnerId: String,

    @ColumnInfo(name = "uid")
    val uid: String
)

@Entity(tableName = "first_paper_planes")
data class FirstPaperPlanes(
    @PrimaryKey @ColumnInfo(name = "fromId")
    val fromId: String,

    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "message")
    val message: String?,

    @ColumnInfo(name = "flightDistance")
    val flightDistance: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)

@Entity(tableName = "my_message_record")
data class MyPaperPlaneRecord(
    @PrimaryKey @ColumnInfo(name = "partnerId")
    val partnerId: String,

    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "userMessage")
    val userMessage: String?,

    @ColumnInfo(name = "firstTimestamp")
    val firstTimestamp: Long
)


@Entity(tableName = "replied_paper_planes")
data class RepliedPaperPlanes(
    @PrimaryKey @ColumnInfo(name = "fromId")
    val fromId: String,

    @ColumnInfo(name = "uid")
    val uid: String,

    @ColumnInfo(name = "userMessage")
    val userMessage: String?,

    @ColumnInfo(name = "partnerMessage")
    val partnerMessage: String?,

    @ColumnInfo(name = "flightDistance")
    val flightDistance: Double,

    @ColumnInfo(name = "firstTimestamp")
    val firstTimestamp: Long,

    @ColumnInfo(name = "replyTimestamp")
    val replyTimestamp: Long
)


@Entity(tableName = "chat_rooms")
data class ChatRooms(
    @PrimaryKey @ColumnInfo val partnerId: String,
    val partnerNickname: String?,
    @ColumnInfo(name = "uid")
    val uid: String,
    @ColumnInfo(name = "lastMessage")
    val lastMessage: String?,
    @ColumnInfo(name = "lastMessageTimestamp")
    val lastMessageTimestamp: Long?
)

@Entity(tableName = "uid")
data class Uid(
    @PrimaryKey @ColumnInfo val uid: String
)

data class UidWithFirstPlanes(
    @Embedded val uid: Uid,
    @Relation(
        parentColumn = "uid",
        entity = FirstPaperPlanes::class,
        entityColumn = "uid"
    )
    var firstPlanes: List<FirstPaperPlanes> = ArrayList()
)

data class UidWithRepliedPlanes(
    @Embedded val uid: Uid,
    @Relation(
        parentColumn = "uid",
        entity = RepliedPaperPlanes::class,
        entityColumn = "uid"
    )
    var repliedPlanes: List<RepliedPaperPlanes> = ArrayList()
)

data class UidWithChatRooms(
    @Embedded val uid: Uid,
    @Relation(
        parentColumn = "uid",
        entity = ChatRooms::class,
        entityColumn = "uid"
    )
    var chatRooms: List<ChatRooms> = ArrayList()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatRooms::class,
            parentColumns = arrayOf("partnerId"),
            childColumns = arrayOf("chatRoomId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChatMessages(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "chatRoomId", index = true)
    val chatRoomId: String?,
    val meOrPartner: Int,
    val message: String?,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long?
)

data class ChatRoomsAndMessages(
    @Embedded val room: ChatRooms,
    @Relation(
        parentColumn = "partnerId",
        entity = ChatMessages::class,
        entityColumn = "chatRoomId"
    )
    var chatMessages: List<ChatMessages> = ArrayList()
)




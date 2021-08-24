package com.sejigner.closest.room

import androidx.room.*

@Entity(tableName = "first_paper_planes")
data class FirstPaperPlanes(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "fromId")
    val fromId: String?,

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

    @ColumnInfo(name = "userMessage")
    val userMessage: String?,

    @ColumnInfo(name = "firstTimestamp")
    val firstTimestamp: Long
)


@Entity(tableName = "replied_paper_planes")
data class RepliedPaperPlanes(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "userMessage")
    val userMessage: String?,

    @ColumnInfo(name = "fromId")
    val fromId: String?,

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
    @ColumnInfo(name = "lastMessage")
    val lastMessage : String?,
    @ColumnInfo(name = "lastMessageTimestamp")
    val lastMessageTimestamp : Long?
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
    @ColumnInfo(name = "chatRoomId")
    val chatRoomId: String?,
    val partnerOrMe: Boolean,
    val message: String?,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long?
)

data class ChatRoomsWithMessages(
    @Embedded val room: ChatRooms,
    @Relation(
        parentColumn = "partnerId",
        entityColumn = "chatRoomId"
    )
    val chatMessages: List<ChatMessages>
)



package com.gievenbeck.paple.room

import androidx.annotation.Keep
import androidx.room.*

@Keep
@Entity(tableName = "acquaintances")
data class Acquaintances(
    @PrimaryKey @ColumnInfo(name = "partnerId")
    val partnerId: String,

    @ColumnInfo(name = "uid")
    val uid: String
)
@Keep
@Entity(tableName = "first_paper_planes")
data class FirstPaperPlanes(
    @PrimaryKey @ColumnInfo(name = "fromId", index = true)
    val fromId: String,

    @ColumnInfo(name = "uid", index = true)
    val uid: String,

    @ColumnInfo(name = "message")
    val message: String?,

    @ColumnInfo(name = "flightDistance")
    val flightDistance: Double,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)
@Keep
@Entity(tableName = "my_paper")
data class MyPaper(
    @PrimaryKey(autoGenerate = true)
    var messageId: Int? = null,

    @ColumnInfo(name = "uid", index = true)
    val uid : String,

    @ColumnInfo(name = "text")
    val text : String,

    @ColumnInfo(name = "timestamp")
    val timestamp : Long
)
@Keep
@Entity(tableName = "my_message_record")
data class MyPaperPlaneRecord(
    @PrimaryKey @ColumnInfo(name = "partnerId")
    val partnerId: String,

    @ColumnInfo(name = "uid", index = true)
    val uid: String,

    @ColumnInfo(name = "userMessage")
    val userMessage: String?,

    @ColumnInfo(name = "firstTimestamp")
    val firstTimestamp: Long
)
@Keep
@Entity(tableName = "finishedChat")
data class FinishedChat(
    @PrimaryKey @ColumnInfo(name = "partnerId", index = true)
    val partnerId : String,
    @ColumnInfo(name = "uid", index = true)
    val uid: String
)

@Keep
@Entity(tableName = "replied_paper_planes")
data class RepliedPaperPlanes(
    @PrimaryKey @ColumnInfo(name = "fromId", index = true)
    val fromId: String,

    @ColumnInfo(name = "uid", index = true)
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

@Keep
@Entity(tableName = "chat_rooms")
data class ChatRooms(
    @PrimaryKey @ColumnInfo(name= "partnerId", index = true)
    val partnerId: String,
    val partnerNickname: String?,
    @ColumnInfo(name = "uid", index = true)
    val uid: String,
    @ColumnInfo(name = "lastMessage")
    val lastMessage: String?,
    @ColumnInfo(name = "lastMessageTimestamp")
    val lastMessageTimestamp: Long?,
    @ColumnInfo(name = "isOver")
    val isOver : Boolean
)
@Keep
@Entity(tableName = "user")
data class User(
    @PrimaryKey @ColumnInfo(name = "uid")
    val uid: String,
    @ColumnInfo(name = "nickname")
    val nickname : String,
    @ColumnInfo(name = "gender")
    val gender : String,
    @ColumnInfo(name = "birthYear")
    val birthYear : Int
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
@Keep
data class ChatMessages(
    @PrimaryKey(autoGenerate = true)
    var messageId: Int? = null,
    @ColumnInfo(name = "chatRoomId", index = true)
    val chatRoomId: String?,
    val uid: String,
    val meOrPartner: Int,
    val message: String?,
    @ColumnInfo(name = "timestamp", index = true)
    val timestamp: Long?
)
@Keep
data class ChatRoomsAndMessages(
    @Embedded val room: ChatRooms,
    @Relation(
        parentColumn = "partnerId",
        entityColumn = "messageId",
        associateBy = Junction(ChatRoomMessageCrossRef::class)
    )
    var chatMessages: List<ChatMessages> = ArrayList()
)

@Entity(primaryKeys = ["partnerId", "messageId"])
data class ChatRoomMessageCrossRef(
    val partnerId: String,
    val messageId: Int
)




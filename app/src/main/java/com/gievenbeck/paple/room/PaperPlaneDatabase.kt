package com.gievenbeck.paple.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FirstPaperPlanes::class, RepliedPaperPlanes::class, MyPaper::class, MyPaperPlaneRecord::class, ChatMessages::class, ChatRooms::class, Acquaintances::class, ChatRoomMessageCrossRef::class, User::class, FinishedChat::class], version = 2, exportSchema = false)
abstract class PaperPlaneDatabase: RoomDatabase() {

    abstract fun getUserInfoDao() : UserInfoDao
    abstract fun getFirstPaperPlaneDao() : FirstPaperPlaneDao
    abstract fun getRepliedPaperPlaneDao() : RepliedPaperPlaneDao
    abstract fun getMyPaperPlaneRecordDao() : MyPaperPlaneRecordDao
    abstract fun getChatRoomsDao() : ChatRoomsDao
    abstract fun getChatMessagesDao() : ChatMessagesDao
    abstract fun getChatRoomAndMessageDao() : ChatRoomsAndMessagesDao
    abstract fun getAcquaintancesDao() : AcquaintancesDao
    abstract fun getMyPaperDao() : MyPaperDao
    abstract fun getFinishedChatDao() : FinishedChatDao


    companion object{
        private var instance: PaperPlaneDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context : Context) = instance?: synchronized(LOCK) {
            instance?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(context.applicationContext, PaperPlaneDatabase::class.java, "PaperPlaneDatabase.db").fallbackToDestructiveMigration().build()

    }
}



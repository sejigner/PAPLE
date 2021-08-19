package com.sejigner.closest.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FirstPaperPlanes::class, RepliedPaperPlanes::class], version = 2)
abstract class PaperPlaneDatabase: RoomDatabase() {

    abstract fun getFirstPaperPlaneDao() : FirstPaperPlaneDao
    abstract fun getRepliedPaperPlaneDao() : RepliedPaperPlaneDao


    companion object{
        private var instance: PaperPlaneDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context : Context) = instance?: synchronized(LOCK) {
            instance?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(context.applicationContext, PaperPlaneDatabase::class.java, "PaperPlaneDatabase.db").build()

    }
}



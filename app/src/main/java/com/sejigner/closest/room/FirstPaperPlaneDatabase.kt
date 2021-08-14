package com.sejigner.closest.room

import android.content.Context
import android.os.strictmode.InstanceCountViolation
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(FirstPaperPlaneRD::class), version = 1, exportSchema = false)
abstract class FirstPaperPlaneDatabase: RoomDatabase() {

    abstract fun paperplaneDao() : FirstPaperPlaneDao

    private class FirstPaperPlaneDatabaseCallback (
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let{ database -> scope.launch {
                var paperplaneDao = database.paperplaneDao()

                // Delete all content here.
                paperplaneDao.deleteAll()

                // Add sample words.
                var firstPaperPlaneRD = FirstPaperPlaneRD("exampleUserId_01","examplePartnerId_01","Example 1",253.0,564154132)
                paperplaneDao.insert(firstPaperPlaneRD)
                firstPaperPlaneRD = FirstPaperPlaneRD("exampleUserId_02","examplePartnerId_02","Example 2",23.0,45646123)
                paperplaneDao.insert(firstPaperPlaneRD)
            }
            }
        }
    }

    companion object{
        private var INSTANCE: FirstPaperPlaneDatabase? = null
        fun getDatabase(context: Context, scope: CoroutineScope): FirstPaperPlaneDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FirstPaperPlaneDatabase::class.java,
                    "paperplane_database")
                    .addCallback(FirstPaperPlaneDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }

        }

    }
}



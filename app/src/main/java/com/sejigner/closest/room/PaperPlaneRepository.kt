package com.sejigner.closest.room

import androidx.lifecycle.LiveData




class PaperPlaneRepository(private val db: PaperPlaneDatabase) {
    suspend fun insert(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().delete(paperPlane)

    suspend fun insert(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: RepliedPaperPlanes) = db.getRepliedPaperPlaneDao().delete(paperPlane)
    fun getWithId(id: String): MyPaperPlaneRecord {
        return db.getMyPaperPlaneRecordDao().getWithId(id)
    }
    suspend fun delete(record: MyPaperPlaneRecord) = db.getMyPaperPlaneRecordDao().delete(record)

    fun allFirstPaperPlanes() = db.getFirstPaperPlaneDao().getAllFirstPlanes()
    fun allRepliedPaperPlanes() = db.getRepliedPaperPlaneDao().getAllRepliedPlanes()
}
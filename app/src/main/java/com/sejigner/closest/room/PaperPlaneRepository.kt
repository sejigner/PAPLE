package com.sejigner.closest.room

class PaperPlaneRepository(private val db: PaperPlaneDatabase) {
    suspend fun insert(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().insert(paperPlane)
    suspend fun delete(paperPlane: FirstPaperPlanes) = db.getFirstPaperPlaneDao().delete(paperPlane)

    fun allFirstPaperPlanes() = db.getFirstPaperPlaneDao().getAllFirstPlanes()
}
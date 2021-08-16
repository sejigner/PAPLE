package com.sejigner.closest.room

import androidx.lifecycle.LiveData
import androidx.room.*

interface FirstPaperPlaneDao {
    @Query("SELECT * FROM first_paper_planes")
    fun getAllFirstPlanes(): LiveData<List<PaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: PaperPlanes)

    @Update
    fun update(paperPlane: PaperPlanes)

    @Delete
    suspend fun delete(paperPlane: PaperPlanes)

    @Query("DELETE FROM first_paper_planes")
    suspend fun deleteAll()
}

interface RepliedPaperPlaneDao {
    @Query("SELECT * FROM replied_paper_planes")
    fun getAllRepliedPlanes(): LiveData<List<PaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: PaperPlanes)

    @Update
    fun update(paperPlane: PaperPlanes)

    @Delete
    suspend fun delete(paperPlane: PaperPlanes)

    @Query("DELETE FROM replied_paper_planes")
    suspend fun deleteAll()

}
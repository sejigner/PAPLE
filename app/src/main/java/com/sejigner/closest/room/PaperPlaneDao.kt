package com.sejigner.closest.room

import androidx.lifecycle.LiveData
import androidx.room.*
@Dao
interface FirstPaperPlaneDao {
    @Query("SELECT * FROM first_paper_planes")
    fun getAllFirstPlanes(): LiveData<List<FirstPaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: FirstPaperPlanes)

    @Update
    fun update(paperPlane: FirstPaperPlanes)

    @Delete
    suspend fun delete(paperPlane: FirstPaperPlanes)

    @Query("DELETE FROM first_paper_planes")
    suspend fun deleteAll()
}

interface RepliedPaperPlaneDao {
    @Query("SELECT * FROM replied_paper_planes")
    fun getAllRepliedPlanes(): LiveData<List<RepliedPaperPlanes>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paperPlane: RepliedPaperPlanes)

    @Update
    fun update(paperPlane: RepliedPaperPlanes)

    @Delete
    suspend fun delete(paperPlane: RepliedPaperPlanes)

    @Query("DELETE FROM replied_paper_planes")
    suspend fun deleteAll()
}
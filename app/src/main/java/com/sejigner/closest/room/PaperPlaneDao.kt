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
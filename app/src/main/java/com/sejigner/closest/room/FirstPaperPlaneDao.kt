package com.sejigner.closest.room

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

interface FirstPaperPlaneDao {
    @Query("SELECT * FROM firstPaperPlaneTable ORDER BY fromId DESC")
    fun getAll(): Flow<List<FirstPaperPlaneRD>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(firstPaperPlane: FirstPaperPlaneRD)

    @Update
    fun update(paperPlane: FirstPaperPlaneRD)

    @Delete
    fun delete(paperPlane: FirstPaperPlaneRD)

    @Query("DELETE FROM firstPaperPlaneTable")
    suspend fun deleteAll()
}
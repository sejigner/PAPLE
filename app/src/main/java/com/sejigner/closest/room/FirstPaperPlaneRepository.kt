package com.sejigner.closest.room

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class FirstPaperPlaneRepository(private val firstPaperPlaneDao: FirstPaperPlaneDao) {
    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allFirstPaperPlanes : Flow<List<FirstPaperPlaneRD>> = firstPaperPlaneDao.getAll()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(firstPaperPlaneRD: FirstPaperPlaneRD) {
        firstPaperPlaneDao.insert(firstPaperPlaneRD)
    }
}
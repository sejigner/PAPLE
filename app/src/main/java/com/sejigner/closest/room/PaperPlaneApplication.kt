package com.sejigner.closest.room

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PaperPlaneApplication : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val firstPaperPlaneDatabase by lazy { FirstPaperPlaneDatabase.getDatabase(this, applicationScope) }
    val firstPaperPlaneRepository by lazy { FirstPaperPlaneRepository(firstPaperPlaneDatabase.paperplaneDao()) }
}
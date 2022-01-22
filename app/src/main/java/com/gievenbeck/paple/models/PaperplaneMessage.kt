package com.sejigner.closest.models

import androidx.annotation.Keep

@Keep
data class PaperplaneMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val flightDistance: Double,
    val timestamp: Long,
    var isReplied : Boolean = false
) {
    constructor() : this("", "", "", "", 0.0, -1, false)
}

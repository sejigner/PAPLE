package com.sejigner.closest.models

data class PaperplaneMessage(
    val id: String,
    val text: String,
    val fromId: String,
    val toId: String,
    val flightDistance: Double,
    val timestamp: Long,
    var isReplied : Boolean = false
) {
    constructor() : this("", "", "", "", 0.0, 0L, false)
}

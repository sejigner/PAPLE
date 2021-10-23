package com.sejigner.closest.models

class ChatMessage(val id: String, val uid : String ,val message: String, val fromId : String, val toId : String, val timestamp: Long ) {
    constructor() : this("","","","","",-1)
}
package com.sejigner.closest.models

class LatestChatMessage(val recipientId: String, val message: String, val time : Long) {
    constructor() : this("","",-1)
}
package com.gievenbeck.paple.models

import androidx.annotation.Keep

@Keep
class LatestChatMessage(val recipientId: String, val message: String, val time : Long) {
    constructor() : this("","",-1)
}
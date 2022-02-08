package com.gievenbeck.paple.models

import androidx.annotation.Keep

@Keep
class ReportedChat(val reporterNickname: String, val reporterUid: String, val partnerNickname : String, val partnerUid : String, val date: String ) {
    constructor() : this("", "", "", "", "")
}
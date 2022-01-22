package com.gievenbeck.paple.models

import androidx.annotation.Keep

@Keep
class ReportMessage(val fromId: String, val message: String, val timestamp: Long ) {
    constructor() : this("","",-1)
}
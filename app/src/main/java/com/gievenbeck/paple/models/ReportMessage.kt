package com.gievenbeck.paple.models

import androidx.annotation.Keep

@Keep
class ReportMessage(val fromId: String, val message: String, val date: String ) {
    constructor() : this("","","")
}
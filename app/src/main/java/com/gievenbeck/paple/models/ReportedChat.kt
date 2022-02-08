package com.gievenbeck.paple.models

import androidx.annotation.Keep

@Keep
class ReportedChat(val reporter: String, val partner : String, val date: String ) {
    constructor() : this("","", "")
}
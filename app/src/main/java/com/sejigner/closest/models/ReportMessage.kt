package com.sejigner.closest.models

class ReportMessage(val uid: String, val message: String, val timestamp: Long ) {
    constructor() : this("","",-1)
}
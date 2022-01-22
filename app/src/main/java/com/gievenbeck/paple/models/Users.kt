package com.gievenbeck.paple.models

import androidx.annotation.Keep

@Keep
data class Users(
    var nickname: String? = null,
    var gender : String? = null,
    var birthYear : String? = null,
    var status : String? = null,
    var registrationToken : String? = null
    ) {
    constructor() : this("", "", "", "active", "")
}

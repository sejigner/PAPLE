package com.sejigner.closest.models

import android.graphics.Point
import android.location.Location
import android.provider.ContactsContract
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

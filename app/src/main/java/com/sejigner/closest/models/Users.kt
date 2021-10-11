package com.sejigner.closest.models

import android.graphics.Point
import android.location.Location
import android.provider.ContactsContract

data class Users(
        var strNickname: String? = null,
        var gender : String? = null,
        var birthYear : String? = null,
        var fcmToken : String? = null
    )

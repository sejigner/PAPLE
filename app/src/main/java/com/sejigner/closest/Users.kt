package com.sejigner.closest

import android.graphics.Point
import android.location.Location
import android.provider.ContactsContract

data class Users(
        var uid : String? = null,
        var userId : String? = null,
        var strNickname: String? = null,
        var latlng: Pair<Double,Double>? = null,
        var gender : String? = null,
        var birthYear : String? = null
    )

package com.sejigner.closest

import android.provider.ContactsContract

data class Users(
    var uid : String? = null,
    var userId : String? = null,
    var strNickname: String? = null,
    var latitude: String? = null,
    var longitude: String? = null,
    var gender : String? = null,
    var birthYear : String? = null
    )

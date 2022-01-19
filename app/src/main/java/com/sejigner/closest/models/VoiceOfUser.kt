package com.sejigner.closest.models

import androidx.annotation.Keep

@Keep
data class VoiceOfUser(
    var gender: String? = null,
    var birthYear : Int? = null,
    var content: String?= null
)

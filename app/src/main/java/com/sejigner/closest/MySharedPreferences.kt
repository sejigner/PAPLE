package com.sejigner.closest

import android.content.Context
import android.content.SharedPreferences

class MySharedPreferences(context : Context) {

    private val prefsFileName = "prefs"
    private val prefsKeyNickname = "myNickname"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFileName, 0)

    var myNickname: String?
    get() = prefs.getString(prefsKeyNickname, "")
    set(value) = prefs.edit().putString(prefsKeyNickname, value).apply()
}
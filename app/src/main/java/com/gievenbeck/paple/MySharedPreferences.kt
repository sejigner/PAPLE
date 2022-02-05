package com.gievenbeck.paple

import android.content.Context
import android.content.SharedPreferences
import android.location.Location

class MySharedPreferences(context: Context) {

    private val prefsFileName = "prefs"
    private val prefsKeyNickname = "myNickname"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsFileName, 0)

    var myNickname: String?
        get() = prefs.getString(prefsKeyNickname, "")
        set(value) = prefs.edit().putString(prefsKeyNickname, value).apply()

    fun getString(key: String, defValue: String): String {
        return prefs.getString(key, defValue).toString()
    }

    fun setString(key: String, str: String) {
        prefs.edit().putString(key, str).apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    fun setBoolean(key: String, boolean: Boolean) {
        prefs.edit().putBoolean(key, boolean).apply()
    }
}
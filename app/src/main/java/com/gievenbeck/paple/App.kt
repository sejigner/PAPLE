package com.gievenbeck.paple

import android.app.Application

// 테스트용 광고 단위 ID
const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
private const val LOG_TAG = "AppOpenAdManager"

class App : Application() {

    companion object {
        lateinit var prefs: MySharedPreferences
    }


    override fun onCreate() {
        prefs = MySharedPreferences(applicationContext)
        super.onCreate()
    }
}
package com.gievenbeck.paple

import android.app.Application
import android.os.Build
import java.util.*

// 테스트용 광고 단위 ID ca-app-pub-3940256099942544/1033173712
const val AD_UNIT_ID = "ca-app-pub-5118743253590971/6381726931"
private const val LOG_TAG = "AppOpenAdManager"

class App : Application() {
    companion object {
        lateinit var prefs: MySharedPreferences
    }


    override fun onCreate() {
        super.onCreate()
        prefs = MySharedPreferences(applicationContext)
        if(prefs.getString("countryCode","").isEmpty()) {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resources.configuration.locales.get(0)
            } else {
                resources.configuration.locale
            }
            prefs.setString("countryCode",locale.country)
        }
    }
}
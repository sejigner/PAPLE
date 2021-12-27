package com.sejigner.closest

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.*

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
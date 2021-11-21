package com.sejigner.closest

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.activity_splash_congrats.*

class SplashCongratsActivity : AppCompatActivity() {

    lateinit var timerTask : CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_congrats)

        this.supportActionBar?.hide()
        transparentStatusAndNavigation()
        val splashAnimation : Animation = AnimationUtils.loadAnimation(applicationContext,R.anim.anim_splash)
        val splashAnimationMove : Animation = AnimationUtils.loadAnimation(applicationContext,R.anim.anim_splash_move)

        tv_second_splash.text = ""

        timerTask = object : CountDownTimer(4000,1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_second_splash.text = (millisUntilFinished/1000.0).toInt().toString()
                tv_second_splash.startAnimation(splashAnimation)
                iv_dot_splash.startAnimation(splashAnimationMove)
            }

            override fun onFinish() {
                val setupIntent = Intent(this@SplashCongratsActivity, MainActivity::class.java)
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(setupIntent)
            }
        }.start()

    }

    override fun onStop() {
        super.onStop()
        val setupIntent = Intent(this@SplashCongratsActivity, MainActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(setupIntent)
    }

    private fun Activity.transparentStatusAndNavigation(
        systemUiScrim: Int = Color.parseColor("#40000000") // 25% black
    ) {
        var systemUiVisibility = 0
        // Use a dark scrim by default since light status is API 23+
        var statusBarColor = systemUiScrim
        //  Use a dark scrim by default since light nav bar is API 27+
        var navigationBarColor = systemUiScrim
        val winParams = window.attributes


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            navigationBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.decorView.systemUiVisibility = systemUiVisibility
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags = winParams.flags or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            winParams.flags = winParams.flags and
                    (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION).inv()
            window.statusBarColor = statusBarColor
            window.navigationBarColor = navigationBarColor
        }

        window.attributes = winParams
    }
}
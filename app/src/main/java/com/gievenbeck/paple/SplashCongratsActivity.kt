package com.gievenbeck.paple

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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

        val ivDecoUp = findViewById<ImageView>(R.id.iv_deco_up)
        val ivDecoDown = findViewById<ImageView>(R.id.iv_deco_down)

        Glide.with(this).load(R.drawable.bg_deco_up).into(ivDecoUp)
        Glide.with(this).load(R.drawable.bg_deco_down).into(ivDecoDown)

        tv_second_splash.text = ""

        timerTask = object : CountDownTimer(4000,1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_second_splash.text = (millisUntilFinished/1000.0).toInt().toString()
                tv_second_splash.startAnimation(splashAnimation)
                iv_dot_splash.startAnimation(splashAnimationMove)
            }

            override fun onFinish() {
                val setupIntent = Intent(this@SplashCongratsActivity, GuideActivity::class.java)
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(setupIntent)
            }
        }.start()

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
        window.attributes = winParams
    }
}
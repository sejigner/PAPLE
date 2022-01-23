package com.gievenbeck.paple

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.gievenbeck.paple.App.Companion.prefs
import com.gievenbeck.paple.fragment.SuspendAlertDialogFragment
import com.gievenbeck.paple.ui.FragmentChatViewModel


private const val TAG = "SplashActivity"


class SplashActivity : AppCompatActivity(), SuspendAlertDialogFragment.OnConfirmedListener {

    private lateinit var fbDatabase: FirebaseDatabase
    lateinit var viewModel: FragmentChatViewModel
    private val time: Long = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        this.supportActionBar?.hide()

        fbDatabase = FirebaseDatabase.getInstance()
        transparentStatusAndNavigation()

        createTimer(time)
    }

    private fun createTimer(seconds: Long) {

        val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                verifyUserIsLoggedIn()
            }
        }
        countDownTimer.start()
    }

    private fun confirmSuspend() {
        val alertDialog = SuspendAlertDialogFragment.newInstance(
            "규정 위반으로 사용 정지된 계정입니다.", "종료하기"
        )
        val fm = supportFragmentManager
        alertDialog.show(fm, "suspend-confirmation")
    }

    private fun verifyUserIsLoggedIn() {
        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid
        if (user != null) {
            val reference = fbDatabase.reference.child("Users").child(uid!!).child("status")
            reference.get()
                .addOnSuccessListener { it ->
                    if (it.value != null) {
                        if (it.value == "suspended") {
                            confirmSuspend()
                        } else {
                            Log.d(
                                "SplashActivity",
                                "Checked, User Info already set - user's status : ${it.value}"
                            )
                            val intent = Intent(this@SplashActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("IS_AD", true)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        startInitialSetting()
                    }
                }.addOnFailureListener {
                    Log.e("SplashActivity", it.message.toString())
                    startMainActivity()
                }
        } else {
                prefs.myNickname = ""
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("IS_AD", true)
        startActivity(intent)
        finish()
    }

    private fun startInitialSetting() {
        val setupIntent =
            Intent(this@SplashActivity, InitialSetupActivity::class.java)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(setupIntent)
        finish()
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


    override fun finishApp() {
        finish()
    }
}
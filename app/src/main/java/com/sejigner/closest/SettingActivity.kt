package com.sejigner.closest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.sejigner.closest.App.Companion.prefs
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        tv_sign_out_setting.setOnClickListener {
            val intent = Intent(this@SettingActivity, SignOutActivity::class.java)
            startActivity(intent)
        }

        sb_toggle_notification.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                prefs.setBoolean("Notification",true)
            } else {
                prefs.setBoolean("Notification",false)
            }
        }
    }
}
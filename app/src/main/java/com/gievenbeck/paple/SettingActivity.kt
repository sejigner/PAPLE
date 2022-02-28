package com.gievenbeck.paple

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.gievenbeck.paple.App.Companion.prefs
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.room.PaperPlaneDatabase
import com.gievenbeck.paple.room.PaperPlaneRepository
import com.gievenbeck.paple.room.User
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    var birthYear = 0
    lateinit var gender : String
    lateinit var viewModel : FragmentChatViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)

        viewModel =
            ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]

        val nickname = intent.getStringExtra("nickname")
        CoroutineScope(IO).launch {
            val userInfo : User = viewModel.getUser(UID).await()
            birthYear = userInfo.birthYear
            gender = userInfo.gender
        }

        val auth = FirebaseAuth.getInstance()



        iv_back_setting_activity.setOnClickListener {
            finish()
        }

        sb_toggle_subscription.isChecked = prefs.getBoolean("isDailyTopic",true)

        sb_toggle_subscription.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                prefs.setBoolean("isDailyTopic", true)
            } else {
                prefs.setBoolean("isDailyTopic", false)

            }
        }

        tv_sign_out_setting.setOnClickListener {
                auth.signOut()
                val intent = Intent(this@SettingActivity, SignOutActivity::class.java)
                intent.putExtra("nickname", nickname)
                intent.putExtra("birthYear", birthYear)
                intent.putExtra("gender", gender)
                startActivity(intent)
        }

        sb_toggle_notification.isChecked = prefs.getBoolean("isNotification", true)
        sb_toggle_notification.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                subscribeToDailyTopic()
            } else {
                unsubscribeFromDailyTopic()
            }
        }
    }
    private fun subscribeToDailyTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("dailyTopic")
            .addOnCompleteListener { task ->
                prefs.setBoolean("isDailyTopic",true)
                Toast.makeText(this,"데일리 토픽을 수신합니다.",Toast.LENGTH_SHORT).show()
            }
    }
    private fun unsubscribeFromDailyTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("dailyTopic")
            .addOnCompleteListener { task ->
                prefs.setBoolean("isDailyTopic",false)
                Toast.makeText(this,"데일리 토픽을 수신하지 않습니다.",Toast.LENGTH_SHORT).show()
            }
    }
}
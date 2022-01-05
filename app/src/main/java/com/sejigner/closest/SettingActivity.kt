package com.sejigner.closest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.sejigner.closest.App.Companion.prefs
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.room.User
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
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



        iv_back_setting_activity.setOnClickListener {
            finish()
        }

        tv_sign_out_setting.setOnClickListener {
            val intent = Intent(this@SettingActivity, SignOutActivity::class.java)
            intent.putExtra("nickname", nickname)
            intent.putExtra("birthYear", birthYear)
            intent.putExtra("gender", gender)
            startActivity(intent)
        }

        sb_toggle_notification.isChecked = prefs.getBoolean("notification", true)
        sb_toggle_notification.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) {
                prefs.setBoolean("notification",true)
            } else {
                prefs.setBoolean("notification",false)
            }
        }
    }
}
package com.gievenbeck.paple

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.gievenbeck.paple.App.Companion.prefs
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.fragment.FragmentHome
import com.gievenbeck.paple.room.PaperPlaneDatabase
import com.gievenbeck.paple.room.PaperPlaneRepository
import com.gievenbeck.paple.room.User
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
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

        Log.d(FragmentHome.TAG, UID)
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

        tv_sign_out_setting.setOnClickListener {
                auth.signOut()
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
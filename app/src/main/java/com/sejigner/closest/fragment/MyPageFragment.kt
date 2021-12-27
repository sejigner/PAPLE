package com.sejigner.closest.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_my_page.*

class MyPageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val nickname = App.prefs.myNickname!!
        tv_nickname_my_page.text = nickname
        tv_log_out_my_page.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Firebase 내 토큰 제거
            val fbDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(
                MainActivity.UID
            ).child("registrationToken")
            fbDatabase.removeValue()
            // SharedPreference 닉네임 값 제거
            App.prefs.myNickname = ""
            // 로그인 페이지 이동
            val intent = Intent(this@MyPageFragment.context, NewSignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        tv_setting_my_page.setOnClickListener {
            val intent = Intent(this@MyPageFragment.context, SettingActivity::class.java)
            intent.putExtra("nickname", nickname)
            startActivity(intent)
        }

        tv_guide_my_page.setOnClickListener {
            val intent = Intent(this@MyPageFragment.context, GuideActivity::class.java)
            startActivity(intent)
        }
    }
}
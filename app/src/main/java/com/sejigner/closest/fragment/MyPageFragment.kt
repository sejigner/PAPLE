package com.sejigner.closest.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.App
import com.sejigner.closest.MainActivity
import com.sejigner.closest.NewSignInActivity
import com.sejigner.closest.R
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
        tv_nickname_my_page.text = App.prefs.myNickname!!
        tv_sign_out_my_page.setOnClickListener {
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

    }
}
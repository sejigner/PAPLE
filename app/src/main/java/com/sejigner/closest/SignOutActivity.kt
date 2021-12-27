package com.sejigner.closest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_out.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.coroutineContext

class SignOutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_out)

        val nickname = intent.getStringExtra("nickname")
        tv_content_sign_out_activity.text = nickname

        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid

        // 유저 위치정보 제거
        val userLocationRef = FirebaseDatabase.getInstance().reference.child("User-Location/$uid")
        userLocationRef.removeValue()

        // 유저 개인정보 제거
        val userInfoRef = FirebaseDatabase.getInstance().reference.child("Users/$uid")
        userInfoRef.removeValue()

        val acquaintanceRef = FirebaseDatabase.getInstance().reference.child("Acquaintances/$uid")
        acquaintanceRef.removeValue()

        user?.delete()?.addOnCompleteListener {
            if(it.isSuccessful) {
                CoroutineScope(Main).launch {
                    Toast.makeText(this@SignOutActivity,"탈퇴되었습니다",Toast.LENGTH_SHORT).show()
                    // 2초 후 로그인 화면으로 이동
                    delay(2000L)
                    val intent = Intent(this@SignOutActivity, NewSignInActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }
        }


    }
}
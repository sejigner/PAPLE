package com.sejigner.closest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.sejigner.closest.models.VoiceOfUser
import kotlinx.android.synthetic.main.activity_sign_out.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlin.coroutines.coroutineContext

class SignOutActivity : AppCompatActivity() {

    lateinit var user : FirebaseUser
    var uid : String ?= ""
    var birthYear = 0
    var gender : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_out)

        val nickname = intent.getStringExtra("nickname")
        birthYear = intent.getIntExtra("birthYear", 0)
        gender = intent.getStringExtra("gender").toString()
        tv_content_sign_out_activity.text = nickname

        if(FirebaseAuth.getInstance().currentUser != null) {
            user = FirebaseAuth.getInstance().currentUser!!
            uid = user.uid
        }

        iv_back_sign_out.setOnClickListener{
            finish()
        }

        tv_sign_out.setOnClickListener {
            if(!et_reason_sign_out.text.isNullOrBlank()) {
                val vou = VoiceOfUser(gender, birthYear, et_reason_sign_out.text.toString())
                submitReason(vou)
            } else {
                signOut()
            }
        }

        tv_sign_out.setBackgroundColor(ContextCompat.getColor(this@SignOutActivity, R.color.paperplane_theme))

    }

    private fun signOut() {
        user.delete().addOnCompleteListener {
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

        // 유저 위치정보 제거
        val userLocationRef = FirebaseDatabase.getInstance().reference.child("User-Location/$uid")
        userLocationRef.removeValue()

        // 유저 개인정보 제거
        val userInfoRef = FirebaseDatabase.getInstance().reference.child("Users/$uid")
        userInfoRef.removeValue()

        val acquaintanceRef = FirebaseDatabase.getInstance().reference.child("Acquaintances/$uid")
        acquaintanceRef.removeValue()

    }

    private fun submitReason(vou : VoiceOfUser) {
        val ref = FirebaseDatabase.getInstance().reference.child("VOU").push()
        ref.setValue(vou).addOnCompleteListener {
            signOut()
        }.addOnFailureListener {
            signOut()
        }
    }
}
package com.sejigner.closest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.models.VoiceOfUser
import kotlinx.android.synthetic.main.activity_sign_out.*
import kotlinx.coroutines.*

class SignOutActivity : AppCompatActivity() {

    lateinit var user: FirebaseUser
    var uid: String? = ""
    var birthYear = 0
    var gender: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_out)

        val nickname = intent.getStringExtra("nickname")
        birthYear = intent.getIntExtra("birthYear", 0)
        gender = intent.getStringExtra("gender").toString()
        tv_content_sign_out_activity.text = nickname

        if (FirebaseAuth.getInstance().currentUser != null) {
            user = FirebaseAuth.getInstance().currentUser!!
            uid = user.uid
        }

        iv_back_sign_out.setOnClickListener {
            finish()
        }

        tv_sign_out.setOnClickListener {
            if (!et_reason_sign_out.text.isNullOrBlank()) {
                val vou = VoiceOfUser(gender, birthYear, et_reason_sign_out.text.toString())
                submitReason(vou)
            } else {
                signOut()
            }
        }

        tv_cancel_sign_out.setOnClickListener {
            finish()
        }

    }

    private fun signOut() {
        user.delete().addOnCompleteListener {
            if (it.isSuccessful) {
                val intent = Intent(this@SignOutActivity, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this@SignOutActivity, "탈퇴되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("SignOutActivity","탈퇴 처리 에러 uid : $UID")
                Toast.makeText(this@SignOutActivity, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 유저 위치정보 제거
        val userLocationRef = FirebaseDatabase.getInstance().reference.child("User-Location/$uid")
        userLocationRef.removeValue()

        // 유저 개인정보 제거
        val userInfoRef = FirebaseDatabase.getInstance().reference.child("Users/$uid")
        userInfoRef.removeValue()

    }

    private fun submitReason(vou: VoiceOfUser) {
        val ref = FirebaseDatabase.getInstance().reference.child("VOU").push()
        ref.setValue(vou).addOnCompleteListener {
            signOut()
        }.addOnFailureListener {
            signOut()
        }
    }
}
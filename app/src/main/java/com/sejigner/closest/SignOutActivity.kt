package com.sejigner.closest

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
    var isOnline = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_out)

        val nickname = intent.getStringExtra("nickname")
        birthYear = intent.getIntExtra("birthYear", 0)
        gender = intent.getStringExtra("gender").toString()
        tv_content_sign_out_activity.text = getString(R.string.content_sign_out, nickname)

        if (FirebaseAuth.getInstance().currentUser != null) {
            user = FirebaseAuth.getInstance().currentUser!!
            uid = user.uid
        }

        iv_back_sign_out.setOnClickListener {
            finish()
        }

        tv_sign_out.setOnClickListener {
            if (isOnline) {
                if (!et_reason_sign_out.text.isNullOrBlank()) {
                    val vou = VoiceOfUser(gender, birthYear, et_reason_sign_out.text.toString())
                    submitReason(vou)
                } else {
                    signOut()
                }
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
            }
        }

        tv_cancel_sign_out.setOnClickListener {
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        registerNetworkCallback()
    }

    override fun onStop() {
        super.onStop()
        terminateNetworkCallback()
    }

    private fun signOut() {
        user.delete().addOnSuccessListener {
            val intent = Intent(this@SignOutActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            Toast.makeText(this@SignOutActivity, "탈퇴되었습니다", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Log.e("SignOutActivity","탈퇴 처리 에러 uid : $UID")
            Toast.makeText(this@SignOutActivity, "다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }

        // 유저 위치정보 제거
        val userLocationRef = FirebaseDatabase.getInstance().reference.child("User-Location/$uid")
        userLocationRef.removeValue()

        // 유저 개인정보 제거
        val userInfoRef = FirebaseDatabase.getInstance().reference.child("Users/$uid")
        userInfoRef.removeValue()

    }

    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isOnline = true
        }

        override fun onLost(network: Network) {
            isOnline = false
        }
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallBack)
    }

    private fun terminateNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.unregisterNetworkCallback(networkCallBack)
    }

    private fun submitReason(vou: VoiceOfUser) {
        val ref = FirebaseDatabase.getInstance().reference.child("VOU").push()
        ref.setValue(vou).addOnSuccessListener {
            Log.d("SignOutActivity", "Submitted the VOU to the server")
            signOut()
        }.addOnFailureListener {
            Log.d("SignOutActivity", "fail to submit the VOU to the server")
            signOut()
        }
    }
}
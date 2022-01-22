package com.gievenbeck.paple

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.models.VoiceOfUser
import kotlinx.android.synthetic.main.activity_initial_setup.*
import kotlinx.android.synthetic.main.activity_sign_out.*
import kotlinx.coroutines.*


class SignOutActivity : AppCompatActivity() {

    var uid: String? = ""
    var birthYear = 0
    var gender: String = ""
    var isOnline = false
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_out)

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        val nickname = intent.getStringExtra("nickname")
        birthYear = intent.getIntExtra("birthYear", 0)
        gender = intent.getStringExtra("gender").toString()
        tv_content_sign_out_activity.text = getString(R.string.content_sign_out, nickname)

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
        val reference = firebaseDatabase.reference
        reference.child("Users/$UID").removeValue()
        reference.child("User-Location").child(UID).setValue(null).addOnSuccessListener {
            Toast.makeText(this@SignOutActivity, R.string.success_sign_out, Toast.LENGTH_SHORT)
                .show()
            val intent = Intent(this@SignOutActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
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
package com.gievenbeck.paple

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.gievenbeck.paple.ui.LoadingDialog
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit


class SignInActivity : AppCompatActivity() {

    var phoneNumber: String = ""
    lateinit var auth: FirebaseAuth

    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var loadingDialog: LoadingDialog
    private var isOnline = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("kr")
        cl_request_otp.isEnabled = false
        loadingDialog = LoadingDialog(this@SignInActivity)


        // start verification on click of the button
        cl_request_otp.setOnClickListener {
            if (isOnline) {
                signIn()
                showLoadingDialog()
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
            }
        }

        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
                Log.d("@MainActivity", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("@MainActivity", "onVerificationFailed $e")
                Toast.makeText(this@SignInActivity, "인증 실패 - 번호를 확인해주세요.", Toast.LENGTH_SHORT)
                    .show()
                dismissLoadingDialog()
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("@MainActivity", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
                val intent = Intent(applicationContext, OtpActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                intent.putExtra("phoneNumber", phoneNumber)
                dismissLoadingDialog()
                startActivity(intent)
                finish()
            }
        }

        et_phone_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim { it <= ' ' }.isEmpty()) {
                    cl_request_otp.isEnabled = false
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim { it <= ' ' }.isNotEmpty()) {
                    cl_request_otp.isEnabled = true
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        registerNetworkCallback()
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

    override fun onStop() {
        super.onStop()
        terminateNetworkCallback()
    }

    private fun terminateNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.unregisterNetworkCallback(networkCallBack)
    }

    private fun showLoadingDialog() {
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    private fun signIn() {
        phoneNumber = findViewById<EditText>(R.id.et_phone_number).text.trim().toString()

        // get the phone number from edit text and append the country cde with it
        if (phoneNumber.isNotEmpty()) {
            phoneNumber = "+82$phoneNumber"
            sendVerificationCode(phoneNumber)
            Log.d("Verification", "Number inserted $phoneNumber")
        } else {
            Toast.makeText(this, "Enter mobile number", Toast.LENGTH_SHORT).show()
        }
    }

    // this method sends the verification code
    // and starts the callback of verification
    // which is implemented above in onCreate
    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("@OtpActivity", "Auth started")
    }
}
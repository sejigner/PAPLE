package com.sejigner.closest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_sign_in.*
import java.util.concurrent.TimeUnit


class NewSignInActivity : AppCompatActivity() {

    var phoneNumber : String =""
    lateinit var  auth : FirebaseAuth

    lateinit var storedVerificationId : String
    lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks : PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_sign_in)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("kr")
        cl_request_otp.isEnabled = false

        // start verification on click of the button
        cl_request_otp.setOnClickListener {
            login()
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
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d("@MainActivity","onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token

                // Start a new activity using intent
                // also send the storedVerificationId using intent
                // we will use this id to send the otp back to firebase
                val intent = Intent(applicationContext, OtpActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                intent.putExtra("phoneNumber", phoneNumber)
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

    private fun login() {
        phoneNumber = findViewById<EditText>(R.id.et_phone_number).text.trim().toString()

        // get the phone number from edit text and append the country cde with it
        if (phoneNumber.isNotEmpty()) {
            phoneNumber = "+82$phoneNumber"
            sendVerificationCode(phoneNumber)
        } else{
            Toast.makeText(this, "Enter mobile number", Toast.LENGTH_SHORT).show()
        }
    }

    // this method sends the verification code
    // and starts the callback of verification
    // which is implemented above in onCreate
    private fun sendVerificationCode(phoneNumber : String) {
        val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks)
                .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        Log.d("@OtpActivity","Auth started")
    }
}
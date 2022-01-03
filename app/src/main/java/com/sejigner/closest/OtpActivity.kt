package com.sejigner.closest

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.ui.LoadingDialog
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_sign_in.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask

class OtpActivity : AppCompatActivity() {

    // get reference of the firebase auth
    lateinit var auth: FirebaseAuth
    lateinit var fbDatabase: FirebaseDatabase
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var timerTask: CountDownTimer
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("kr")
        fbDatabase = FirebaseDatabase.getInstance()
        cl_otp_check.isEnabled = false
        tv_request_resend.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        loadingDialog = LoadingDialog(this@OtpActivity)

        et_otp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim { it <= ' ' }.isEmpty()) {
                    cl_otp_check.isEnabled = false
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim { it <= ' ' }.isNotEmpty()) {
                    cl_otp_check.isEnabled = true
                }
            }
        })

        // get storedVerificationId from the intent
        var storedVerificationId = intent.getStringExtra("storedVerificationId")
        val phoneNumber = intent.getStringExtra("phoneNumber")

        // fill otp and call the on click on button
        cl_otp_check.setOnClickListener {
            val otp = findViewById<EditText>(R.id.et_otp).text.trim().toString()
            if (otp.isNotEmpty()) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), otp
                )
                signInWithPhoneAuthCredential(credential)
                showLoadingDialog()
            } else {
                Toast.makeText(this, "인증 번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        timerTask = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tv_time_left.text =
                    getString(R.string.otp_time, millisUntilFinished / 1000.0f.toInt())
            }

            override fun onFinish() {
                tv_request_resend.isEnabled = true
                tv_time_left.text = getString(R.string.otp_failure)
            }
        }

        startTimer()

        tv_request_resend.setOnClickListener {
            startTimer()
            sendVerificationCode(phoneNumber!!)
            showLoadingDialog()
        }

        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            // This method is called when the verification is completed
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                dismissLoadingDialog()
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
                Log.d("@OtpActivity", "onVerificationCompleted Success")
            }

            // Called when verification is failed add log statement to see the exception
            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("@OtpActivity", "onVerificationFailed $e")
                dismissLoadingDialog()
                Toast.makeText(this@OtpActivity, "인증 실패 - 번호를 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
            }

            // On code is sent by the firebase this method is called
            // in here we start a new activity where user can enter the OTP
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("@OtpActivity", "onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token
                dismissLoadingDialog()
//                // Start a new activity using intent
//                // also send the storedVerificationId using intent
//                // we will use this id to send the otp back to firebase
//                val intent = Intent(applicationContext, OtpActivity::class.java)
//                intent.putExtra("storedVerificationId", storedVerificationId)
//                startActivity(intent)
//                finish()
            }
        }
    }

    private fun showLoadingDialog() {
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

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

    private fun startTimer() {
        tv_time_left.text = getString(R.string.otp_time, 60)
        tv_request_resend.isEnabled = false
        timerTask.start()
    }

    // verifies if the code matches sent by firebase
    // if success start the main activity
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                    val reference =
                        fbDatabase.reference.child("Users").child(uid!!).child("nickname")
                    reference.get()
                        .addOnSuccessListener { it ->
                            if (it.value != null) {
                                Log.d(
                                    MainActivity.TAG,
                                    "Checked, User Info already set - user nickname : ${it.value}"
                                )
                                startActivity(Intent(applicationContext, MainActivity::class.java))
                                finish()
                            } else {
                                val setupIntent =
                                    Intent(this@OtpActivity, InitialSetupActivity::class.java)
                                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(setupIntent)
                                finish()
                            }
                        }
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "인증 실패 - 받으신 인증 코드를 확인해주세요.", Toast.LENGTH_SHORT).show()
                        dismissLoadingDialog()
                    }
                }
            }
    }
}
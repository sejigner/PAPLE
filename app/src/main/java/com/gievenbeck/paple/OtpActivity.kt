package com.gievenbeck.paple

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gievenbeck.paple.App.Companion.countryCode
import com.gievenbeck.paple.InitialSetupActivity.Companion.TEST_UID
import com.gievenbeck.paple.fragment.SuspendAlertDialogFragment
import com.gievenbeck.paple.ui.LoadingDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class OtpActivity : AppCompatActivity(), SuspendAlertDialogFragment.OnConfirmedListener {

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
        tv_otp_confirm.isEnabled = false
        tv_request_resend.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        loadingDialog = LoadingDialog(this@OtpActivity)

        iv_back_otp.setOnClickListener {
            finish()
        }

        et_otp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim { it <= ' ' }.isEmpty()) {
                    tv_otp_confirm.isEnabled = false
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim { it <= ' ' }.isNotEmpty()) {
                    tv_otp_confirm.isEnabled = true
                }
            }
        })

        // get storedVerificationId from the intent
        var storedVerificationId = intent.getStringExtra("storedVerificationId")
        val phoneNumber = intent.getStringExtra("phoneNumber")

        tv_phone_number.text = PhoneNumberUtils.formatNumber(phoneNumber, "KR")

            // fill otp and call the on click on button
        tv_otp_confirm.setOnClickListener {
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
                tv_time_left.text = (millisUntilFinished / 1000.0f.toInt()).toString()
            }

            override fun onFinish() {
                tv_request_resend.isEnabled = true
                tv_time_left.text = "만료"
            }
        }

        startTimer()

        tv_request_resend.setOnClickListener {
            //핸드폰번호 유효성
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
                Toast.makeText(this@OtpActivity, "인증 실패 - 번호를 다시 입력해주세요.", Toast.LENGTH_SHORT)
                    .show()
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun showLoadingDialog() {
        loadingDialog.show()
    }

    private fun dismissLoadingDialog() {
        loadingDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoadingDialog()
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
        tv_time_left.text = "60"
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
                    if (uid != TEST_UID) {
                        val reference =
                            fbDatabase.reference.child("Users").child(countryCode).child(uid!!)
                                .child("status")
                        reference.get()
                            .addOnSuccessListener { it ->
                                if (it.value != null) {
                                    if (it.value == "suspended") {
                                        confirmSuspend()
                                    } else {
                                        Log.d(
                                            MainActivity.TAG,
                                            "Checked, User Info already set - user's status : ${it.value}"
                                        )
                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
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
                        val reference =
                        fbDatabase.reference.child("Users").child("test").child(uid!!)
                            .child("status")
                        reference.get()
                            .addOnSuccessListener { it ->
                                if (it.value != null) {
                                    if (it.value == "suspended") {
                                        confirmSuspend()
                                    } else {
                                        Log.d(
                                            MainActivity.TAG,
                                            "Checked, User Info already set - user's status : ${it.value}"
                                        )
                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                } else {
                                    val setupIntent =
                                        Intent(this@OtpActivity, InitialSetupActivity::class.java)
                                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(setupIntent)
                                    finish()
                                }
                            }

                    }

                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, "인증 실패 - 받으신 인증 코드를 확인해주세요.", Toast.LENGTH_SHORT)
                            .show()
                        dismissLoadingDialog()
                    }
                }
            }
    }

    private fun confirmSuspend() {
        val alertDialog = SuspendAlertDialogFragment.newInstance(
            "규정 위반으로 사용 정지된 계정입니다.", "종료하기"
        )
        val fm = supportFragmentManager
        alertDialog.show(fm, "suspend-confirmation")
    }


    override fun finishApp() {
        finish()
    }
}
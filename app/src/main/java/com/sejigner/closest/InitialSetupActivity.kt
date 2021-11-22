package com.sejigner.closest

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.sejigner.closest.fragment.FragmentHome
import com.sejigner.closest.models.Users
import kotlinx.android.synthetic.main.activity_initial_setup.*
import kotlinx.android.synthetic.main.activity_initial_setup.rb_female
import kotlinx.android.synthetic.main.activity_initial_setup.rb_male
import java.util.*
import android.R.attr.name
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.R.attr.name
import android.app.Service
import android.text.InputFilter
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import java.util.regex.Pattern


class InitialSetupActivity : AppCompatActivity() {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFireStore: FirebaseFirestore? = null
    private var fbDatabase: FirebaseDatabase? = null
    private var uid: String? = null
    private var lastTimePressed = 0L
    private val date: Calendar = Calendar.getInstance()
    private val year = date.get(Calendar.YEAR)
    private var userInfo = Users()
    lateinit var inputMethodManager : InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFireStore = FirebaseFirestore.getInstance()
        fbDatabase = FirebaseDatabase.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid
        initFcmToken()

        inputMethodManager =
            getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager

        rg_initial_gender.visibility = View.GONE
        numberPicker_birth_year_initial_setup.visibility = View.GONE

        cl_initial_setup.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                et_nickname.clearFocus()
                hideKeyboard()
                return false
            }

        })


        btn_birth_year.setOnClickListener {
            if (numberPicker_birth_year_initial_setup.visibility == View.GONE) {
                et_nickname.clearFocus()
                hideKeyboard()
                Log.d("Year", "$year")
                numberPicker_birth_year_initial_setup.minValue = date.get(Calendar.YEAR) - 80
                numberPicker_birth_year_initial_setup.maxValue = date.get(Calendar.YEAR) - 12
                numberPicker_birth_year_initial_setup.value = 1994
                Log.d(
                    "Year",
                    "${numberPicker_birth_year_initial_setup.minValue}~${numberPicker_birth_year_initial_setup.maxValue}"
                )

                rg_initial_gender.visibility = View.GONE
                numberPicker_birth_year_initial_setup.visibility = View.VISIBLE

                numberPicker_birth_year_initial_setup.setOnValueChangedListener { picker: NumberPicker, oldVal, newVal ->
                    Log.d("InitialSetupActivity", "oldVal : ${oldVal}, newVal : $newVal")
                    userInfo.birthYear = newVal.toString()
                    tv_initial_birth_year.text = newVal.toString()
                    Log.d("InitialSetupActivity", "User's birthday's been set as $newVal")

                }
            }
        }

        btn_gender.setOnClickListener {
            if (rg_initial_gender.visibility == View.GONE) {
                hideKeyboard()
                numberPicker_birth_year_initial_setup.visibility = View.GONE
                rg_initial_gender.visibility = View.VISIBLE

                rb_female.setOnClickListener {
                    userInfo.gender = "female"
                    tv_initial_gender.text = "여성"
                }

                rb_male.setOnClickListener {
                    userInfo.gender = "male"
                    tv_initial_gender.text = "남성"
                }
            }
        }

        // 닉네임 입력 감지 리스너
        et_nickname.addTextChangedListener(textWatcherNickname)

        et_nickname.setOnEditorActionListener{ textView, action, event ->
            var handled = false

            if (action == EditorInfo.IME_ACTION_DONE) {
                // 키보드 내리기
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(et_nickname.windowToken, 0)
                handled = true
            }

            handled
        }


        cl_initial_start.setOnClickListener {
            if ((userInfo.nickname.isNullOrEmpty() || userInfo.birthYear == null || userInfo.gender == null))
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                // 개인정보 확인 다이얼로그 구현
                setInitialSetupToFireStore()

                val intent = Intent(this, SplashCongratsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

        et_nickname.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            val ps : Pattern = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣu318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$")
            if(source.equals("") || ps.matcher(source).matches()) {
                return@InputFilter source
            }
            Toast.makeText(this, "한글, 영문, 숫자만 사용해주세요!", Toast.LENGTH_SHORT).show()
            ""
        }, InputFilter.LengthFilter(10))





    }

    private val textWatcherNickname = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s != null && s.toString().isNotEmpty()){
                et_nickname.setGravity(Gravity.END)
            }else{
                et_nickname.setGravity(Gravity.START)
            }
        }

        override fun afterTextChanged(s: Editable?) {
            if (s != null && s.toString().isNotEmpty()){
                et_nickname.setGravity(Gravity.END)
                userInfo.nickname = s.toString()
            }else{
                et_nickname.setGravity(Gravity.START)
            }

        }
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(et_nickname.windowToken, 0)
    }

    private fun initFcmToken() {
        if (checkGooglePlayServices()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        MainActivity.TAG,
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@OnCompleteListener
                }

                val token = task.result

                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(MainActivity.TAG, msg)
            })
        } else {
            Log.w(MainActivity.TAG, "Device doesn't have google play services")
        }

    }

    private fun setInitialSetupToFireStore() {
        val database = fbDatabase?.reference
        database?.child("Users")?.child(uid!!)?.setValue(userInfo)?.addOnSuccessListener {
            Log.d(
                FragmentHome.TAG,
                "Saved Users info to Firebase Realtime database: ${database.key}"
            )
        }
        database?.child("Acquaintances/$uid")?.setValue(uid)
    }

    private fun checkGooglePlayServices(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(MainActivity.TAG, "Error")
            false
        } else {
            Log.i(MainActivity.TAG, "Google play services updated")
            true
        }
    }


    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastTimePressed < 2000) //short Toast duration, now should be faded out
            finish()
        else {
            Toast.makeText(this, "앱을 종료하시려면 뒤로가기 버튼을 두번 터치해주세요.", Toast.LENGTH_SHORT).show()
        }

        lastTimePressed = System.currentTimeMillis()
    }
}



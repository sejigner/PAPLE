package com.sejigner.closest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_initial_setup.*
import kotlinx.android.synthetic.main.activity_initial_setup.rb_female
import kotlinx.android.synthetic.main.activity_initial_setup.rb_male
import kotlinx.android.synthetic.main.activity_my_page.*
import java.util.*

class InitialSetupActivity : AppCompatActivity() {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFireStore: FirebaseFirestore? = null
    private var uid: String? = null
    private var lastTimePressed = 0L
    private var gender: String? = null
    private var birthYear: String? = null
    private var nickname : String? = null
    private val date: Calendar = Calendar.getInstance()
    private val year = date.get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFireStore = FirebaseFirestore.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid
        // 닉네임 입력 감지 리스너
        et_nickname.addTextChangedListener(textWatcherNickname)


        rb_female.setOnClickListener {
            gender = "female"
        }

        rb_male.setOnClickListener {
            gender = "male"
        }

        tv_done.setOnClickListener {
            if ((nickname == null || birthYear == null || gender == null))
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                // 개인정보 확인 다이얼로그 구현


                setInitialSetupToFireStore()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        // 유저의 생년 저장

        Log.d("Year", "$year")
        numberPicker_birth_year_my_page.minValue = date.get(Calendar.YEAR) - 80
        numberPicker_birth_year_my_page.maxValue = date.get(Calendar.YEAR) - 12
        numberPicker_birth_year_my_page.value = 1994
        Log.d(
            "Year",
            "${numberPicker_birth_year_my_page.minValue}~${numberPicker_birth_year_my_page.maxValue}"
        )

        numberPicker_birth_year_my_page.setOnValueChangedListener { picker: NumberPicker, oldVal, newVal ->
            Log.d("InitialSetupActivity", "oldVal : ${oldVal}, newVal : $newVal")
            val picked = picker.toString()
            birthYear = picked
            Log.d("MyPageActivity", "User's birthday's been set as $picked")

        }
    }

        private val textWatcherNickname = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                TODO("Not yet implemented")
            }

            override fun afterTextChanged(s: Editable?) {
                nickname = s.toString()
            }
        }

        // 파이어스토어 User 정보의 isInitialSetup 값을 True로 설정하여 최초 1회 설정만 가능하게 함
        private fun setInitialSetupToFireStore() {

            fbFireStore = FirebaseFirestore.getInstance()
            fbFireStore?.collection("users")?.document("$uid")
                ?.update(mapOf("isInitialSetup" to true))?.addOnSuccessListener(this,
                OnSuccessListener {
                    Log.d("isInitialSetup", "set data to Firestore")

                })
                ?.addOnFailureListener {
                    Log.d("isInitialSetup", "fail to set data to Firestore")
                }
        }


        override fun onBackPressed() {
            if (System.currentTimeMillis() - lastTimePressed > 2000) //short Toast duration, now should be faded out
                finish();
            else
                Toast.makeText(this, "앱을 종료하시려면 뒤로가기 버튼을 두번 터치해주세요.", Toast.LENGTH_SHORT).show()
            lastTimePressed = System.currentTimeMillis()
        }
    }

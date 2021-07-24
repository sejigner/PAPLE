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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.fragment.FragmentHome
import kotlinx.android.synthetic.main.activity_initial_setup.*
import kotlinx.android.synthetic.main.activity_initial_setup.rb_female
import kotlinx.android.synthetic.main.activity_initial_setup.rb_male
import kotlinx.android.synthetic.main.activity_my_page.*
import java.util.*

class InitialSetupActivity : AppCompatActivity() {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFireStore: FirebaseFirestore? = null
    private var fbDatabase : FirebaseDatabase? = null
    private var uid: String? = null
    private var lastTimePressed = 0L
    private val date: Calendar = Calendar.getInstance()
    private val year = date.get(Calendar.YEAR)
    private var userInfo = Users()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFireStore = FirebaseFirestore.getInstance()
        fbDatabase = FirebaseDatabase.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid
        // 닉네임 입력 감지 리스너
        et_nickname.addTextChangedListener(textWatcherNickname)


        rb_female.setOnClickListener {
            userInfo.gender = "female"
        }

        rb_male.setOnClickListener {
            userInfo.gender = "male"
        }

        tv_done.setOnClickListener {
            if ((userInfo.strNickname == null || userInfo.birthYear == null || userInfo.gender == null))
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
        numberPicker_birth_year_initial_setup.minValue = date.get(Calendar.YEAR) - 80
        numberPicker_birth_year_initial_setup.maxValue = date.get(Calendar.YEAR) - 12
        numberPicker_birth_year_initial_setup.value = 1994
        Log.d(
            "Year",
            "${numberPicker_birth_year_initial_setup.minValue}~${numberPicker_birth_year_initial_setup.maxValue}"
        )

        numberPicker_birth_year_initial_setup.setOnValueChangedListener { picker: NumberPicker, oldVal, newVal ->
            Log.d("InitialSetupActivity", "oldVal : ${oldVal}, newVal : $newVal")
            userInfo.birthYear = newVal.toString()
            Log.d("InitialSetupActivity", "User's birthday's been set as $newVal")

        }
    }

        private val textWatcherNickname = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                userInfo.strNickname = s.toString()
            }
        }
    
        private fun setInitialSetupToFireStore() {
           val database = fbDatabase?.reference
            database?.child("Users")?.child(uid!!)?.setValue(userInfo)?.addOnSuccessListener {
                Log.d(FragmentHome.TAG,"Saved Users info to Firebase Realtime database: ${database.key}")
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

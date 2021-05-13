package com.sejigner.closest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.android.synthetic.main.activity_my_page.view.*
import java.util.*


class MyPageActivity : AppCompatActivity() {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFirestore: FirebaseFirestore? = null
    private var uid: String? = null
    private var birthYear: String? = null
    private var gender: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFirestore = FirebaseFirestore.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid

        // 유저의 생년 저장
        val date = Calendar.getInstance()
        val year = date.get(Calendar.YEAR)
        Log.d("Year", "$year")
        numberPicker_birth_year.minValue = date.get(Calendar.YEAR) - 80
        numberPicker_birth_year.maxValue = date.get(Calendar.YEAR) - 12
        numberPicker_birth_year.value = 1994
        Log.d("Year", "${numberPicker_birth_year.minValue}~${numberPicker_birth_year.maxValue}")

        numberPicker_birth_year.setOnValueChangedListener { picker : NumberPicker, oldVal, newVal ->
            Log.d("MyPageActivity", "oldVal : ${oldVal}, newVal : $newVal")
            val picked = picker.toString()
            birthYear = picked
            Log.d("MyPageActivity", "User's birthday's been set as $picked")


            // 유저의 성별 저장
            rg_gender.setOnCheckedChangeListener(object  :
            RadioGroup.OnCheckedChangeListener{
                override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                    when (checkedId) {
                        R.id.rb_female -> gender = "female"
                        R.id.rb_male -> gender = "male"
                    }
                }
            })

            // 유저의 정보 저장
            bt_save.setOnClickListener {
                if (fireBaseUser != null) {
                    val birthYear: String = et_birth_year.text.toString()
                    val gender: String = et_gender.text.toString()
                    if (birthYear == null || gender == null) Toast.makeText(this@MyPageActivity, "정보를 입력하세요.", Toast.LENGTH_SHORT).show()
                    else {
                        setInfoOnFirestore(birthYear, gender)
                    }
                }
                }
        }
    }
    private fun setInfoOnFirestore(birthYear: String, gender: String) {
        fbFirestore?.collection("users")?.document("$uid")?.update(mapOf("birthYear" to birthYear, "gender" to gender))?.addOnSuccessListener(this,
                OnSuccessListener {
                    Log.d("MyPageActivity", "set users' infos on firestore successfully")
                    Toast.makeText(this, "정보를 저장했어요.", Toast.LENGTH_SHORT).show()
                })
                ?.addOnFailureListener {
                    Log.d("MyPageActivity", "fail to set infos on firestore")
                    Toast.makeText(this, "정보를 저장하지 못했어요.", Toast.LENGTH_SHORT).show()
                }
    }
}
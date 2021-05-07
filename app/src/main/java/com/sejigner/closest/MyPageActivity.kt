package com.sejigner.closest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_my_page.*


class MyPageActivity : AppCompatActivity() {
    private var fireBaseAuth : FirebaseAuth? = null
    private var fireBaseUser : FirebaseUser? = null
    private var fbFirestore : FirebaseFirestore? = null
    private var uid : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFirestore = FirebaseFirestore.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid


        bt_save.setOnClickListener{
            if(fireBaseUser != null) {
                var birthYear : String? = et_birth_year.text.toString()
                var gender : String? = et_gender.text.toString()
                if(birthYear == null || gender == null) Toast.makeText(this@MyPageActivity,"정보를 입력하세요.",Toast.LENGTH_SHORT).show()
                else {
                    setInfoOnFirestore(birthYear,gender)
                }
            }

        }
    }

    private fun setInfoOnFirestore(birthYear : String, gender : String) {
        fbFirestore?.collection("users")?.document("$uid")?.update(mapOf("birthYear" to birthYear, "gender" to gender))?.addOnSuccessListener(this,
            OnSuccessListener {
                Log.d("MyPageActivity","set users' infos on firestore successfully")
                Toast.makeText(this, "정보를 저장했어요.",Toast.LENGTH_SHORT).show()
            })
            ?.addOnFailureListener {
                Log.d("MyPageActivity", "fail to set infos on firestore")
                Toast.makeText(this, "정보를 저장하지 못했어요.",Toast.LENGTH_SHORT).show()
            }
    }
}
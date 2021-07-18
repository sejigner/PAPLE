package com.sejigner.closest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_initial_setup.*

class InitialSetupActivity : AppCompatActivity() {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFireStore: FirebaseFirestore? = null
    private var uid: String? = null
    private var lastTimePressed = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        var gender: String? = null
        var birthYear: String? = null
        var nickname : String? = null

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFireStore = FirebaseFirestore.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid

        rb_female.setOnClickListener {
            gender = "female"
        }

        rb_male.setOnClickListener {
            gender = "male"
        }

        tv_done.setOnClickListener {
            if((nickname==null||birthYear==null||gender==null))
                Toast.makeText(this,"모든 정보를 입력해주세요.",Toast.LENGTH_SHORT).show()
            else {
                // 개인정보 확인 다이얼로그 구현

                setTrueInitialSetup()
            }
        }


    }

    private fun setTrueInitialSetup() {

        fbFireStore = FirebaseFirestore.getInstance()
        fbFireStore?.collection("users")?.document("$uid")?.update(mapOf("isInitialSetup" to true))?.addOnSuccessListener(this,
            OnSuccessListener {
                Log.d("isInitialSetup","set data to Firestore")

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
}
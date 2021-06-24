package com.sejigner.closest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_initial_setting.*

class InitialSetting : AppCompatActivity() {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFireStore: FirebaseFirestore? = null
    private var uid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setting)

        var gender:String? = null
        var birthYear:String? = null

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
    }
}
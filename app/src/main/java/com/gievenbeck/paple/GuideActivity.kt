package com.gievenbeck.paple

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_guide.*

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        cl_guide_confirm.setOnClickListener {
            val setupIntent = Intent(this@GuideActivity, MainActivity::class.java)
            setupIntent.putExtra("IS_AD", false)
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(setupIntent)
        }

    }

    override fun onStart() {
        super.onStart()
        Glide.with(this)
            .load(R.drawable.bg_paple_illustration)
            .into(iv_paple_illustration)
    }
}
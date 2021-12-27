package com.sejigner.closest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_guide.*
import kotlinx.android.synthetic.main.activity_setting.*

class GuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guide)

        iv_back_guide.setOnClickListener {
            finish()
        }
    }
}
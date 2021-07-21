package com.sejigner.closest

import android.content.Intent
import android.location.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.fragment.FragmentChat
import com.sejigner.closest.fragment.FragmentHome
import com.sejigner.closest.fragment.FragmentMyPage
import com.sejigner.closest.fragment.MainViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var userName: String? = null
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFirestore: FirebaseFirestore? = null
    private val fragmentHome by lazy { FragmentHome() }
    private val fragmentChat by lazy { FragmentChat() }
    private val fragmentMyPage by lazy { FragmentMyPage() }

    private val fragments: List<Fragment> = listOf(fragmentHome, fragmentChat, fragmentMyPage)

    private val pagerAdapter: MainViewPagerAdapter by lazy { MainViewPagerAdapter(this, fragments) }



    companion object {
        const val TAG = "MainActivity"
        const val ANONYMOUS = "anonymous"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewPager()
        initNavigationBar()


        userName = ANONYMOUS

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser

        if (fireBaseUser == null) {
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        } else {
            userName = fireBaseUser!!.displayName
        }

        fireBaseAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "got instance from Firestore successfully")

        val uid = fireBaseAuth?.uid
        // fbFirestore?.collection("users")?.document(fireBaseAuth?.uid.toString())?.set(userInfo)

        // 파이어스토어에 저장된 정보 유무를 통해 개인정보 초기설정 실행 여부 판단
        val docRef = fbFirestore?.collection("users")?.document("$uid")
        docRef?.get()?.addOnSuccessListener { document ->
            if (document != null) {
                val isUserNickName = document.get("strNickname")
                Log.d("TAG", "strNickname: $isUserNickName")
                if (isUserNickName == null) {
                    val setupIntent = Intent(this@MainActivity, InitialSetupActivity::class.java)
                    startActivity(setupIntent)
                    // fbFirestore?.collection("users")?.document(fireBaseAuth?.uid.toString())?.set(userInfo)
                }
            }
        }
    }

    private fun initNavigationBar() {
        bnv_main.run {
            setOnNavigationItemSelectedListener {
                val page = when (it.itemId) {
                    R.id.home -> 0
                    R.id.chat -> 1
                    R.id.my_page -> 2
                    else -> 0
                }

                if (page != vp_main.currentItem) {
                    vp_main.currentItem = page
                }

                true
            }
            selectedItemId = R.id.home
        }
    }

    private fun initViewPager() {
        vp_main.run {
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val navigation = when (position) {
                        0 -> R.id.home
                        1 -> R.id.chat
                        2 -> R.id.my_page
                        else -> R.id.home
                    }

                    if (bnv_main.selectedItemId != navigation) {
                        bnv_main.selectedItemId = navigation
                    }
                }
            })
        }
    }
}
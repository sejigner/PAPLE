package com.sejigner.closest

import android.content.Intent
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.sejigner.closest.Adapter.MainViewPagerAdapter
import com.sejigner.closest.fragment.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*


class MainActivity : AppCompatActivity(), FragmentHome.FlightListener,
    FragmentDialogWritePaper.WritePaperListenerMain, FragmentDialogFirst.FirstPlaneListenerMain {

    private var userName: String? = null
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFirestore: FirebaseFirestore? = null
    private var fbDatabase: FirebaseDatabase? = null
    private val fragmentHome by lazy { FragmentHome() }
    private val fragmentChat by lazy { FragmentChat() }
    private val fragmentMyPage by lazy { FragmentMyPage() }
    private val fragments: List<Fragment> = listOf(fragmentHome, fragmentChat, fragmentMyPage)
    private val LOCATION_PERMISSION_REQ_CODE = 1000;
    private val pagerAdapter: MainViewPagerAdapter by lazy { MainViewPagerAdapter(this, fragments) }
    private lateinit var dialog : LoadingDialog


    companion object {
        const val TAG = "MainActivity"
        const val ANONYMOUS = "anonymous"
        var UID = ""
        var MYNICKNAME = ""

        private lateinit var auth: FirebaseAuth

        fun getUid() : String{

            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewPager()
        initNavigationBar()

        dialog = LoadingDialog(this@MainActivity)

        userName = ANONYMOUS

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbDatabase = FirebaseDatabase.getInstance()

        fireBaseAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "got instance from Firestore successfully")
        UID = getUid()

        // 실시간 데이터베이스에 저장된 정보 유무를 통해 개인정보 초기설정 실행 여부 판단
        val uid = getUid()


        val reference = fbDatabase?.reference?.child("Users")?.child(uid)?.child("nickname")
        reference?.get()
            ?.addOnSuccessListener { it ->
                if (it.value != null) {
                    Log.d(TAG, "Checked, User Info already set - user nickname : ${it.value}")
                } else {
                    val setupIntent = Intent(this@MainActivity, InitialSetupActivity::class.java)
                    startActivity(setupIntent)
                }
            }

        MYNICKNAME = App.prefs.myNickname!!
        if (MYNICKNAME.isBlank()) {
            val ref =
                FirebaseDatabase.getInstance().getReference("/Users/$uid")
                    .child("nickname")
            ref.get().addOnSuccessListener {
                App.prefs.myNickname = it.value.toString()
            }
        }

        if(checkGooglePlayServices()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if(!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result

                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)

                val ref =
                    FirebaseDatabase.getInstance().getReference("/Users/$uid")
                        .child("registrationToken")
                // fcm토큰 업로드
                ref.setValue(token)
            })
        } else {
            Log.w(TAG, "Device doesn't have google play services")
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

    override fun onBackPressed() {
        if(vp_main.currentItem == 0) {
            finish()
        } else {
            vp_main.currentItem = 0
        }
    }

    private fun checkGooglePlayServices() : Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Error")
            false
        } else {
            Log.i(TAG, "Google play services updated")
            true
        }
    }
    override fun runFragmentDialogWritePaper(
        currentAddress: String,
        latitude: Double,
        longitude: Double
    ) {

        val dialog = FragmentDialogWritePaper.newInstance(
            UID, currentAddress, latitude, longitude
        )
        val fm = supportFragmentManager
        dialog.show(fm, "write paper")
    }

    // TODO : 유저가 어느정도 확보된 후 무조건 유저에게 도달하게 하고 거리 정보 제공
    override fun showSuccessFragment(flightDistance: Double) {
        closeYourDialogFragment()
        val fragmentFlySuccess = FragmentFlySuccess.newInstance(flightDistance)
        fragmentFlySuccess.show(supportFragmentManager, "successfulFlight")
    }

    override fun showSuccessFragment() {
        closeYourDialogFragment()
        val fragmentFlySuccess = FragmentFlySuccess()
        fragmentFlySuccess.show(supportFragmentManager, "successfulFlight")
    }

    override fun showLoadingDialog() {
        dialog.show()
    }

    override fun dismissLoadingDialog() {
        dialog.dismiss()
    }

    private fun closeYourDialogFragment() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        val fragmentToRemove = supportFragmentManager.findFragmentByTag("write paper")
        if (fragmentToRemove != null) {
            ft.remove(fragmentToRemove)
        }
        ft.addToBackStack(null)
        ft.commit() // or ft.commitAllowingStateLoss()
    }

    override fun showReplySuccessFragment(isReply: Boolean, flightDistance: Double) {
        closeYourDialogFragment()
        val fragmentFlySuccess = FragmentFlySuccess.newInstance(true, flightDistance)
        fragmentFlySuccess.show(supportFragmentManager, "successfulFlight")
    }
}
package com.sejigner.closest

import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.fragment.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.IOException
import java.util.*
import kotlin.math.round


class MainActivity : AppCompatActivity(), FragmentHome.FlightListener, FragmentDialogWritePaper.WritePaperListener {

    private var userName: String? = null
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFirestore: FirebaseFirestore? = null
    private var fbDatabase: FirebaseDatabase? = null
    private val fragmentHome by lazy { FragmentHome() }
    private val fragmentChat by lazy { FragmentChat() }
    private val fragmentMyPage by lazy { FragmentMyPage() }

    private val fragments: List<Fragment> = listOf(fragmentHome, fragmentChat, fragmentMyPage)
    val pageHistory = Stack<Int>()
    var saveToHistory = false

    private val LOCATION_PERMISSION_REQ_CODE = 1000;

    private val pagerAdapter: MainViewPagerAdapter by lazy { MainViewPagerAdapter(this, fragments) }


    private var flightDistance: Double = 0.0
    private var currentAddress: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var userCurrentLocation: Location? = null
    private var lastUser: String ?= null

    private lateinit var userFoundLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient



    companion object {
        const val TAG = "MainActivity"
        const val ANONYMOUS = "anonymous"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewPager()
        initNavigationBar()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        getClosestUser()

        userName = ANONYMOUS

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbDatabase = FirebaseDatabase.getInstance()

        fireBaseAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "got instance from Firestore successfully")



        // 실시간 데이터베이스에 저장된 정보 유무를 통해 개인정보 초기설정 실행 여부 판단
        val uid = fireBaseAuth?.uid
        val reference = fbDatabase?.reference?.child("Users")?.child(uid!!)?.child("strNickname")
        reference?.get()
            ?.addOnSuccessListener { it ->
                if (it.value != null) {
                    Log.d(TAG, "Checked, User Info already set - user nickname : ${it.value}")
                } else {
                    val setupIntent = Intent(this@MainActivity, InitialSetupActivity::class.java)
                    startActivity(setupIntent)
                }
            }

        pageHistory.push(0)



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

                    if (saveToHistory) {
                        if (pageHistory.contains(position)) {
                            pageHistory.remove(position)
                            pageHistory.push(position)
                        } else {
                            pageHistory.push(position)
                        }
                    }
                }
            })
            saveToHistory = true
        }
    }

    override fun onBackPressed() {
//        if (vp_main.currentItem == 0) {
//            super.onBackPressed()
//        } else {
//            vp_main.currentItem = vp_main.currentItem - 1
//        }

        if (pageHistory.size > 1) {

            saveToHistory = false;
            pageHistory.pop()
            vp_main?.currentItem = pageHistory.peek()
            saveToHistory = true;

        } else {
            Log.i(TAG, "pageHistory inside 0 size ${pageHistory.size}")

            if (pageHistory.size == 1) {
                pageHistory.pop()
            }
            if (vp_main?.currentItem == 0) {
                super.onBackPressed()
            } else {
                vp_main?.currentItem = 0
            }
        }

    }

    override fun setUserFound() {
        userFound = false
        userFoundId = ""
    }

    private var radius: Double = 0.0
    private var userFound: Boolean = false
    private var userFoundId: String = ""

    override fun runFragmentDialogWritePaper() {

        if(userFound) {
            val dialog = FragmentDialogWritePaper.newInstance(
                FirebaseAuth.getInstance().uid!!, userFoundId, currentAddress, flightDistance
            )
            val fm = supportFragmentManager
            dialog.show(fm, "write paper")
        } else {
            Toast.makeText(this, "10km 내에 유저가 없어요.",Toast.LENGTH_SHORT).show()
        }
    }

    override fun showSuccessFragment() {
        replaceYourDialogFragment()
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

    private fun replaceYourDialogFragment() {
        closeYourDialogFragment()
        val fragmentFlySuccess = FragmentFlySuccess.newInstance(flightDistance)
        fragmentFlySuccess.show(supportFragmentManager, "successfulFlight")
    }

    override fun getClosestUser() {
        getCurrentLocation()
        fbFirestore = FirebaseFirestore.getInstance()

        val userLocation: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("User-Location")
        val geoFire = GeoFire(userLocation)
        val geoQuery: GeoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
        geoQuery.removeAllListeners()


        // recursive method 이용
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if ((!userFound) && key != fireBaseUser?.uid) {
                    val uid = fireBaseUser?.uid
                    FirebaseDatabase.getInstance().getReference("/Acquaintances/$uid")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.hasChild(key!!)) {

                                    userFound = true

                                    userFoundId = key

                                    userFoundLocation = Location(location.toString())
                                    var ref: DatabaseReference =
                                        userLocation.child(userFoundId).child("l")
                                    ref.get().addOnSuccessListener {
                                        val map: List<Object> = it.value as List<Object>
                                        var locationFoundLat = 0.0
                                        var locationFoundLng = 0.0

                                        locationFoundLat = map[0].toString().toDouble()
                                        locationFoundLng = map[1].toString().toDouble()

                                        val locationFound: Location = Location("")
                                        locationFound.latitude = locationFoundLat
                                        locationFound.longitude = locationFoundLng

                                        val distance: Float =
                                            locationFound.distanceTo(userCurrentLocation)
                                        flightDistance = round((distance.toDouble()) * 100) / 100

                                    }
//
//                        Log.d(TAG, userFoundLocation.toString() + "현재 위치:"+ userCurrentLocation)
//
//                        val distance = userFoundLocation.distanceTo(userCurrentLocation).toDouble()
//                        Log.d(TAG, distance.toString())
//                        // 거리 소숫점 두번째 자리 반올림
//                        flightDistance = String.format("%.2f", distance).toDouble()
//                        // flightDistance = String.format("%.3f", distance).toFloat()/1000
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    Log.d(FragmentHome.TAG, "전에 만난 적이 있는 유저를 만났습니다.")
                }
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onGeoQueryReady() {
                if (!userFound) {
                    if (radius < 10) {
                        radius++
                        getClosestUser()
                    } else {
                        //Toast.makeText(this@MainActivity, "10km 이내에 유저가 없어요.", Toast.LENGTH_SHORT).show()
                        return
                    }

                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }
        })
    }

    override fun getCurrentLocation() {
        // checking location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request Permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQ_CODE
            )
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            // getting the last known or current location
            latitude = location.latitude
            longitude = location.longitude
            userCurrentLocation = location
            getAddress(location.latitude, location.longitude)

        }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed on getting current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getAddress(latitude: Double, longitude: Double): String {
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.d("CheckCurrentLocation", "현재 나의 위치 : $latitude, $longitude")

        var mGeocoder = Geocoder(this, Locale.KOREAN)
        var mResultList: List<Address>? = null
        try {
            mResultList = mGeocoder.getFromLocation(
                latitude, longitude, 1
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (mResultList != null) {
            Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
            currentAddress = mResultList[0].getAddressLine(0)
            Log.d("CheckCurrentLocation", "$currentAddress")
            // "대한민국" 삭제
            currentAddress = currentAddress.substring(4)
            Log.d("CheckCurrentLocation", "$currentAddress")
        }
        tv_update_location.text = currentAddress
        setLocationToDatabase(latitude, longitude)

        return currentAddress
    }

    private fun setLocationToDatabase(latitude: Double, longitude: Double) {
        var userId: String? = FirebaseAuth.getInstance().currentUser?.uid
        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("User-Location")

        var geoFire = GeoFire(ref)
        geoFire.setLocation(
            userId, GeoLocation(latitude, longitude)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                    Toast.makeText(
                        this, "You need to grant permission to access location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
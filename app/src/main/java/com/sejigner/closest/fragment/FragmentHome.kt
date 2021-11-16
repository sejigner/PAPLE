package com.sejigner.closest.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.sejigner.closest.*
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.coroutines.resumeWithException
import kotlin.math.round


private const val TAG = "MainActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34


class FragmentHome : Fragment(){

    companion object {
        const val TAG = "FlightLog"
        val CURRENTADDRESS = "CURRENT_ADDRESS"
    }

    private val LOCATION_PERMISSION_REQ_CODE = 1000;

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var fbFirestore: FirebaseFirestore? = null
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var currentAddress: String = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var userCurrentLocation: Location? = null
    private var mListener: FlightListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        getCurrentLocation()

        tv_update_location.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        tv_update_location.setOnClickListener {
            getCurrentLocation()
        }

        FirebaseDatabase.getInstance().getReference("/Acquaintances/$UID").child(UID).setValue("")

        bt_sign_out_test.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@FragmentHome.context, NewSignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        iv_paper_plane_home.setOnClickListener {
            mListener?.runFragmentDialogWritePaper(currentAddress, latitude, longitude)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FlightListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + "must implement FlightListener")
        }
    }


    interface FlightListener {
        fun runFragmentDialogWritePaper(currentAddress: String, latitude: Double, longitude: Double)
    }


    private var userFoundId: String = ""
    private var flightDistance: Double = 0.0

//    override fun getClosestUser() {
//        fbFirestore = FirebaseFirestore.getInstance()
//
//        val userLocation: DatabaseReference =
//            FirebaseDatabase.getInstance().reference.child("User-Location")
//        val geoFire = GeoFire(userLocation)
//        val geoQuery: GeoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
//        geoQuery.removeAllListeners()
//
//
//        // recursive method 이용
//        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
//            override fun onKeyEntered(key: String?, location: GeoLocation?) {
//                Log.d("geoQuery", key.toString())
//                if ((!userFound) && key != UID) {
//                    val ref = FirebaseDatabase.getInstance().getReference("/Acquaintances/$UID")
//
//                    CoroutineScope(IO).launch {
//
//                        if (ref.child(key!!).awaitsSingle()!!.exists()) {
//                            // user exists in the database
//                            Log.d(FragmentHome.TAG, "전에 만난 적이 있는 유저를 만났습니다.")
//                        } else {
//                            // user does not exist in the database
//                            userFound = true
//
//                            userFoundId = key
//
//                            userFoundLocation = Location(location.toString())
//                            val ref: DatabaseReference =
//                                userLocation.child(userFoundId).child("l")
//                            ref.get().addOnSuccessListener {
//
//                                val map: List<Object> = it.value as List<Object>
//
//                                calDistance(map)
//
//
//                            }
//                        }
//                    }
//                }
//            }
//
//            override fun onKeyExited(key: String?) {
//            }
//
//            override fun onKeyMoved(key: String?, location: GeoLocation?) {
//
//            }
//
//            override fun onGeoQueryReady() {
//                if (!userFound) {
//                    if (radius < 400) {
////                        radius++
//                        getClosestUser()
//                    }
//                }
//            }
//
//            override fun onGeoQueryError(error: DatabaseError?) {
//
//            }
//        })
//    }



//    suspend fun DatabaseReference.awaitsSingle(): DataSnapshot? =
//        suspendCancellableCoroutine { continuation ->
//            val listener = object : ValueEventListener {
//                override fun onCancelled(error: DatabaseError) {
//                    val exception = when (error.toException()) {
//                        is FirebaseException -> error.toException()
//                        else -> Exception("The Firebase call for reference $this was cancelled")
//                    }
//                    continuation.resumeWithException(exception)
//                }
//
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    try {
//                        continuation.resume(snapshot) {}
//                    } catch (exception: Exception) {
//                        continuation.resumeWithException(exception)
//                    }
//                }
//            }
//            continuation.invokeOnCancellation { this.removeEventListener(listener) }
//            this.addListenerForSingleValueEvent(listener)
//        }


    fun getCurrentLocation() {
        // checking location permission
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request Permission
            ActivityCompat.requestPermissions(
                requireActivity(),
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
            currentAddress = getAddress(location.latitude, location.longitude)

        }
            .addOnFailureListener {
                Toast.makeText(
                    requireActivity(),
                    "Failed on getting current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun getAddress(latitude: Double, longitude: Double): String {
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        Log.d("CheckCurrentLocation", "현재 나의 위치 : $latitude, $longitude")

        var mGeocoder = Geocoder(requireActivity(), Locale.KOREAN)
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
                        requireActivity(), "You need to grant permission to access location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

}





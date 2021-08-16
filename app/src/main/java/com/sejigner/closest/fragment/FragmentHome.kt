package com.sejigner.closest.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.location.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.*
import com.sejigner.closest.R
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.RuntimeException
import java.util.*

private const val TAG = "MainActivity"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34


class FragmentHome : Fragment() {

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
    private var mListener : FlightListener ?= null

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



        tv_update_location.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        tv_update_location.setOnClickListener {
            mListener?.getCurrentLocation()
        }

        bt_sign_out_test.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@FragmentHome.context, NewSignInActivity::class.java)
            startActivity(intent)
        }

        iv_paper_plane_home.setOnClickListener {
            mListener?.runFragmentDialogWritePaper()
        }
    }

<<<<<<< Updated upstream
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is FlightListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + "must implement FlightListener")
=======
    private fun performSendAnonymousMessage() {

        if (userFoundId != "") {
            var toId = userFoundId
            val text = et_message_paper.text.toString()
            val fromId = FirebaseAuth.getInstance().uid!!
            val distance = flightDistance

            val paperPlaneReceiverReference =
                FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$toId/$fromId")

            val acquaintanceRecordFromReference =
                FirebaseDatabase.getInstance().getReference("/Acquaintances/$fromId")
            val acquaintanceRecordToReference =
                FirebaseDatabase.getInstance().getReference("/Acquaintances/$toId")
            val paperplaneMessage = PaperplaneMessage(
                paperPlaneReceiverReference.key!!,
                text,
                fromId,
                toId,
                distance,
                System.currentTimeMillis() / 1000L,
                false
            )

            /* 같은 내용의 Message 데이터들을 각각 보낸 유저와 받은 유저의 ID로 저장
            paperPlaneReference.setValue(paperplaneMessage).addOnSuccessListener {
                Log.d(TAG, "The message has been flown $flightDistance away")
            }
            paperPlaneToReference.setValue(paperplaneMessage)

             */
            paperPlaneReceiverReference.setValue(paperplaneMessage).addOnFailureListener {
                Log.d(TAG, "Receiver 실패")
            }.addOnSuccessListener {
                Toast.makeText(
                    requireActivity(),
                    "종이비행기가 ${flightDistance}m 거리의 유저에게 도달했어요!",
                    Toast.LENGTH_LONG
                ).show()

                acquaintanceRecordFromReference.child(toId).child("haveMet").setValue(true)
                    .addOnSuccessListener {
                        Log.d(TAG, "소통 기록 저장 - 발신자: $fromId")
                    }
                acquaintanceRecordToReference.child(fromId).child("haveMet").setValue(true)
                    .addOnSuccessListener {
                        Log.d(TAG, "소통 기록 저장 - 수신자: $toId")
                    }
                getClosestUser()
            }
        }
        else {
            Toast.makeText(requireActivity(), "10km 이내에 유저가 없어요.", Toast.LENGTH_SHORT)
                .show()
>>>>>>> Stashed changes
        }
    }

    interface FlightListener {
        fun runFragmentDialogWritePaper()
        fun getClosestUser()
        fun getCurrentLocation()
    }

//    private var radius: Double = 0.0
//    private var userFound: Boolean = false
//    private var userFoundId: String = ""
//    private lateinit var userFoundLocation: Location
//    private var flightDistance: Double = 0.0


//    fun getClosestUser() {
//        getCurrentLocation()
//        fbFirestore = FirebaseFirestore.getInstance()
//
//        val userLocation: DatabaseReference =
//            FirebaseDatabase.getInstance().reference.child("User-Location")
//        val geoFire = GeoFire(userLocation)
//        val geoQuery: GeoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
//        geoQuery.removeAllListeners()
//
<<<<<<< Updated upstream
//
//        // recursive method 이용
//        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
//            override fun onKeyEntered(key: String?, location: GeoLocation?) {
//                if ((!userFound) && key != fireBaseUser?.uid) {
//                    val uid = fireBaseUser?.uid
//                    FirebaseDatabase.getInstance().getReference("/Acquaintances/$uid")
//                        .addValueEventListener(object : ValueEventListener {
//                            override fun onDataChange(snapshot: DataSnapshot) {
//                                if (!snapshot.hasChild(key!!)) {
//
//                                    userFound = true
//
//                                    userFoundId = key
//
//                                    userFoundLocation = Location(location.toString())
//                                    var ref: DatabaseReference =
//                                        userLocation.child(userFoundId).child("l")
//                                    ref.get().addOnSuccessListener {
//                                        val map: List<Object> = it.value as List<Object>
//                                        var locationFoundLat = 0.0
//                                        var locationFoundLng = 0.0
//
//                                        locationFoundLat = map[0].toString().toDouble()
//                                        locationFoundLng = map[1].toString().toDouble()
//
//                                        val locationFound: Location = Location("")
//                                        locationFound.latitude = locationFoundLat
//                                        locationFound.longitude = locationFoundLng
//
//                                        val distance: Float =
//                                            locationFound.distanceTo(userCurrentLocation)
//                                        flightDistance = round((distance.toDouble()) * 100) / 100
//                                    }
////
////                        Log.d(TAG, userFoundLocation.toString() + "현재 위치:"+ userCurrentLocation)
////
////                        val distance = userFoundLocation.distanceTo(userCurrentLocation).toDouble()
////                        Log.d(TAG, distance.toString())
////                        // 거리 소숫점 두번째 자리 반올림
////                        flightDistance = String.format("%.2f", distance).toDouble()
////                        // flightDistance = String.format("%.3f", distance).toFloat()/1000
//                                }
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//
//                            }
//                        })
//                    Log.d(TAG, "전에 만난 적이 있는 유저를 만났습니다.")
//
//
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
//                    if (radius < 10) {
//                        radius++
//                        getClosestUser()
//                    } else {
//                        Toast.makeText(requireActivity(), "10km 이내에 유저가 없어요.", Toast.LENGTH_SHORT)
//                            .show()
//                        return
//                    }
//
//                }
//            }
//
//            override fun onGeoQueryError(error: DatabaseError?) {
//
//            }
//        })
//    }
//
//    private fun getCurrentLocation() {
//        // checking location permission
//        if (ActivityCompat.checkSelfPermission(
//                requireActivity(),
//                android.Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request Permission
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
//                LOCATION_PERMISSION_REQ_CODE
//            )
//            return
//        }
//
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            // getting the last known or current location
//            latitude = location.latitude
//            longitude = location.longitude
//            userCurrentLocation = location
//            getAddress(location.latitude, location.longitude)
//
//        }
//            .addOnFailureListener {
//                Toast.makeText(
//                    requireActivity(),
//                    "Failed on getting current location",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//    }
//
//    private fun getAddress(latitude: Double, longitude: Double): String {
//        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        Log.d("CheckCurrentLocation", "현재 나의 위치 : $latitude, $longitude")
//
//        var mGeocoder = Geocoder(requireActivity(), Locale.KOREAN)
//        var mResultList: List<Address>? = null
//        try {
//            mResultList = mGeocoder.getFromLocation(
//                latitude, longitude, 1
//            )
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        if (mResultList != null) {
//            Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
//            currentAddress = mResultList[0].getAddressLine(0)
//            Log.d("CheckCurrentLocation", "$currentAddress")
//            // "대한민국" 삭제
//            currentAddress = currentAddress.substring(4)
//            Log.d("CheckCurrentLocation", "$currentAddress")
//        }
//        tv_update_location.text = currentAddress
//        setLocationToDatabase(latitude, longitude)
//
//        return currentAddress
//    }
//
//    private fun setLocationToDatabase(latitude: Double, longitude: Double) {
//        var userId: String? = FirebaseAuth.getInstance().currentUser?.uid
//        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("User-Location")
//
//        var geoFire = GeoFire(ref)
//        geoFire.setLocation(
//            userId, GeoLocation(latitude, longitude)
//        )
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            LOCATION_PERMISSION_REQ_CODE -> {
//                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission granted
//                } else {
//                    // permission denied
//                    Toast.makeText(
//                        requireActivity(), "You need to grant permission to access location",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//        }
//    }
=======
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
                    Log.d(TAG, "전에 만난 적이 있는 유저를 만났습니다.")


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
                        Toast.makeText(requireActivity(), "10km 이내에 유저가 없어요.", Toast.LENGTH_SHORT)
                            .show()
                        return
                    }

                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }
        })
    }

    private fun getCurrentLocation() {
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
            getAddress(location.latitude, location.longitude)

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

    private fun getRandomUser() : String {
        
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
>>>>>>> Stashed changes
}





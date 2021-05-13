package com.sejigner.closest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.lang.NullPointerException
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private var userName : String? = null

    private var fireBaseAuth : FirebaseAuth? = null
    private var fireBaseUser : FirebaseUser? = null
    private var googleApiClient : GoogleApiClient? = null
    private var fbFirestore : FirebaseFirestore? = null
    private lateinit var locationManager : LocationManager
    private val locationPermissionCode = 2
    private var currentLocation : String = ""
    private var latitude : Double? = null
    private var longitude : Double? = null
    private var locationGps : Location? = null
    private var locationNetwork : Location? = null
    private var currentCoordinates : Location? = null

    companion object {
        const val TAG = "MainActivity"
        const val ANONYMOUS = "anonymous"
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed $connectionResult ")

        Toast.makeText(this, "Google Play Services error",Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tvUpdateCoordinates: TextView = findViewById(R.id.tv_update_location)
        val tvCurrentLocation : TextView = findViewById(R.id.tv_address)

        googleApiClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API)
            .build()

        userName = ANONYMOUS

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser

        if (fireBaseUser == null) {
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        } else{
            userName = fireBaseUser!!.displayName
        }

        fireBaseAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()
        Log.d(TAG,"got instance from Firestore successfully")



        // definite the user's location address
        tvCurrentLocation.text = getLocation()

        // userInfos are set to FireStore under the document "uid"
        var userInfo = Users()
        userInfo.uid = fireBaseAuth?.uid
        userInfo.userId = fireBaseAuth?.currentUser?.email
        fbFirestore?.collection("users")?.document(fireBaseAuth?.uid.toString())?.set(userInfo)


        tvUpdateCoordinates.setOnClickListener{
            getCoordinates()
            tvCurrentLocation.text = getLocation()
        }

        // Button to save location data on FireStore
        bt_save_coordinates_test.setOnClickListener{
            if(latitude != null && longitude != null) {
                val pairCoordinates : Pair<Double, Double> = Pair(latitude!!,longitude!!)
                        if(userInfo.uid != null) {
                            val uid = userInfo.uid
                            userInfo.latlng = pairCoordinates
                            fbFirestore?.collection("users")?.document("$uid")?.update(mapOf("latlng" to pairCoordinates))?.addOnSuccessListener(this,
                                    OnSuccessListener {
                                        Log.d("CheckFirestore","set users' coordinates on firestore successfully")
                                        Toast.makeText(this, "위치정보를 저장했어요.",Toast.LENGTH_SHORT).show()
                                    })
                                    ?.addOnFailureListener {
                                        Log.d("CheckFirestore", "fail to set coordinates on firestore")
                                        Toast.makeText(this, "위치정보를 저장하지 못했어요.",Toast.LENGTH_SHORT).show()
                                    }
                        }
            }
        }

        // Button to move to MyPage for test
        bt_firestore_test.setOnClickListener{
            val nextIntent = Intent(this@MainActivity, MyPageActivity::class.java)
            startActivity(nextIntent)
        }
    }
    private fun getCoordinates() {
        val uid = fireBaseAuth?.currentUser?.uid
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        var hasGps : Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var hasNetwork : Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps || hasNetwork) {

            if(hasGps) {
                if (ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            ,locationPermissionCode)
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if(location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                    }
                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null) {
                    locationGps = localGpsLocation
                }


            }

            if(hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F, object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if(location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                    }
                })

                val localNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (localNetworkLocation != null) {
                    locationNetwork = localNetworkLocation
                }

            }

            if(locationGps != null && locationNetwork != null) {
                if(locationGps!!.accuracy > locationNetwork!!.accuracy) {
                    latitude = locationGps!!.latitude
                    longitude= locationGps!!.longitude
                    currentCoordinates = locationGps
                } else {
                    latitude = locationNetwork!!.latitude
                    longitude= locationNetwork!!.longitude
                    currentCoordinates = locationNetwork
                }
            }
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }
    /*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

    private fun getLocation() : String {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var userLocation : Location ?= currentCoordinates
        if(userLocation != null) {
            latitude = userLocation.latitude
            longitude =  userLocation.longitude
            Log.d("CheckCurrentLocation", "현재 나의 위치 : $latitude, $longitude")

            var mGeocoder = Geocoder(applicationContext, Locale.KOREAN)
            var mResultList : List<Address>?= null
            try {
                mResultList = mGeocoder.getFromLocation(
                        latitude!!, longitude!!, 1
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (mResultList != null) {
                Log.d("CheckCurrentLocation", mResultList[0].getAddressLine(0))
                currentLocation = mResultList[0].getAddressLine(0)
                currentLocation = currentLocation.substring(11)
            }

        }
        return currentLocation
    }

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }
}
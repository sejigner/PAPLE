package com.sejigner.closest.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.common.api.GoogleApi
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.MainActivity
import com.sejigner.closest.MainActivity.Companion.TAG
import com.sejigner.closest.R
import com.sejigner.closest.Users
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.IOException
import java.util.*


class FragmentHome : Fragment() {

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
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?, )  {
        super.onViewCreated(view, savedInstanceState)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser

        val uid = fireBaseAuth?.uid



        // Button to save location data on FireStore
        bt_save_coordinates_test.setOnClickListener{
            if(latitude != null && longitude != null) {
                val pairCoordinates : Pair<Double, Double> = Pair(latitude!!,longitude!!)

                Log.d("CheckFirestore","pairCoordinates=$pairCoordinates")
                if(uid != null) {
                    val docRef = fbFirestore?.collection("users")?.document("$uid")
                    docRef?.get()?.addOnSuccessListener {
                        fbFirestore?.collection("users")?.document("$uid")
                            ?.update(mapOf("latlng" to pairCoordinates))?.addOnSuccessListener(
                            requireActivity()
                        ) {
                            Log.d(
                                "CheckFirestore",
                                "set users' coordinates on firestore successfully"
                            )
                            Toast.makeText(requireActivity(), "위치정보를 저장했어요.", Toast.LENGTH_SHORT)
                                .show()
                        }
                            ?.addOnFailureListener {
                                Log.d("CheckFirestore", "fail to set coordinates on firestore")
                                Toast.makeText(
                                    requireActivity(),
                                    "위치정보를 저장하지 못했어요.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            } else Log.d("CheckFirestore","fail to save coordinates ")
        }

        tv_update_location.setOnClickListener{
            getCoordinates()
            tv_address.text = getLocation()
        }

    }

    private fun getCoordinates() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // locationManager = getSystemService() as LocationManager

        var hasGps : Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var hasNetwork : Boolean = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if(hasGps || hasNetwork) {

            if(hasGps) {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        ,locationPermissionCode)
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0F, object :
                    LocationListener {
                    override fun onLocationChanged(location: Location) {
                        latitude = location.latitude
                        longitude = location.longitude

                    }
                })

                val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (localGpsLocation != null) {
                    locationGps = localGpsLocation
                }


            }

            if(hasNetwork) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0F
                ) { location ->
                    latitude = location.latitude
                    longitude = location.longitude
                }

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
        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        var userLocation: Location? = currentCoordinates
        if (userLocation != null) {
            latitude = userLocation.latitude
            longitude = userLocation.longitude
            Log.d("CheckCurrentLocation", "현재 나의 위치 : $latitude, $longitude")

            var userId: String? = FirebaseAuth.getInstance().currentUser?.uid
            var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

            var geoFire = GeoFire(ref)
            geoFire.setLocation(
                userId, GeoLocation(userLocation.latitude, userLocation.longitude)
            )


            var mGeocoder = Geocoder(requireActivity(), Locale.KOREAN)
            var mResultList: List<Address>? = null
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
                Log.d("CheckCurrentLocation", "$currentLocation")
                // "대한민국" 삭제
                currentLocation = currentLocation.substring(4)
                Log.d("CheckCurrentLocation", "$currentLocation")
            }

        }
        return currentLocation
    }
}





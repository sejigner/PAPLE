package com.sejigner.closest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private var userName : String? = null

    private var fireBaseAuth : FirebaseAuth? = null
    private var fireBaseUser : FirebaseUser? = null
    private var googleApiClient : GoogleApiClient? = null
    private var fbFirestore : FirebaseFirestore? = null
    private lateinit var locationManager : LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
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

        var userInfo = Users()
        userInfo.uid = fireBaseAuth?.uid
        userInfo.userId = fireBaseAuth?.currentUser?.email
        fbFirestore?.collection("users")?.document(fireBaseAuth?.uid.toString())?.set(userInfo)

        val tvUpdateLocation: TextView = findViewById(R.id.tv_update_location)
        tvUpdateLocation.setOnClickListener{
            getLocation()
        }

        bt_firestore_test.setOnClickListener{
            val nextIntent = Intent(this@MainActivity, MyPageActivity::class.java)
            startActivity(nextIntent)
        }
    }
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
    }
    override fun onLocationChanged(location: Location) {
        tvGpsLocation = findViewById(R.id.tv_coordinates)
        tvGpsLocation.text = "Latitude: " + location.latitude + " , Longitude: " + location.longitude
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
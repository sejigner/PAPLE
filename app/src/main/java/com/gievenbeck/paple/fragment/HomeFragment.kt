package com.gievenbeck.paple.fragment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.location.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.gievenbeck.paple.App.Companion.countryCode
import com.gievenbeck.paple.MainActivity.Companion.getUid
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.gievenbeck.paple.MainActivity.Companion.isOnline
import com.gievenbeck.paple.R
import com.gievenbeck.paple.adapter.SentPaperPlaneAdapter
import com.gievenbeck.paple.models.PaperplaneMessage
import com.gievenbeck.paple.room.*
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
import kotlinx.android.synthetic.main.activity_otp.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*
import kotlin.math.round

private const val LOCATION_PERMISSION_REQ_CODE = 1000;

class FragmentHome : Fragment(), AlertDialogChildFragment.OnConfirmedListener {

    companion object {
        const val TAG = "FlightLog"

        // GeoFire Query 최대 거리
        const val MAX_RADIUS = 550
    }


    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var currentAddress: String = ""
    private var latitude: Double = 0.0
    private var sentMessage: String = ""
    private var longitude: Double = 0.0
    private var userFound: Boolean = false
    private var radius: Double = 0.0
    private var foundUserId: String = ""
    private var userCurrentLocation: Location? = null
    private var mListener: FlightListener? = null
    lateinit var viewModel: FragmentChatViewModel
    lateinit var locationManager: LocationManager
    private var uid = ""
//    private var timerTask: Timer ?= null
//    private var milliSec = 0.0


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
        uid = getUid()

        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)

        viewModel =
            ViewModelProvider(requireActivity(), factory)[FragmentChatViewModel::class.java]

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        val updateAnimation: Animation =
            AnimationUtils.loadAnimation(requireActivity(), R.anim.anim_update_location)
        tv_update_location.setOnClickListener {
            iv_update_location.startAnimation(updateAnimation)
            getCurrentLocation()
        }

        tv_paper_send.setOnClickListener {
            if (isOnline) {
                if(userCurrentLocation!=null) {
                    mListener?.confirmFlight()
                } else {
                    Toast.makeText(requireActivity(),"먼저 위치 정보를 업데이트 해주세요!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show()
            }

        }

        tv_delete_all_records.setOnClickListener {
            confirmDelete()
        }

        tv_paper_send.isEnabled = false
        et_write_paper?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                tv_count_letter_paper?.text = getString(R.string.limit_write)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val userinput = et_write_paper.text.toString()
                tv_count_letter_paper?.text = userinput.length.toString() + " / 200"
                tv_paper_send.isEnabled = s != null && s.toString().isNotEmpty()
            }
        })
        et_write_paper.setOnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 키보드 내리기
                val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(et_write_paper.windowToken, 0)
                handled = true
            }
            handled
        }

        val sentPlaneAdapter = SentPaperPlaneAdapter(listOf(), viewModel) { MyPaper ->

            val dialog = FragmentDialogSent.newInstance(
                MyPaper
            )
            val fm = childFragmentManager
            dialog.show(fm, "my paper")
        }

        rv_sent_paper.adapter = sentPlaneAdapter

        val mLayoutManager = LinearLayoutManager(requireActivity())
        mLayoutManager.reverseLayout = true
        mLayoutManager.stackFromEnd = true
        mLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rv_sent_paper.layoutManager = mLayoutManager

        viewModel.allMyPaperPlaneRecord(uid).observe(viewLifecycleOwner, Observer {
            sentPlaneAdapter.differ.submitList(it)
            tv_delete_all_records.isEnabled = it.isNotEmpty()
            rv_sent_paper.scrollToPosition(it.size - 1)
        })

        viewModel.setCurrentLocation("")
        viewModel.currentLocation.observe(viewLifecycleOwner, {
            Log.d("current location", "result : $it")
            if(it.isNotEmpty()) {
                tv_update_location.text = it
            } else {
                tv_update_location.text = resources.getString(R.string.update_address)
            }
        })
        getCurrentLocation()
    }

    override fun onStart() {
        super.onStart()
        tv_update_location.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tv_delete_all_records.paint.isUnderlineText = true
    }

    fun sendPaperPlane() {
        mListener?.showLoadingBottomSheet()
        viewModel.setResult("flying")
        sentMessage = et_write_paper.text.toString()
        savePaperToDB(sentMessage)
        et_write_paper.text.clear()
        getClosestUser()
    }


    private fun savePaperToDB(message: String) {
        val myPaperRecord = MyPaper(null, uid, message, System.currentTimeMillis() / 1000L)
        viewModel.insertPaperRecord(myPaperRecord)
    }

    override fun proceed() {
        deleteAllMyPaper()
    }

    private fun deleteAllMyPaper() {
        viewModel.deleteAll(uid)
        Toast.makeText(requireContext(), "제거되었습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun confirmDelete() {
        val alertDialog = AlertDialogChildFragment.newInstance(
            "날려보낸 모든 비행기 기록을 제거하시겠습니까?", "제거하기"
        )
        val fm = childFragmentManager
        alertDialog.show(fm, "deleteConfirmation")
    }


    private fun performSendAnonymousMessage() {
        val toId = foundUserId
        val message = sentMessage
        val fromId = uid
        val distance = flightDistance
        val timestamp = System.currentTimeMillis() / 1000L
        val paperPlaneReceiverReference =
            FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$countryCode/$toId/$fromId")

        val paperplaneMessage = PaperplaneMessage(
            paperPlaneReceiverReference.key!!,
            message,
            fromId,
            toId,
            distance,
            timestamp,
            false
        )

        paperPlaneReceiverReference.setValue(paperplaneMessage).addOnFailureListener {
            Log.d(FragmentHome.TAG, "Receiver 실패")
            Toast.makeText(
                requireActivity(),
                resources.getText(R.string.no_internet),
                Toast.LENGTH_SHORT
            ).show()
        }.addOnSuccessListener {
            val sentPaper = MyPaperPlaneRecord(
                paperplaneMessage.toId,
                uid,
                paperplaneMessage.text,
                paperplaneMessage.timestamp
            )
            viewModel.insert(sentPaper)
            val acquaintances = Acquaintances(toId, uid)
            viewModel.insert(acquaintances)
        }
        foundUserId = ""
        viewModel.setResult("success")
    }

    fun getClosestUser() {
        val userLocation: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("User-Location").child(countryCode)
        val geoFire = GeoFire(userLocation)
        val geoQuery: GeoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
        geoQuery.removeAllListeners()

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                runBlocking {
                    Log.d("geoQuery", key.toString())
                    if ((!userFound) && key != uid) {
                        // Room DB로 대체
                        val haveMet: Boolean = viewModel.haveMet(uid, key!!).await()
                        if (haveMet) {
                            // user exists in the database
                            Log.d(TAG, "전에 만난 적이 있는 유저를 만났습니다. $key")
                        } else {
                            // user does not exist in the database
                            userFound = true

                            foundUserId = key
                            val userFoundLocation =
                                GeoLocation(location!!.latitude, location.longitude)

                            calDistance(userFoundLocation)

                            performSendAnonymousMessage()
                        }
                    }
                }
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onGeoQueryReady() {
                if (!userFound && (radius < MAX_RADIUS)) {
                    radius++
                    Log.d("test", "$radius")
                    getClosestUser()
                } else {
                    userFound = false
                    radius = 0.0
                    viewModel.setResult("success")
                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }
        })
    }

    private fun calDistance(location: GeoLocation?) {
        var locationFoundLat = 0.0
        var locationFoundLng = 0.0
        if (location != null) {
            locationFoundLat = location.latitude
            locationFoundLng = location.longitude
        }
        val locationFound = Location("")
        locationFound.latitude = locationFoundLat
        locationFound.longitude = locationFoundLng

        val distance: Float =
            locationFound.distanceTo(userCurrentLocation)
        flightDistance = round((distance.toDouble()) * 100) / 100
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
        fun confirmFlight()
        fun showLoadingBottomSheet()
        fun checkLocationAccessPermission()
    }

    private var flightDistance: Double = 0.0

    private fun getCurrentLocation() {
        // checking location permission
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mListener?.checkLocationAccessPermission()
        } else {
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    // getting the last known or current location
                    latitude = location.latitude
                    longitude = location.longitude
                    userCurrentLocation = location
                    currentAddress = getAddress(location.latitude, location.longitude)
                    tv_update_location.text = currentAddress
                }.addOnFailureListener {

                    Toast.makeText(
                        requireActivity(),
                        "현재 위치를 감지하지 못했어요.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(requireActivity(),"위치 정보를 업데이트 하시려면 기기의 GPS 기능을 켜주세요.",Toast.LENGTH_SHORT).show()
            }

        }
    }


    private fun getAddress(latitude: Double, longitude: Double): String {
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
        setLocationToDatabase(latitude, longitude)
        return currentAddress
    }

    private fun setLocationToDatabase(latitude: Double, longitude: Double) {
        var ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("User-Location").child(countryCode)

        var geoFire = GeoFire(ref)
        if(uid.isNotEmpty()) {
            geoFire.setLocation(
                uid, GeoLocation(latitude, longitude)
            )
        }
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





package com.sejigner.closest.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.room.*
import kotlinx.android.synthetic.main.fragment_dialog_first.*
import kotlinx.android.synthetic.main.fragment_dialog_write.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import kotlin.math.round


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ITEMS = "data"

/**
 * A simple [Fragment] subclass.
 * Use the [FragmentDialogFirst.newInstance] factory method to
 * create an instance of this fragment.
 */
class FragmentDialogWritePaper : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var uid: String? = null
    private var currentAddress: String? = null
    private var mCallbackMain: WritePaperListenerMain? = null
    lateinit var viewModel: FragmentChatViewModel
    private var radius: Double = 0.0
    private var userFound: Boolean = false
    private var userFoundId: String = ""
    private var flightDistance: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var userCurrentLocation: Location? = Location("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString("uid")
            currentAddress = it.getString("currentAddress")
            latitude = it.getDouble("latitude", latitude)
            longitude = it.getDouble("longitude", longitude)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(true)
        } else {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_write, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)

        viewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        val etPaper = view.findViewById<View>(R.id.et_write_paper) as? EditText
        val textCount = view.findViewById<View>(R.id.tv_count_letter_paper) as? TextView
        val btnClose = view.findViewById<View>(R.id.iv_close_paper) as? ImageView
        val btnFly = view.findViewById<View>(R.id.tv_paper_send_dialog) as? TextView
        val location = view.findViewById<View>(R.id.tv_update_location_paper) as? TextView
        val job = Job()
        userCurrentLocation?.longitude = longitude
        userCurrentLocation?.latitude = latitude


        location?.text = currentAddress
        btnFly?.setOnClickListener {
            mCallbackMain?.showLoadingDialog()

            getClosestUser()


        }

        btnClose?.setOnClickListener {
            dismiss()
        }



    }

    interface WritePaperListenerMain {
        fun showSuccessFragment(flightDistance: Double)
        fun showSuccessFragment()
        fun showLoadingDialog()
        fun dismissLoadingDialog()
    }

    fun Window.getSoftInputMode(): Int {
        return attributes.softInputMode
    }

    // TODO : 파이어베이스 데이터베이스 내 세 개의 Path 설정; User-Location, Female-User-Location, Male-User-Location
    //  초기 설정 시 User-Location + 남/여 2가지 Path에 데이터 입력
    //  기본 탐색 -> User-Location 탐색, 필터 탐색 -> 성별-Location 탐색 (생년 탐색은 추후 고민)
    //  getClosestUser -> onGeoQueryReady -> radius++ -> onKeyEntered ->
    fun getClosestUser() {
        val userLocation: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("User-Location")
        val geoFire = GeoFire(userLocation)
        val geoQuery: GeoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
        geoQuery.removeAllListeners()

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                runBlocking {
                    Log.d("geoQuery", key.toString())
                    if ((!userFound) && key != UID) {
                        var haveMet: Boolean
                        // Room DB로 대체
                        haveMet = viewModel.haveMet(UID, key!!).await()
                        if (haveMet) {
                            // user exists in the database
                            Log.d(FragmentHome.TAG, "전에 만난 적이 있는 유저를 만났습니다. $key")
                        } else {
                            // user does not exist in the database
                            userFound = true

                            userFoundId = key!!
                            val userFoundLocation =
                                GeoLocation(location!!.latitude, location!!.longitude)

                            calDistance(userFoundLocation)

                            performSendAnonymousMessage()
                            dismiss()
                            // mCallbackMain?.dismissLoadingDialog()
                        }
                    }
                }
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onGeoQueryReady() {
                if (!userFound && (radius < 15)) {
                    radius++
                    getClosestUser()
                } else {
                    mCallbackMain?.dismissLoadingDialog()
                    // TODO : 상대방을 찾지 않았음을 알리지 않기 위해 우선 비행거리는 제공 X'
                    //   추후 사용자수가 확보되면 거리 제공
                    mCallbackMain?.showSuccessFragment()
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

    private fun performSendAnonymousMessage() {


        val toId = userFoundId
        val message = et_write_paper_dialog.text.toString()
        val fromId = UID
        val distance = flightDistance

        val paperPlaneReceiverReference =
            FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$toId/$fromId")

        val paperplaneMessage = PaperplaneMessage(
            paperPlaneReceiverReference.key!!,
            message,
            fromId,
            toId,
            distance,
            System.currentTimeMillis() / 1000L,
            false
        )

        paperPlaneReceiverReference.setValue(paperplaneMessage).addOnFailureListener {
            Log.d(FragmentHome.TAG, "Receiver 실패")
        }.addOnSuccessListener {
            val sentPaper = MyPaperPlaneRecord(
                paperplaneMessage.toId,
                UID,
                paperplaneMessage.text,
                paperplaneMessage.timestamp
            )
            viewModel.insert(sentPaper)
        }
        val acquaintances = Acquaintances(toId, UID)
        viewModel.insert(acquaintances)
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is WritePaperListenerMain) {
            mCallbackMain = context
        } else {
            throw RuntimeException(context.toString() + "must implement WritePaperListener")
        }
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FragmentDialog.
         */

        const val TAG = "WritePaperDialog"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(uid: String, currentAddress: String, latitude: Double, longitude: Double) =
            FragmentDialogWritePaper().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                    putString("currentAddress", currentAddress)
                    putDouble("latitude", latitude)
                    putDouble("longitude", longitude)
                }
            }
    }
}

package com.sejigner.closest.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.firestore.FirebaseFirestore
import com.sejigner.closest.LoadingDialog
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.R
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.room.MyPaperPlaneRecord
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
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
    private var mCallbackMain : WritePaperListenerMain ?= null
    private var mCallbackHome : WritePaperListenerHome ?= null
    lateinit var ViewModel: FragmentChatViewModel
    private var radius: Double = 350.0
    private var userFound: Boolean = false
    private var partnerSet : Boolean = false
    private var userFoundId: String = ""
    private lateinit var userFoundLocation: Location
    private var flightDistance: Double = 0.0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var userCurrentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            uid = it.getString("uid")
            currentAddress = it.getString("currentAddress")
            latitude = it.getDouble("latitude", latitude)
            longitude = it.getDouble("longitude", longitude)
        }
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

        ViewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        val etPaper = view.findViewById<View>(R.id.et_write_paper) as? EditText
        val textCount = view.findViewById<View>(R.id.tv_count_letter_paper) as? TextView
        val btnClose = view.findViewById<View>(R.id.iv_close_paper) as? ImageView
        val btnFly = view.findViewById<View>(R.id.tv_paper_send) as? TextView
        val location = view.findViewById<View>(R.id.tv_update_location_paper) as? TextView



        location?.text = currentAddress
        btnFly?.setOnClickListener{
            val dialog = LoadingDialog(requireActivity())

            CoroutineScope(Main).launch {
                dialog.show()
                getClosestUser()
                delay(3000)
                dialog.dismiss()

                val isSuccess = performSendAnonymousMessage()
                if(isSuccess) {

                    mCallbackMain?.showSuccessFragment(flightDistance)
                    dismiss()

                }
            }


        }

        btnClose?.setOnClickListener {
            dismiss()
        }

        etPaper?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                textCount?.text = getString(R.string.limit_write)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var userinput = et_write_paper.text.toString()
                textCount?.text = userinput.length.toString() + " / 250"
            }

            override fun afterTextChanged(s: Editable?) {
                var userinput = et_write_paper.text.toString()
                textCount?.text = userinput.length.toString() + " / 250"
            }
        })

    }

    suspend fun getClosestUser() = CoroutineScope(IO).launch {

        val userLocation: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child("User-Location")
        val geoFire = GeoFire(userLocation)
        val geoQuery: GeoQuery = geoFire.queryAtLocation(GeoLocation(latitude, longitude), radius)
        geoQuery.removeAllListeners()


        // recursive method 이용
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Log.d("geoQuery", key.toString())
                if ((!userFound) && key != UID) {
                    val ref = FirebaseDatabase.getInstance().getReference("/Acquaintances/$UID")

                    CoroutineScope(Dispatchers.IO).launch {
                        // Room DB로 대체
                        if (ref.child(key!!).awaitsSingle()!!.exists()) {
                            // user exists in the database
                            Log.d(FragmentHome.TAG, "전에 만난 적이 있는 유저를 만났습니다.")
                        } else {
                            // user does not exist in the database
                            userFound = true

                            userFoundId = key

                            userFoundLocation = Location(location.toString())
                            val ref: DatabaseReference =
                                userLocation.child(userFoundId).child("l")
                            ref.get().addOnSuccessListener {

                                val map: List<Object> = it.value as List<Object>

                                calDistance(map)
                                partnerSet = true
                            }
                        }
                    }
                }
            }

            override fun onKeyExited(key: String?) {
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {

            }

            override fun onGeoQueryReady() {
                if (!userFound) {
                        getClosestUser()
                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {

            }
        })
    }

    private fun calDistance(location: List<Object>) {
        var locationFoundLat = 0.0
        var locationFoundLng = 0.0
        locationFoundLat = location[0].toString().toDouble()
        locationFoundLng = location[1].toString().toDouble()

        val locationFound = Location("")
        locationFound.latitude = locationFoundLat
        locationFound.longitude = locationFoundLng

        val distance: Float =
            locationFound.distanceTo(userCurrentLocation)
        flightDistance = round((distance.toDouble()) * 100) / 100
    }

    fun setUserFound() {
        userFound = false
        userFoundId = ""
    }


    private fun performSendAnonymousMessage() : Boolean {
        var success : Boolean = false
        if (userFoundId != "") {
            val toId = userFoundId!!
            val message = et_write_paper.text.toString()
            val fromId = UID
            val distance = flightDistance

            val paperPlaneReceiverReference =
                FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$toId/$fromId")

            val acquaintanceRecordFromReference =
                FirebaseDatabase.getInstance().getReference("/Acquaintances/$fromId")
            val acquaintanceRecordToReference =
                FirebaseDatabase.getInstance().getReference("/Acquaintances/$toId")
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
                val sentPaper = MyPaperPlaneRecord(paperplaneMessage.toId,paperplaneMessage.text, paperplaneMessage.timestamp)
                ViewModel.insert(sentPaper)

                acquaintanceRecordFromReference.child(toId).setValue("")
                    .addOnSuccessListener {
                        Log.d(FragmentHome.TAG, "소통 기록 저장 - 발신자: $fromId")
                    }
                acquaintanceRecordToReference.child(fromId).setValue("haveMet")
                    .addOnSuccessListener {
                        Log.d(FragmentHome.TAG, "소통 기록 저장 - 수신자: $toId")
                        success = true
                    }
            }
            return success

        } else return success
    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    interface WritePaperListenerMain {
        fun showSuccessFragment(flightDistance: Double)
    }

    interface WritePaperListenerHome {
        fun getClosestUser()
        fun setUserFound()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is WritePaperListenerMain) {
            mCallbackMain = context
        } else {
            throw RuntimeException(context.toString() + "must implement WritePaperListener")
        }
    }


    companion object{
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
        fun newInstance(uid: String,currentAddress: String, latitude:Double, longitude : Double) =
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
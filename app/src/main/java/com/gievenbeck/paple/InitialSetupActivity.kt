package com.gievenbeck.paple

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.gievenbeck.paple.fragment.FragmentHome
import com.gievenbeck.paple.models.Users
import kotlinx.android.synthetic.main.activity_initial_setup.*
import kotlinx.android.synthetic.main.activity_initial_setup.rb_female
import kotlinx.android.synthetic.main.activity_initial_setup.rb_male
import java.util.*
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.app.Service
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.text.InputFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.*
import com.gievenbeck.paple.fragment.AlertDialogFragment
import com.gievenbeck.paple.room.PaperPlaneDatabase
import com.gievenbeck.paple.room.PaperPlaneRepository
import com.gievenbeck.paple.room.User
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
import java.util.regex.Pattern


class InitialSetupActivity : AppCompatActivity(), AlertDialogFragment.OnConfirmedListener {
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFireStore: FirebaseFirestore? = null
    private var fbDatabase: FirebaseDatabase? = null
    private var isDuplicated: Boolean = false
    private var uid: String? = null
    private var lastTimePressed = 0L
    private val date: Calendar = Calendar.getInstance()
    private val year = date.get(Calendar.YEAR)
    private var userInfo = Users()
    lateinit var inputMethodManager: InputMethodManager
    lateinit var viewModel: FragmentChatViewModel
    private var isOnline = false

    companion object {
        const val TAG = "InitialSetupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbFireStore = FirebaseFirestore.getInstance()
        fbDatabase = FirebaseDatabase.getInstance()
        uid = fireBaseAuth!!.currentUser?.uid
        initFcmToken()

        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)

        viewModel =
            ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]

        inputMethodManager =
            getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager

        rg_initial_gender.visibility = View.GONE
        numberPicker_birth_year_initial_setup.visibility = View.GONE

        val networkConnect = NetworkConnection(this)
        networkConnect.observe(this) { isConnected ->
            isOnline = when (isConnected) {
                true -> true
                else -> false
            }
        }

        btn_birth_year.setOnClickListener {
            if (numberPicker_birth_year_initial_setup.visibility == View.GONE) {
                et_nickname.clearFocus()
                hideKeyboard()
                Log.d("Year", "$year")
                numberPicker_birth_year_initial_setup.minValue = date.get(Calendar.YEAR) - 80
                numberPicker_birth_year_initial_setup.maxValue = date.get(Calendar.YEAR) - 12
                userInfo.birthYear = "1994"
                tv_initial_birth_year.text = "1994"
                numberPicker_birth_year_initial_setup.value = 1994
                Log.d(
                    "Year",
                    "${numberPicker_birth_year_initial_setup.minValue}~${numberPicker_birth_year_initial_setup.maxValue}"
                )

                rg_initial_gender.visibility = View.GONE
                numberPicker_birth_year_initial_setup.visibility = View.VISIBLE

                numberPicker_birth_year_initial_setup.setOnValueChangedListener { picker: NumberPicker, oldVal, newVal ->
                    Log.d("InitialSetupActivity", "oldVal : ${oldVal}, newVal : $newVal")
                    userInfo.birthYear = newVal.toString()
                    tv_initial_birth_year.text = newVal.toString()
                    tv_initial_birth_year.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.black
                        )
                    )
                    Log.d("InitialSetupActivity", "User's birthday's been set as $newVal")

                }
            }
        }

        btn_gender.setOnClickListener {
            if (rg_initial_gender.visibility == View.GONE) {
                hideKeyboard()
                numberPicker_birth_year_initial_setup.visibility = View.GONE
                rg_initial_gender.visibility = View.VISIBLE

                rb_female.setOnClickListener {
                    userInfo.gender = "female"
                    tv_initial_gender.text = "여성"
                    tv_initial_gender.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.black
                        )
                    )
                }

                rb_male.setOnClickListener {
                    userInfo.gender = "male"
                    tv_initial_gender.text = "남성"
                    tv_initial_gender.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.black
                        )
                    )
                }
            }
        }

        // 닉네임 입력 감지 리스너
        et_nickname.addTextChangedListener(textWatcherNickname)

        cl_initial_start.setOnClickListener {
            if ((userInfo.nickname.isNullOrEmpty() || userInfo.birthYear.isNullOrEmpty() || userInfo.gender.isNullOrEmpty()))
                Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
            else {
                if (isDuplicated) {
                    Toast.makeText(this, "다른 닉네임을 사용해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    confirmInformation()
                }
            }
        }

        et_nickname.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            val ps: Pattern =
                Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$")
            if (source.equals("") || ps.matcher(source).matches()) {
                return@InputFilter source
            }
            Toast.makeText(this, "한글, 영문, 숫자만 사용해주세요!", Toast.LENGTH_SHORT).show()
            ""
        }, InputFilter.LengthFilter(10))


    }

    private fun confirmInformation() {
        val alertDialog = AlertDialogFragment.newInstance(
            "이대로 가입하시겠어요?\n가입 후 수정이 불가능합니다", "가입하기"
        )
        val fm = supportFragmentManager
        alertDialog.show(fm, "confirmation")
    }

    override fun proceed() {
        if(isOnline) {
            setInitialSetupToFirebase()
        } else {
            Toast.makeText(this, R.string.no_internet,Toast.LENGTH_SHORT).show()
        }
    }

    private fun setInfoToRoomDB() {
        val user = User(uid!!, userInfo.nickname!!, userInfo.gender!!, userInfo.birthYear!!.toInt())
        viewModel.insert(user)
    }

    private val textWatcherNickname = object : TextWatcher {
        private var timer = Timer()
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            duplication_check.text = ""
            if (s != null && s.toString().isNotEmpty()) {
                et_nickname.gravity = Gravity.END
            } else {
                et_nickname.gravity = Gravity.START
            }
        }

        override fun afterTextChanged(s: Editable?) {
            // TODO : 입력 딜레이 시키기
            duplication_check_progress_bar.visibility = View.VISIBLE
            timer.cancel()
            timer = Timer()
            if (s != null && s.toString().isNotEmpty()) {
                et_nickname.gravity = Gravity.END
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        val reference = fbDatabase?.reference!!
                        val query: Query =
                            reference.child("Users").orderByChild("nickname").equalTo(s.toString())
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    duplication_check.setTextColor(
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.txt_red
                                        )
                                    )
                                    userInfo.nickname = s.toString()
                                    isDuplicated = true
                                    Log.d(this.toString(), "닉네임 중복 : $s")
                                    duplication_check_progress_bar.visibility = View.INVISIBLE
                                    duplication_check.text = "이미 사용중인 닉네임이에요!"
                                } else {
                                    duplication_check.setTextColor(
                                        ContextCompat.getColor(
                                            applicationContext,
                                            R.color.paperplane_theme
                                        )
                                    )
                                    Log.d(this.toString(), "닉네임 사용 가능 : $s")
                                    userInfo.nickname = s.toString()
                                    isDuplicated = false
                                    duplication_check_progress_bar.visibility = View.INVISIBLE
                                    duplication_check.text = "사용 가능한 닉네임이에요!"
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }, 500)

            } else {
                duplication_check_progress_bar.visibility = View.INVISIBLE
                et_nickname.gravity = Gravity.START
                duplication_check.text = ""
            }
        }
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(et_nickname.windowToken, 0)
    }

    private fun initFcmToken() {
        if (checkGooglePlayServices()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(
                        TAG,
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    return@OnCompleteListener
                }

                val token = task.result

                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)
            })
        } else {
            Log.w(TAG, "Device doesn't have google play services")
        }

    }


    private fun setInitialSetupToFirebase() {
        val database = fbDatabase?.reference
        userInfo.status = "active"
        database?.child("Users")?.child(uid!!)?.setValue(userInfo)?.addOnSuccessListener {
            Log.d(
                TAG,
                "Saved Users info to Firebase Realtime database: ${database.key}"
            )
            setInfoToRoomDB()
            val intent = Intent(this@InitialSetupActivity, SplashCongratsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun checkGooglePlayServices(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Error")
            false
        } else {
            Log.i(TAG, "Google play services updated")
            true
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - lastTimePressed < 2000) //short Toast duration, now should be faded out
            finish()
        else {
            Toast.makeText(this, "앱을 종료하시려면 뒤로가기 버튼을 두번 터치해주세요.", Toast.LENGTH_SHORT).show()
        }

        lastTimePressed = System.currentTimeMillis()
    }

//    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
//        override fun onAvailable(network: Network) {
//            isOnline = true
//        }
//
//        override fun onLost(network: Network) {
//            isOnline = false
//        }
//    }
//
//    private fun registerNetworkCallback() {
//        val connectivityManager = getSystemService(ConnectivityManager::class.java)
//        val networkRequest = NetworkRequest.Builder()
//            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
//            .build()
//        connectivityManager.registerNetworkCallback(networkRequest, networkCallBack)
//    }
//
//    private fun terminateNetworkCallback() {
//        val connectivityManager = getSystemService(ConnectivityManager::class.java)
//        connectivityManager.unregisterNetworkCallback(networkCallBack)
//    }

}



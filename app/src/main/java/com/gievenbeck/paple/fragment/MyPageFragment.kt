package com.gievenbeck.paple.fragment

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gievenbeck.paple.App
import com.gievenbeck.paple.App.Companion.countryCode
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.R
import com.gievenbeck.paple.SettingActivity
import com.gievenbeck.paple.SignInActivity
import com.gievenbeck.paple.models.Users
import com.gievenbeck.paple.room.PaperPlaneDatabase
import com.gievenbeck.paple.room.PaperPlaneRepository
import com.gievenbeck.paple.room.User
import com.gievenbeck.paple.ui.FragmentChatViewModel
import com.gievenbeck.paple.ui.FragmentChatViewModelFactory
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class MyPageFragment : Fragment(), AlertDialogChildFragment.OnConfirmedListener {

    lateinit var viewModel: FragmentChatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    )
            : View? {
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = PaperPlaneRepository(PaperPlaneDatabase(requireActivity()))
        val factory = FragmentChatViewModelFactory(repository)
        viewModel =
            ViewModelProvider(requireActivity(), factory).get(FragmentChatViewModel::class.java)

        init()
    }

    private fun init() {
        setInfo()

        val nickname = App.prefs.myNickname!!

        tv_log_out_my_page.setOnClickListener {
            val alertDialog = AlertDialogChildFragment.newInstance(
                "정말 로그아웃 하시겠어요?", "로그아웃"
            )
            val fm = childFragmentManager
            alertDialog.show(fm, "My paper logout confirmation")
        }

        tv_setting_my_page.setOnClickListener {
            val intent = Intent(this@MyPageFragment.context, SettingActivity::class.java)
            intent.putExtra("nickname", nickname)
            startActivity(intent)
        }

        tv_open_source_license.setOnClickListener {
            startActivity(Intent(this@MyPageFragment.context, OssLicensesMenuActivity::class.java))
        }
    }

    private fun signOutFromFirebase() {

        FirebaseAuth.getInstance().signOut()
        // Firebase 내 토큰 제거
        val fbDatabase = FirebaseDatabase.getInstance().reference.child("Users/$countryCode/$UID/registrationToken")
        fbDatabase.removeValue().addOnFailureListener {
            val intent = Intent(this@MyPageFragment.context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }.addOnSuccessListener {
            // SharedPreference 닉네임 값 제거
            App.prefs.myNickname = ""
            // 로그인 페이지 이동
            val intent = Intent(this@MyPageFragment.context, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun setInfo() {
        val nickname = App.prefs.myNickname!!
        tv_nickname_my_page.text = nickname
        tv_current_version.text = getVersionInfo()
        CoroutineScope(IO).launch {
            if (viewModel.isExists(UID).await()) {
                val info = viewModel.getUser(UID).await()
                CoroutineScope(Main).launch {
                    if (info.gender == "male") {
                        tv_gender_my_page.text = "남성"
                    } else {
                        tv_gender_my_page.text = "여성"
                    }
                    tv_birth_date_my_page.text = info.birthYear.toString()
                }
            } else {
                getUserInfoFromFirebase(object : UserInfoCallback{
                    override fun onReceiveDataListener(user: Users) {
                        CoroutineScope(Main).launch {
                            setInfoToRoomDB(
                                user.nickname!!,
                                user.gender!!,
                                user.birthYear!!.toInt()
                            )
                            if (user.gender == "male") {
                                tv_gender_my_page.text = "남성"
                            } else {
                                tv_gender_my_page.text = "여성"
                            }
                            tv_birth_date_my_page.text = user.birthYear
                        }
                    }
                })


            }
        }
    }

    private fun setInfoToRoomDB(nickname: String, gender: String, birthYear: Int) {
        val user = User(UID, nickname, gender, birthYear)
        viewModel.insert(user)
    }

    private fun getUserInfoFromFirebase(firebaseCallback: UserInfoCallback) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users/$countryCode/$UID")
        var user: Users? = null
        ref.get().addOnSuccessListener {
            val nickname = it.child("nickname").value.toString()
            val gender = it.child("gender").value.toString()
            val birthYear = it.child("birthYear").value.toString()
            val status = it.child("status").value.toString()
            val registrationToken = "0"
            user = Users(nickname, gender, birthYear, status, registrationToken)
            firebaseCallback.onReceiveDataListener(user!!)
        }.addOnFailureListener {
            user = Users("unknown", "unknown", 0.toString(), "unknown", "unknown")
            Toast.makeText(requireActivity(), R.string.no_internet, Toast.LENGTH_SHORT).show()
            firebaseCallback.onReceiveDataListener(user!!)
        }
    }

    private fun getVersionInfo(): String {
        val info: PackageInfo =
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        val version = info.versionName
        return version
    }

    override fun proceed() {
        signOutFromFirebase()
    }

    interface UserInfoCallback {
        fun onReceiveDataListener(user : Users)
    }
}
package com.sejigner.closest.fragment

import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sejigner.closest.*
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.models.Users
import com.sejigner.closest.room.PaperPlaneDatabase
import com.sejigner.closest.room.PaperPlaneRepository
import com.sejigner.closest.room.User
import com.sejigner.closest.ui.FragmentChatViewModel
import com.sejigner.closest.ui.FragmentChatViewModelFactory
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_my_page.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.sign

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
        val fbDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(
            UID
        ).child("registrationToken")
        fbDatabase.removeValue()
        // SharedPreference 닉네임 값 제거
        App.prefs.myNickname = ""
        // 로그인 페이지 이동
        val intent = Intent(this@MyPageFragment.context, SignInActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun setInfo() {
        val nickname = App.prefs.myNickname!!
        tv_nickname_my_page.text = nickname
        tv_current_version.text = getVersionInfo()
        CoroutineScope(IO).launch {
            if(viewModel.isExists(UID).await()) {
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
                val userInfo = getUserInfoFromFirebase()
                setInfoToRoomDB(userInfo.nickname!!, userInfo.gender!!, userInfo.birthYear!!.toInt())
                CoroutineScope(Main).launch {
                    if (userInfo.gender == "male") {
                        tv_gender_my_page.text = "남성"
                    } else {
                        tv_gender_my_page.text = "여성"
                    }
                    tv_birth_date_my_page.text = userInfo.birthYear
                }
            }
        }
    }

    private fun setInfoToRoomDB(nickname : String, gender : String, birthYear : Int) {
        val user = User(UID, nickname, gender, birthYear)
        viewModel.insert(user)
    }

    private suspend fun getUserInfoFromFirebase() : Users {
        val ref = FirebaseDatabase.getInstance().reference.child("Users/$UID")
        var user : Users ?= null
        CoroutineScope(IO).launch {
            ref.get().addOnSuccessListener {
                val nickname = it.child("nickname").value.toString()
                val gender = it.child("gender").value.toString()
                val birthYear = it.child("birthYear").value.toString()
                val status = it.child("status").value.toString()
                val registrationToken = "0"
                user = Users(nickname, gender, birthYear, status, registrationToken)
            }.addOnFailureListener {
                user = Users("unknown", "unknown", 0.toString(),"unknown", "unknown")
            }.await()
        }.join()
        return user!!
    }

    private fun getVersionInfo() : String {
        val info: PackageInfo = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
        val version = info.versionName
        return version
    }

    override fun proceed() {
        signOutFromFirebase()
    }
}
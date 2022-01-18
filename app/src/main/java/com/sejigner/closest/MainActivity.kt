package com.sejigner.closest

import android.content.Intent
import android.location.*
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.sejigner.closest.adapter.MainViewPagerAdapter
import com.sejigner.closest.fragment.*
import com.sejigner.closest.models.LatestChatMessage
import com.sejigner.closest.models.PaperplaneMessage
import com.sejigner.closest.models.Users
import com.sejigner.closest.room.*
import com.sejigner.closest.ui.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.util.*


// TODO: unread messages indicator
//  1. sharedPreferences에 unreadMessages 변수를 추가
//  2. Listener가 메시지 받으면 받은 갯수 만큼 unreadMessages 플러스
//  3. unreadMessages에 Observer를 달아서 0이 아니면 UI에 표시
//  4. 리사이클러뷰 아이템을 클릭하면 unreadMessage 0 대입

class MainActivity : AppCompatActivity(), FragmentHome.FlightListener,
    FragmentChat.OnCommunicationUpdatedListener,
    FirstDialogFragment.OnSuccessListener, AlertDialogFragment.OnConfirmedListener,
    RepliedDialogFragment.OnChatStartListener, SuspendAlertDialogFragment.OnConfirmedListener,
    SuccessBottomSheet.OnFlightSuccess {

    private var userName: String? = null
    private var fireBaseAuth: FirebaseAuth? = null
    private var fireBaseUser: FirebaseUser? = null
    private var fbFirestore: FirebaseFirestore? = null
    private var fbDatabase: FirebaseDatabase? = null
    private val fragmentHome by lazy { FragmentHome() }
    private val fragmentChat by lazy { FragmentChat() }
    private val fragmentMyPage by lazy { MyPageFragment() }
    private val fragments: List<Fragment> = listOf(fragmentHome, fragmentChat, fragmentMyPage)
    private val pagerAdapter: MainViewPagerAdapter by lazy { MainViewPagerAdapter(this, fragments) }
    private var mInterstitialAd: InterstitialAd? = null
    private var mAdIsLoading: Boolean = false
    lateinit var mRefPlane: DatabaseReference
    lateinit var mRefStatus: DatabaseReference
    lateinit var mRefMessages: DatabaseReference
    lateinit var mListenerPlane: ChildEventListener
    lateinit var mListenerStatus: ChildEventListener
    lateinit var mListenerMessages: ChildEventListener
    lateinit var viewModel: FragmentChatViewModel
    private var isAd = false
    private var isNotification = false
    private var bottomSheet: SuccessBottomSheet? = null
    private lateinit var sendLoadingDialog: SendLoadingDialog


    companion object {
        const val TAG = "MainActivity"
        const val ANONYMOUS = "anonymous"
        var UID = ""
        var MYNICKNAME = ""
        var isOnline = false

        private lateinit var auth: FirebaseAuth

        fun getUid(): String {

            auth = FirebaseAuth.getInstance()

            return auth.currentUser?.uid.toString()
        }


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewPager()
        initNavigationBar()


        // 첫 실행시 광고 실행
        userName = ANONYMOUS

        fireBaseAuth = FirebaseAuth.getInstance()
        fireBaseUser = fireBaseAuth!!.currentUser
        fbDatabase = FirebaseDatabase.getInstance()

        fireBaseAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()
        Log.d(TAG, "got instance from Firestore successfully")
        UID = getUid()

        val repository = PaperPlaneRepository(PaperPlaneDatabase(this))
        val factory = FragmentChatViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[FragmentChatViewModel::class.java]
        isNotification = intent.getBooleanExtra("IS_NOTIFICATION", false)
        isAd = intent.getBooleanExtra("IS_AD", false)
        sendLoadingDialog = SendLoadingDialog(this@MainActivity)

        // 실시간 데이터베이스에 저장된 정보 유무를 통해 개인정보 초기설정 실행 여부 판단
        val uid = getUid()

        MYNICKNAME = App.prefs.myNickname!!
        if (MYNICKNAME.isBlank()) {
            val ref =
                FirebaseDatabase.getInstance().getReference("/Users/$uid")
                    .child("nickname")
            ref.get().addOnSuccessListener {
                App.prefs.myNickname = it.value.toString()
            }
        }

        MobileAds.initialize(this) {}
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder().setTestDeviceIds(
                listOf("ABCDEF012345")
            ).build()
        )




        if (checkGooglePlayServices()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result

                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)


                val ref =
                    FirebaseDatabase.getInstance().getReference("/Users/$uid")
                        .child("registrationToken")
                ref.removeValue()
                // fcm토큰 업로드
                ref.child(token).setValue(true)
            })
        } else {
            Log.w(TAG, "Device doesn't have google play services")
        }

        if (!mAdIsLoading && mInterstitialAd == null) {
            mAdIsLoading = true
            loadAd()
        }
        removePartnerFromPrefs()
    }

    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            isOnline = true
        }

        override fun onLost(network: Network) {
            isOnline = false
        }
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallBack)
    }

    private fun terminateNetworkCallback() {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.unregisterNetworkCallback(networkCallBack)
    }

    override fun onStart() {
        super.onStart()
        mRefPlane = FirebaseDatabase.getInstance().getReference("/PaperPlanes/Receiver/$UID")
        mRefMessages = FirebaseDatabase.getInstance().getReference("/Latest-messages/$UID/")
        mRefStatus = FirebaseDatabase.getInstance().getReference("/Users/$UID")
        listenForPlanes()
        listenForMessages()
        listenForStatus()
    }

    override fun onResume() {
        super.onResume()
//        generateDummy()
        // 노티 푸시 타고 들어왔을 경우 ChatFragment로 swipe
        if (isNotification) {
            vp_main.currentItem = 1
        }
        registerNetworkCallback()
    }

    override fun onStop() {
        super.onStop()
        mRefPlane.removeEventListener(mListenerPlane)
        mRefMessages.removeEventListener(mListenerMessages)
        mRefStatus.removeEventListener(mListenerStatus)
        terminateNetworkCallback()
    }

    private fun loadAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    mAdIsLoading = false
//                    Toast.makeText(this@MainActivity, "onAdLoaded()", Toast.LENGTH_SHORT).show()
                    if (isAd) {
                        showInterstitial()
                        isAd = false
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, adError.message)
                    mInterstitialAd = null
                    mAdIsLoading = false
                }
            }
        )
    }

    override fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    Log.d(TAG, "Ad failed to show.")
                    // Don't forget to set the ad reference to null so you
                    // don't show the ad a second time.
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                    // Called when ad is dismissed.
                }
            }
            mInterstitialAd?.show(this)
        } else {
            Toast.makeText(this, "Ad wasn't loaded.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun confirmFlight() {
        val alertDialog = AlertDialogFragment.newInstance(
            "이대로 비행기를 날릴까요?", "날리기"
        )
        val fm = supportFragmentManager
        alertDialog.show(fm, "flight-confirmation")
    }

    private fun initNavigationBar() {
        bnv_main.run {
            setOnNavigationItemSelectedListener {
                val page = when (it.itemId) {
                    R.id.home -> 0
                    R.id.chat -> 1
                    R.id.my_page -> 2
                    else -> 0
                }

                if (page != vp_main.currentItem) {
                    vp_main.currentItem = page
                }

                true
            }
            selectedItemId = R.id.home
        }
    }

    private fun initViewPager() {
        vp_main.run {
            this.isUserInputEnabled = false
            adapter = pagerAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val navigation = when (position) {
                        0 -> R.id.home
                        1 -> R.id.chat
                        2 -> R.id.my_page
                        else -> R.id.home
                    }

                    if (bnv_main.selectedItemId != navigation) {
                        bnv_main.selectedItemId = navigation
                    }

                }
            })
        }
    }

    override fun onBackPressed() {
        if (vp_main.currentItem == 0) {
            finish()
        } else {
            vp_main.currentItem = 0
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

    override fun showLoadingBottomSheet() {
        bottomSheet = SuccessBottomSheet()
        if (bottomSheet != null) {
            bottomSheet!!.show(supportFragmentManager, SuccessBottomSheet.TAG)
        }
    }

    override fun showLoadingDialog() {
        sendLoadingDialog.show()
    }

    override fun dismissLoadingDialog() {
        sendLoadingDialog.dismiss()
    }

    private fun closeYourDialogFragment() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        val fragmentToRemove = supportFragmentManager.findFragmentByTag("write paper")
        if (fragmentToRemove != null) {
            ft.remove(fragmentToRemove)
        }
        ft.addToBackStack(null)
        ft.commit() // or ft.commitAllowingStateLoss()
    }

    private fun onCommunicationUpdated() {
        val badge = bnv_main.getOrCreateBadge(R.id.chat)
        badge.backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.point)
        badge.isVisible = true
    }

    override fun removeBadge() {
        bnv_main.removeBadge(R.id.chat)
    }

    private fun generateDummy() {
        for (i in 1..10) {
            val item = FirstPaperPlanes(
                i.toString(),
                UID,
                "test $i",
                i * 100.0,
                System.currentTimeMillis() / 1000L
            )
            viewModel.insert(item)
        }

        for (i in 1..20) {
            val item = RepliedPaperPlanes(
                i.toString(),
                UID,
                "test $i",
                "test $i",
                i * 100.0,
                System.currentTimeMillis() / 1000L,
                System.currentTimeMillis() / 1000L
            )
            viewModel.insert(item)
        }

    }

    private fun listenForStatus() {
        mListenerStatus = mRefStatus.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val status = snapshot.value
                Log.d(TAG, "status : $status")
                if (status == "suspended") {
                    confirmSuspend()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun confirmSuspend() {
        val alertDialog = SuspendAlertDialogFragment.newInstance(
            "규정 위반으로 사용 정지된 계정입니다.", "종료하기"
        )
        val fm = supportFragmentManager
        alertDialog.show(fm, "suspend-confirmation")
    }

    private fun listenForPlanes() {
        mListenerPlane = mRefPlane.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val paperplane = snapshot.getValue(PaperplaneMessage::class.java) ?: return
                if (paperplane.id.isNotEmpty()) {
                    onCommunicationUpdated()
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!paperplane.isReplied) { // 상대가 날린 첫 비행기

                            val item = FirstPaperPlanes(
                                paperplane.fromId,
                                UID,
                                paperplane.text,
                                paperplane.flightDistance,
                                paperplane.timestamp
                            )
                            viewModel.insert(item)
                            // immediate delete on setting data to local database
                            mRefPlane.child(paperplane.fromId).removeValue()
                            val acquaintances = Acquaintances(paperplane.fromId, UID)
                            viewModel.insert(acquaintances)
                        } else { // 상대가 날린 답장 비행기
                            setRepliedPaperPlane(paperplane)
                            mRefPlane.child(paperplane.fromId).removeValue()
                        }
                    }
                }


                Log.d(FragmentChat.TAG, "Child added successfully")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun listenForMessages() {
        mListenerMessages = mRefMessages.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val latestChatMessage = snapshot.getValue(LatestChatMessage::class.java) ?: return
                val partnerId = snapshot.key!!
                if (latestChatMessage.recipientId == UID) {
                    onCommunicationUpdated()
                }
                CoroutineScope(IO).launch {
                    val isFinishedChat = (viewModel.isExist(UID, partnerId)).await()
                    if (!isFinishedChat) {
                        val isPartnerId = viewModel.exists(UID, partnerId).await()
                        // 아직 채팅이 시작되지 않아서 채팅방 생성 필요
                        if (!isPartnerId) {
                            var partnerNickname = ""
                            val ref =
                                FirebaseDatabase.getInstance().getReference("/Users/$partnerId")
                                    .child("nickname")
                            ref.get().addOnSuccessListener {
                                partnerNickname = it.value.toString()
                                val chatRoom = ChatRooms(
                                    partnerId,
                                    partnerNickname,
                                    UID,
                                    latestChatMessage.message,
                                    latestChatMessage.time,
                                    false
                                )
                                viewModel.insert(chatRoom)
                                val noticeMessage =
                                    ChatMessages(null, partnerId, UID, 3, getString(R.string.init_chat_log), latestChatMessage.time)
                                viewModel.insert(noticeMessage)
                                mRefMessages.child(snapshot.key!!).removeValue()

                            }.addOnFailureListener {
                                Log.e("MainActivity",it.message.toString())
                            }
                        } else { // 이미 시작된 채팅
                            viewModel.updateLastMessages(
                                UID,
                                partnerId,
                                latestChatMessage.message,
                                latestChatMessage.time
                            ).join()
                            mRefMessages.child(snapshot.key!!).removeValue()
                        }
                    } else {
                        mRefMessages.child(snapshot.key!!).removeValue()
                    }

                }
                Log.d(FragmentChat.TAG, "Child added successfully")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    suspend fun setRepliedPaperPlane(paperPlane: PaperplaneMessage) {

        CoroutineScope(Dispatchers.IO).launch {
            val myPaperPlaneRecord = viewModel.getWithId(UID, paperPlane.fromId).await()
            if (myPaperPlaneRecord != null) {
                val item = RepliedPaperPlanes(
                    myPaperPlaneRecord.partnerId,
                    UID,
                    myPaperPlaneRecord.userMessage,
                    paperPlane.text,
                    paperPlane.flightDistance,
                    myPaperPlaneRecord.firstTimestamp,
                    paperPlane.timestamp
                )
                viewModel.insert(item)
                viewModel.delete(myPaperPlaneRecord)
            }
        }.join()
    }

    override fun proceed() {
        fragmentHome.sendPaperPlane()
    }

    override fun startChatRoom(message: ChatMessages, partnerUid: String) {
        CoroutineScope(IO).launch {
            viewModel.insert(message).join()
            val intent = Intent(this@MainActivity, ChatLogActivity::class.java)
            intent.putExtra(FragmentChat.USER_KEY, partnerUid)
            startActivity(intent)
        }
    }

    private fun removePartnerFromPrefs() {
        App.prefs.setString("partner", "")
    }

    override fun finishApp() {
        finish()
    }

    override fun showReplySuccessFragment(isReply: Boolean, flightDistance: Double) {
        bottomSheet = SuccessBottomSheet()
        bottomSheet!!.show(supportFragmentManager, SuccessBottomSheet.TAG)
    }
}
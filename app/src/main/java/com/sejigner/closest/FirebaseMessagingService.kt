package com.sejigner.closest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val refreshedToken = FirebaseMessaging.getInstance().token
        Log.d(TAG, "Refreshed token: $token")

        val pref = this.getSharedPreferences("token", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString("token", token).apply()
        editor.commit()

        Log.i(TAG, "saved token successfully")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)

        if(remoteMessage.data.isNotEmpty()) {
            Log.i("바디", remoteMessage.data["body"].toString())
            Log.i("타이틀",remoteMessage.data["title"].toString())
            sendNotification(remoteMessage)
        }

        else {
            Log.i("수신에러","data가 비어있습니다. 메시지를 수신하지 못했습니다.")
            Log.i("data값", remoteMessage.data.toString())
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        
    }


    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
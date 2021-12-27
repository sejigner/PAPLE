package com.sejigner.closest.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sejigner.closest.App
import com.sejigner.closest.MainActivity
import com.sejigner.closest.MainActivity.Companion.UID
import com.sejigner.closest.fragment.FragmentHome
import kotlin.random.Random
import androidx.core.app.NotificationManagerCompat
import android.os.FileUtils
import com.sejigner.closest.R


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
                // Handle message within 10 seconds
                sendNotification(remoteMessage)

        }
    }

    private fun sendNotification(messageBody: RemoteMessage) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val channelId = resources.getString(R.string.default_notification_channel_id)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(messageBody.data["title"])
            .setContentText(messageBody.data["body"])
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setShowWhen(true)
        notification.priority = NotificationCompat.PRIORITY_MAX

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setChannelId(channelId)

            val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributeSet = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId, "default", NotificationManager.IMPORTANCE_HIGH
            )

            channel.apply {
                enableLights(true)
                enableVibration(true)
                setSound(ringtoneManager, audioAttributes)
            }

            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notification.build())

//        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
    }


    override fun onNewToken(token: String) {
        val fbDatabase = FirebaseDatabase.getInstance().reference.child("Users").child(UID)
            .child("registrationToken")
        fbDatabase.removeValue()
        fbDatabase.child(token).setValue(true).addOnSuccessListener {
            Log.d(FragmentHome.TAG, "updated fcmToken: $token")
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(
            ADMIN_CHANNEL_ID,
            adminChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.BLUE
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }


    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
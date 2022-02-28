package com.gievenbeck.paple.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.gievenbeck.paple.App.Companion.countryCode
import com.gievenbeck.paple.App.Companion.prefs
import com.gievenbeck.paple.MainActivity
import com.gievenbeck.paple.MainActivity.Companion.UID
import com.gievenbeck.paple.R
import com.gievenbeck.paple.fragment.FragmentHome
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val ADMIN_CHANNEL_ID = "admin_channel"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val isNotification = prefs.getBoolean("isNotification", true)
        val isDailyTopic = prefs.getBoolean("isDailyTopic", true)
            if (isNotification&&!remoteMessage.data["sender"].isNullOrEmpty()) {
                    val sender = remoteMessage.data["sender"]
                    val currentChatPartner = prefs.getString("partner", "")
                    if (sender != currentChatPartner) {
                        sendNotification(remoteMessage)
                    }
                } else if (isDailyTopic&&!remoteMessage.data["topic"].isNullOrEmpty()) {
                val topic = remoteMessage.data["topic"]
                sendTopicNotification(topic)
            }

    }

    private fun sendTopicNotification(topic: String?) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("IS_NOTIFICATION", true)
        intent.putExtra("IS_AD", false)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                1994,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val channelId = resources.getString(R.string.default_topic_channel_id)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_paple_round)
            .setContentTitle(getString(R.string.topic_notification_title))
            .setContentText(topic)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setShowWhen(true)
        notification.priority = NotificationCompat.PRIORITY_MAX

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                setSound(ringtoneManager, audioAttributeSet)
            }

            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1, notification.build())
    }

    private fun sendNotification(messageBody: RemoteMessage) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("IS_NOTIFICATION", true)
        intent.putExtra("IS_AD", false)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                1994,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val channelId = resources.getString(R.string.default_notification_channel_id)
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_paple_round)
            .setContentTitle(messageBody.data["title"])
            .setContentText(messageBody.data["body"])
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setShowWhen(true)
        notification.priority = NotificationCompat.PRIORITY_MAX

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                setSound(ringtoneManager, audioAttributeSet)
            }

            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notification.build())
    }


    override fun onNewToken(token: String) {
        val fbDatabase =
            FirebaseDatabase.getInstance().reference.child("Users/$countryCode/$UID/registrationToken/")
        fbDatabase.removeValue()
        val ref = FirebaseDatabase.getInstance()
            .getReference("/Users/$countryCode/$UID/registrationToken/")
        ref.child(token).setValue(true).addOnSuccessListener {
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
package com.nacamp.quicktimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

class TimerNotificationHelper(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun createTimerNotificationChannels() {
        val timerRunningChannel = NotificationChannelCompat.Builder(
            TIMER_RUNNING_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
            .setName(TIMER_RUNNING_CHANNEL)
            .setDescription(TIMER_RUNNING_CHANNEL)
            .setSound(null, null)
            .build()

        val timerCompletedChannel = NotificationChannelCompat.Builder(
            TIMER_COMPLETED_CHANNEL,
            NotificationManagerCompat.IMPORTANCE_MAX,
        )
            .setName(TIMER_COMPLETED_CHANNEL)
            .setDescription(TIMER_COMPLETED_CHANNEL)
            .setSound(null, null)
            .build()

        notificationManager.createNotificationChannelsCompat(
            listOf(
                timerRunningChannel,
                timerCompletedChannel,
            ),
        )
    }

    fun getRunningNotificationBuilder(): NotificationCompat.Builder {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, TIMER_RUNNING_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history) // 작은 아이콘 설정
            .setContentTitle("Timer")
            .setContentText("working")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 설정
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 긴급 카테고리 설정
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // android:showOnLockScreen="true"
            .setFullScreenIntent(fullScreenPendingIntent, true) // Full-Screen Intent 설정
            .setContentIntent(fullScreenPendingIntent) // 클릭시 앱이동
    }

    fun getCompletedNotificationBuilder(): NotificationCompat.Builder {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, TIMER_COMPLETED_CHANNEL)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history) // 작은 아이콘 설정
            .setContentTitle("Timer")
            .setContentText("working")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 설정
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 긴급 카테고리 설정
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // android:showOnLockScreen="true"
            .setFullScreenIntent(fullScreenPendingIntent, true) // Full-Screen Intent 설정
            .setContentIntent(fullScreenPendingIntent) // 클릭시 앱이동
    }
}

private const val TIMER_RUNNING_CHANNEL = "timer_running_channel"
private const val TIMER_COMPLETED_CHANNEL = "timer_completed_channel"
const val TIMER_RUNNING_NOTIFICATION_ID = 6
const val TIMER_COMPLETED_NOTIFICATION_ID = 7
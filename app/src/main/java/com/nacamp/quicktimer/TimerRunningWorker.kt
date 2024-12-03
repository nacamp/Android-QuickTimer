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


class TimerRunningWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("jimmy", "doWork success")
            NotificationHelper.createNotificationChannel(
                applicationContext,
                TIMER_RUNNING_CHANNEL,
                "Timer Running",
                "Notifications for running timers"
            )

            val notification = NotificationHelper.getNotificationBuilder(applicationContext, TIMER_RUNNING_CHANNEL).build()

            val foregroundInfo = ForegroundInfo(
                TIMER_RUNNING_NOTIFICATION_ID,
                notification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
            )
            setForeground(foregroundInfo)
            // TODO
//            xxxx.xxxState.collectLatest { state ->
//                if (state.isDone) {
//                    Result.success() // 작업 완료 상태로 리턴
//                } else {
//                    // 시간 변화 상태 전달
//                    xxx.updateTimerServiceNotification(
//                        isPlaying = state.isPlaying,
//                        timeText = state.timeText,
//                    )
//                }
//            }
            awaitCancellation()
//            while (!isStopped) {
//                // 대기 작업
//                delay(1000L)
//            }
//            Result.success()
        } catch (e: CancellationException) {
            Log.d("jimmy", "TimerRunningWorker failure")
//            timerNotificationHelper.removeTimerRunningNotification()
            Result.failure()
        }
    }
}
private const val TIMER_RUNNING_CHANNEL = "timer_running_channel"
private const val TIMER_COMPLETED_CHANNEL = "timer_completed_channel"
const val TIMER_RUNNING_NOTIFICATION_ID = 6

object NotificationHelper {

    fun createNotificationChannel(context: Context, channelId: String, channelName: String, description: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = description
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getNotificationBuilder(context: Context, channelId: String): NotificationCompat.Builder {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, channelId)
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

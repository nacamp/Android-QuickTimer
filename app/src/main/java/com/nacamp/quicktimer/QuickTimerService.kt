package com.nacamp.quicktimer

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuickTimerService () {
//    private val coroutineScope = CoroutineScope(Dispatchers.Main)
//    private val timerHelper = TimerHelper(coroutineScope)
//
//    private val _timeLeft = MutableStateFlow(0L) // 남은 시간
//    val timeLeft = _timeLeft.asStateFlow()
//
//    private val _isRunning = MutableStateFlow(false) // 타이머 실행 상태
//    val isRunning = _isRunning.asStateFlow()
//
//    private val _buttonState = MutableStateFlow("Start")
//    val buttonState = _buttonState.asStateFlow()
//
//    private var totalMillis: Long = 0L
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        totalMillis = intent?.getLongExtra("DURATION_MILLIS", 0L) ?: 0L
//        startForegroundServiceWithTimer()
//        return START_NOT_STICKY
//    }
//
//    override fun onDestroy() {
//        timerHelper.stopTimer()
//        super.onDestroy()
//    }
//
//    private val binder = LocalBinder()
//
//    inner class LocalBinder : Binder() {
//        fun getService(): QuickTimerService = this@QuickTimerService
//    }
//
//    override fun onBind(intent: Intent?): IBinder {
//        return binder
//    }
//
//    fun startTimer() {
//        timerHelper.startTimer(
//            durationMillis = totalMillis,
//            onTick = { remainingTime ->
//                _timeLeft.value = remainingTime // 남은 시간 업데이트
//                updateNotification("Time left: ${remainingTime / 1000} seconds")
//            },
//            onFinish = {
//                _timeLeft.value = 0L
//                _isRunning.value = false
//            }
//        )
//        _isRunning.value = true
//        _buttonState.value = "Running"
//    }
//
//    fun pauseTimer() {
//        timerHelper.pauseTimer()
//        _isRunning.value = false
//        _buttonState.value = "Paused"
//    }
//
//    fun cancelTimer() {
//        timerHelper.stopTimer()
//        _isRunning.value = false
//        _buttonState.value = "Start"
//    }
//
//    fun resetTimer() {
//        _isRunning.value = false
//        _buttonState.value = "Start"
//    }
//
//    private fun startForegroundServiceWithTimer() {
//        val notification = createNotification("Timer is running")
//        startForeground(1, notification)
//        startTimer()
//    }
//
//    private fun stopForegroundService() {
//        stopForeground(true)
//        stopSelf()
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun updateNotification(content: String) {
//        val notification = createNotification(content)
//        NotificationManagerCompat.from(this).notify(1, notification)
//    }
//
//    private fun createNotification(content: String): Notification {
//        val channelId = "TIMER_CHANNEL"
//        val channel = NotificationChannel(channelId, "Timer", NotificationManager.IMPORTANCE_LOW)
//        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
//
//        return Notification.Builder(this, channelId)
//            .setContentTitle("Quick Timer")
//            .setContentText(content)
//            .setSmallIcon(android.R.drawable.ic_notification_overlay)
//            .build()
//    }
}


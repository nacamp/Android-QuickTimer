package com.nacamp.quicktimer

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuickTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private var timerJob: Job? = null
    private val _timeLeft = mutableStateOf(0L)
    val timeLeft: State<Long> get() = _timeLeft

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> get() = _isRunning

    private val _buttonState = mutableStateOf("Start")
    val buttonState: State<String> get() = _buttonState

    private val _selectedMinutes = mutableStateOf(5) // 초기값 설정
    val selectedMinutes: State<Int> get() = _selectedMinutes

    private val _startTime = mutableStateOf<Long?>(null) // 시작 시간 (Epoch milliseconds)
    val startTime: State<Long?> get() = _startTime

    private val _endTime = mutableStateOf<Long?>(null) // 종료 시간 (Epoch milliseconds)
    val endTime: State<Long?> get() = _endTime

    private val totalMillis: Long
        get() = _selectedMinutes.value   * 1000L // 항상 최신 값을 계산

    private val _onTimerFinish = MutableStateFlow(false) // 타이머 완료 이벤트
    val onTimerFinish = _onTimerFinish.asStateFlow()

    fun saveSelectedMinutes(minutes: Int) {
        val sharedPreferences = context.getSharedPreferences("QuickTimerPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("SELECTED_MINUTES", minutes).apply()
        _selectedMinutes.value = minutes
    }

    fun loadSelectedMinutes() {
        val sharedPreferences = context.getSharedPreferences("QuickTimerPrefs", Context.MODE_PRIVATE)
        val savedMinutes = sharedPreferences.getInt("SELECTED_MINUTES", 5) // 기본값은 5
        _selectedMinutes.value = savedMinutes
    }

    private var mediaPlayer: MediaPlayerHelper? = null
    init {
        loadSelectedMinutes()
        mediaPlayer = MediaPlayerHelper(context)
        mediaPlayer?.prepare()
    }

    fun updateSelectedMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return
        saveSelectedMinutes(_selectedMinutes.value) // 선택한 시간 저장
        _timeLeft.value = if (_timeLeft.value > 0) _timeLeft.value else totalMillis
        if(_timeLeft.value == totalMillis) {
            _startTime.value = System.currentTimeMillis()
        }
        _isRunning.value = true
        _buttonState.value = "Running"
        timerJob = viewModelScope.launch {
            var remainingTime = _timeLeft.value.takeIf { it > 0 } ?: totalMillis // 남은 시간 또는 전체 시간
            while (remainingTime > 0 && isActive) {
                delay(1000L)
                remainingTime -= 1000L
                _timeLeft.value = remainingTime
            }
            if (remainingTime <= 0) {
                _isRunning.value = false
                _buttonState.value = "Start"
                _onTimerFinish.value = true
                _endTime.value = System.currentTimeMillis()
                showFullScreenNotification(context)
                mediaPlayer?.start()
                // 10초 후 알림 중단
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3_000L) // 10초 대기
                    mediaPlayer?.release() // 알림 중단
                }
            }
        }
    }

    fun pauseTimer() {
        _isRunning.value = false
        _buttonState.value = "Paused"
        timerJob?.cancel() // 현재 타이머를 중단
    }

    fun cancelTimer() {
        _isRunning.value = false
        _buttonState.value = "Start"
        _timeLeft.value = totalMillis
        timerJob?.cancel()
        _startTime.value = null // 시작 시간 초기화
        _endTime.value = null // 종료 시간 초기화
    }

    fun resetTimer() {
        _isRunning.value = false
        _buttonState.value = "Start"
        _timeLeft.value = totalMillis
        _onTimerFinish.value = false // 이벤트 초기화
    }

    fun showFullScreenNotification(context: Context) {
        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 알림 채널 생성 (Android 8.0 이상 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "TIMER_CHANNEL",
                "Head-Up Notifications",
                NotificationManager.IMPORTANCE_HIGH // 중요도 설정
            ).apply {
                description = "Channel for Head-Up Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 생성
        val notification = NotificationCompat.Builder(context, "TIMER_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 작은 아이콘 설정
            .setContentTitle("Head-Up Notification")
            .setContentText("This is a sample head-up notification.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 우선순위 설정
            .setCategory(NotificationCompat.CATEGORY_ALARM) // 긴급 카테고리 설정
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // android:showOnLockScreen="true"
            .setFullScreenIntent(fullScreenPendingIntent, true) // Full-Screen Intent 설정
            .setContentIntent(fullScreenPendingIntent) // 클릭시 앱이동
            //.setAutoCancel(true) // 클릭 시 알림 자동 삭제
            .build()

        // 알림 표시
        notificationManager.notify(1, notification)
    }
}
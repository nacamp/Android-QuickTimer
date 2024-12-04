package com.nacamp.quicktimer

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.*
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/*
TODO:
1. 모델에서 나머지
TimerState: 에서만 변수접근
WorkManager: foreground service 사용하기
    : 용도는 타이머가 있는 동장은 포그라운서비스...
2. 타이머는 TimerHelper
 */
data class QuickTimerState(
    val timeLeft: Long = 0L,
    val isRunning: Boolean = false,
    val isDone: Boolean = true,
    val selectedMinutes: Int = 5,
    val totalMillis: Long = 0L,
)

class QuickTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val _context = application.applicationContext
    private val _workManager = WorkManager.getInstance(application)
    private val _coroutineScope = CoroutineScope(Dispatchers.Default)
    private var _timerHelper: TimerHelper? = null
    private val _timeLeft = MutableStateFlow(0L)
    private val _isRunning = MutableStateFlow(false)
    private val _isDone = MutableStateFlow(true)
    private val _selectedMinutes = MutableStateFlow(5) // 초기값 설정
    private var _mediaPlayer: MediaPlayerHelper? = null

    private val _totalMillis: Long
        get() = _selectedMinutes.value   * 1000L // 항상 최신 값을 계산

    val uiState: StateFlow<QuickTimerState> = combine(
        _timeLeft,
        _isRunning,
        _isDone,
        _selectedMinutes,
    ) { timeLeft, isRunning, isDone, selectedMinutes->
        QuickTimerState(
            timeLeft = timeLeft,
            isRunning = isRunning,
            isDone = isDone,
            selectedMinutes = selectedMinutes,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        QuickTimerState()
    )

    fun saveSelectedMinutes(minutes: Int) {
        val sharedPreferences = _context.getSharedPreferences("QuickTimerPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt("SELECTED_MINUTES", minutes).apply()
        _selectedMinutes.value = minutes
    }

    fun loadSelectedMinutes() {
        val sharedPreferences = _context.getSharedPreferences("QuickTimerPrefs", Context.MODE_PRIVATE)
        val savedMinutes = sharedPreferences.getInt("SELECTED_MINUTES", 5) // 기본값은 5
        _selectedMinutes.value = savedMinutes
    }


    init {
        loadSelectedMinutes()
        _mediaPlayer = MediaPlayerHelper(_context)
        _mediaPlayer?.prepare()
    }

    fun updateSelectedMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
    }

    fun setTimerHelper() {
        _timerHelper = object : TimerHelper(_coroutineScope, _totalMillis, 1000){
            override fun onTick(remainingTime: Long) {
                _timeLeft.value = remainingTime
            }
            override fun onFinish() {
                Log.d("jimmy", "xxxxxxx")
                _timeLeft.value = 0L
                _isRunning.value = false
                _isDone.value = true
                showFullScreenNotification(_context)
                _mediaPlayer?.start()
                // 10초 후 알림 중단
                CoroutineScope(Dispatchers.Main).launch {
                    delay(3_000L) // 10초 대기
                    _mediaPlayer?.pause() // 알림 중단
                    cancelWorker("TIMER_RUNNING_WORKER_TAG")
                }

            }
        }
    }

    fun startTimer() {
        setTimerHelper()
        _timerHelper?.startTimer()
        _isRunning.value = true
        _isDone.value = false
        saveSelectedMinutes(_selectedMinutes.value) // 선택한 시간 저장
        //_workManager.enqueue(OneTimeWorkRequest.from(TimerRunningWorker::class.java))
        val workRequest = OneTimeWorkRequestBuilder<TimerRunningWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // WorkManager 작업을 즉시 실행
            .addTag("TIMER_RUNNING_WORKER_TAG") // 태그 지정
            .build()
        _workManager.enqueue(workRequest)
    }

    fun cancelWorker(tag: String) {
        _workManager.cancelAllWorkByTag(tag)
    }
    fun restartTimer() {
        _timerHelper?.startTimer()
        _isRunning.value = true
    }

    fun pauseTimer() {
        _timerHelper?.pauseTimer()
        _isRunning.value = false
    }

    fun cancelTimer() {
        _timerHelper?.stopTimer()
        _isRunning.value = false
        _isDone.value = true
    }

    fun resetTimer() {
//        _isRunning.value = false
//        _timeLeft.value = totalMillis
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
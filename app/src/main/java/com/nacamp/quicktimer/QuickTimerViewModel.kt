package com.nacamp.quicktimer

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuickTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private var serviceBound = false
    private var serviceConnection: ServiceConnection? = null
    private var timerService: QuickTimerService? = null

    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _buttonState = MutableStateFlow("Start")
    val buttonState = _buttonState.asStateFlow()

    private val timerHelper = TimerHelper(viewModelScope)
    private var timerJob: Job? = null

    private val _selectedMinutes = mutableStateOf(5) // 초기값 설정
    val selectedMinutes: State<Int> get() = _selectedMinutes

    private val _startTime = mutableStateOf<Long?>(null) // 시작 시간 (Epoch milliseconds)
    val startTime: State<Long?> get() = _startTime

    private val _endTime = mutableStateOf<Long?>(null) // 종료 시간 (Epoch milliseconds)
    val endTime: State<Long?> get() = _endTime

    private val totalMillis: Long
        get() = _selectedMinutes.value * 60 * 1000L // 항상 최신 값을 계산

    private val _onTimerFinish = MutableStateFlow(false) // 타이머 완료 이벤트
    val onTimerFinish = _onTimerFinish.asStateFlow()

    fun updateSelectedMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
    }

    fun startTimer() {
        if (!serviceBound) {
            Intent(context, QuickTimerService::class.java).apply {
                putExtra("DURATION_MILLIS", totalMillis)
                context.startForegroundService(this)
            }
            bindToService{
                _startTime.value = System.currentTimeMillis()
                timerService?.startTimer()
            }
        }else{
            _startTime.value = System.currentTimeMillis()
            timerService?.startTimer()
        }
    }

    fun pauseTimer() {
        timerService?.pauseTimer()
    }

    fun cancelTimer() {
        timerService?.cancelTimer()
    }

    fun resetTimer() {
        timerService?.resetTimer()
    }

    private fun bindToService(onConnected: () -> Unit) {
        if (!serviceBound) {
            Log.d("QuickTimerViewModel", "bindToService")
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder = service as QuickTimerService.LocalBinder
                    timerService = binder.getService()
                    serviceBound = true
                    onConnected()

                    viewModelScope.launch {
                        timerService?.timeLeft?.collect { remainingTime ->
                            _timeLeft.value = remainingTime
                        }
                    }

                    viewModelScope.launch {
                        timerService?.isRunning?.collect { runningState ->
                            _isRunning.value = runningState
                        }
                    }

                    viewModelScope.launch {
                        timerService?.buttonState?.collect { state ->
                            _buttonState.value = state
                        }
                    }


                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    serviceBound = false
                }
            }
            context.bindService(Intent(context, QuickTimerService::class.java), connection, Context.BIND_AUTO_CREATE)
            serviceConnection = connection
        }
    }

    private fun unbindFromService() {
        if (serviceBound) {
            context.unbindService(serviceConnection!!)
            serviceBound = false
        }
    }
}
package com.nacamp.quicktimer

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class QuickTimerViewModel : ViewModel() {
    private var timerJob: Job? = null
    private val _timeLeft = mutableStateOf(0L)
    val timeLeft: State<Long> get() = _timeLeft

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> get() = _isRunning

    private val _buttonState = mutableStateOf("Start")
    val buttonState: State<String> get() = _buttonState

    private val _selectedMinutes = mutableStateOf(5) // 초기값 설정
    val selectedMinutes: State<Int> get() = _selectedMinutes

    private val totalMillis: Long
        get() = _selectedMinutes.value * 1000L // 항상 최신 값을 계산

    private val _onTimerFinish = MutableStateFlow(false) // 타이머 완료 이벤트
    val onTimerFinish = _onTimerFinish.asStateFlow()

    fun updateSelectedMinutes(minutes: Int) {
        _selectedMinutes.value = minutes
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return
        _timeLeft.value = if (_timeLeft.value > 0) _timeLeft.value else totalMillis
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
    }

    fun resetTimer() {
        _isRunning.value = false
        _buttonState.value = "Start"
        _timeLeft.value = totalMillis
        _onTimerFinish.value = false // 이벤트 초기화
    }
}
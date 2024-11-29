package com.nacamp.quicktimer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerHelper(
    private val coroutineScope: CoroutineScope,
    private val intervalMillis: Long = 1000L
) {
    private var timerJob: Job? = null
    private var remainingTime: Long = 0L

    fun startTimer(
        durationMillis: Long,
        onTick: (remainingTime: Long) -> Unit,
        onFinish: () -> Unit
    ) {
        //stopTimer() // 기존 타이머 중지
        if (timerJob?.isActive == true) return
        Log.d("TimerHelper", "111remainingTime: $remainingTime")
        if (remainingTime == 0L) {
            remainingTime = durationMillis
        }

        timerJob = coroutineScope.launch {
            //var remainingTime = durationMillis
            while (remainingTime > 0) {
                onTick(remainingTime)
                delay(intervalMillis)
                remainingTime -= intervalMillis
                Log.d("TimerHelper", "remainingTime: $remainingTime")
            }
            onFinish()
            resetTimer()
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        remainingTime = 0L
    }

    fun isTimerRunning(): Boolean {
        return timerJob?.isActive == true
    }

    fun getRemainingTime(): Long {
        return remainingTime
    }

    private fun resetTimer() {
        remainingTime = 0L
    }
}
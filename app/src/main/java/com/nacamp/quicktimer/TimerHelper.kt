package com.nacamp.quicktimer

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class TimerHelper(
    private val coroutineScope: CoroutineScope,
    private val durationMillis: Long ,
    private val intervalMillis: Long = 1000L
) {
    private var timerJob: Job? = null
    private var remainingTime: Long = 0L

    init {
        remainingTime = durationMillis
    }

    fun startTimer() {
        if (timerJob?.isActive == true) return
        timerJob = coroutineScope.launch {
            while (remainingTime > 0) {
                onTick(remainingTime)
                delay(intervalMillis)
                remainingTime -= intervalMillis
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

    abstract fun onTick(remainingTime: Long)
    abstract fun onFinish()
}
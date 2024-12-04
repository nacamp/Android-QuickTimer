package com.nacamp.quicktimer

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class TimerWorkerFactory(
    private val timerNotificationHelper: TimerNotificationHelper
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        Log.d("jimmy", "createWorker")
        return when (workerClassName) {
            TimerRunningWorker::class.java.name -> TimerRunningWorker(
                appContext,
                workerParameters,
                timerNotificationHelper,
            )
            else -> null
        }
    }
}
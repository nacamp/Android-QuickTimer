package com.nacamp.quicktimer

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.work.Configuration
import androidx.work.WorkManager
import com.nacamp.quicktimer.ui.theme.QuickTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeWorkManager()
        showWhenLockedAndTurnScreenOn()
        enableEdgeToEdge()
        setContent {
            QuickTimerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QuickTimerScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun initializeWorkManager() {
        val timerNotificationHelper = TimerNotificationHelper(applicationContext)
        val timerWorkerFactory = TimerWorkerFactory(timerNotificationHelper)

        val configuration = Configuration.Builder()
            .setWorkerFactory(timerWorkerFactory)
            .build()

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(applicationContext, configuration)
       }
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}
package com.nacamp.quicktimer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import kotlinx.coroutines.Job

class MediaPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var job: Job? = null // 10초 타이머를 위한 Job


    fun prepare() {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build(),
            )
            isLooping = true
            setDataSource(context, Settings.System.DEFAULT_ALARM_ALERT_URI)
            prepare()
        }
// 권한 필요
//        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val vibratorManager =
//                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//            vibratorManager.defaultVibrator
//        } else {
//            @Suppress("DEPRECATION")
//            context.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
//        }
    }

    fun start() {
        mediaPlayer?.start()
        val pattern = longArrayOf(0, 500, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            vibrator?.vibrate(pattern, 0)
        }
    }

    fun release() {
        mediaPlayer?.release()
        vibrator?.cancel()
        mediaPlayer = null
        vibrator = null
    }
}
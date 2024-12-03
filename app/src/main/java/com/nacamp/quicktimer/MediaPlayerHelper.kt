package com.nacamp.quicktimer

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.provider.Settings

class MediaPlayerHelper(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPaused: Boolean = false // 재생 상태 추적

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
    }

    fun start() {
        //잠시 comment
//        if (isPaused) {
////            // TODO: 나중에 수정
////            mediaPlayer?.setVolume(0.01F, 0.01F)
//            mediaPlayer?.start()
//            isPaused = false
//        } else {
//            mediaPlayer?.start()
//        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPaused = true
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
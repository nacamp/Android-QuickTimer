package com.nacamp.quicktimer

import android.os.CountDownTimer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import android.media.Ringtone
import android.media.RingtoneManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

fun playNotificationSound(context: Context) {
    // 기본 알림 소리 URI 가져오기
    val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)//.TYPE_ALARM)

    // Ringtone 객체 생성 및 알림 소리 재생
    val ringtone: Ringtone = RingtoneManager.getRingtone(context, notification)
    ringtone.play()
    //ringtone.stop()
}

suspend fun startCoroutineTimer(
    durationMillis: Long,
    intervalMillis: Long,
    onTick: (Long) -> Unit,
    onFinish: () -> Unit
) {
    var remainingTime = durationMillis
    while (remainingTime > 0) {
        onTick(remainingTime)
        delay(intervalMillis)
        remainingTime -= intervalMillis
    }
    onFinish()
}

@Composable
fun QuickTimerScreen(modifier: Modifier = Modifier) {
    var selectedMinutes by remember { mutableStateOf(5) }
    var timeLeft by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(1f) }
    var buttonText by remember { mutableStateOf("Start") }

    var context = LocalContext.current
    // 타이머 시작
    val totalMillis = selectedMinutes  * 1000L // 전체 시간 (1분 단위)

    fun startTimer() {
        isRunning = true
        buttonText = "Stop"
    }

    // 타이머 중지
    fun stopTimer() {
        isRunning = false
        buttonText = "Start"
    }

    if (isRunning) {
        LaunchedEffect(Unit) {
            startCoroutineTimer(
                durationMillis = totalMillis,
                intervalMillis = 1000L, // 1초 간격
                onTick = { millisUntilFinished ->
                    timeLeft = millisUntilFinished
                    progress = 1f - millisUntilFinished.toFloat() / totalMillis
                },
                onFinish = {
                    timeLeft = 0
                    progress = 1f
                    isRunning = false
                    buttonText = "Restart"
                    playNotificationSound(context)
                }
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 첫 번째 Row: 드롭다운 메뉴로 분 선택
        if (!isRunning) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Select Minutes", fontSize = 18.sp)
                //DropdownMenu(selectedMinutes, onMinutesSelected = { selectedMinutes = it })
            }
        } else {
            // 타이머가 동작 중일 때는 선택할 수 없고 숫자가 줄어듬
            Text(
                text = "Minutes: ${selectedMinutes}",
                fontSize = 24.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 두 번째 Row: 원형 타이머
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    color = Color.LightGray,
                    style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                )
//                Log.d("progress", drawContext.size.toString())
//                Log.d("progress", 200.dp.toPx().toString())
                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xff63C6C4), Color(0xff97CA49)
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    ),
//                    color = Color.Blue,
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    size = drawContext.size,
                    style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "${(timeLeft / 1000 / 60).toInt()} : ${(timeLeft / 1000 % 60).toInt()}",
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 세 번째 Row: 시작/정지/종료 버튼
        Button(
            onClick = {
                if (!isRunning) {
                    startTimer()
                } else {
                    stopTimer()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}

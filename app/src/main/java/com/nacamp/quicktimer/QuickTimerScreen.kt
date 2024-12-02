package com.nacamp.quicktimer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.media.Ringtone
import android.media.RingtoneManager
import android.content.Context
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import com.chargemap.compose.numberpicker.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun playNotificationSound(context: Context) {
    // 기본 알림 소리 URI 가져오기
    val notification: Uri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)//.TYPE_ALARM)

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
    var timeLeft by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(1f) }
    var buttonState by remember { mutableStateOf("Start") } // 버튼 상태 관리
    var selectedMinutes by remember { mutableStateOf(1) }

    val context = LocalContext.current
    val totalMillis = selectedMinutes * 60 * 1000L // 전체 시간 (1분 단위)

    val coroutineScope = rememberCoroutineScope()
    var timerJob by remember { mutableStateOf<Job?>(null) } // Coroutine 타이머 작업 추적

    fun startTimer() {
        isRunning = true
        buttonState = "Running" // Start 이후 Cancel, Pause로 전환
        timeLeft = if (timeLeft > 0) timeLeft else totalMillis // 이전 시간 또는 전체 시간
        timerJob = coroutineScope.launch {
            startCoroutineTimer(
                durationMillis = timeLeft,
                intervalMillis = 1000L,
                onTick = { millisUntilFinished ->
                    timeLeft = millisUntilFinished
                    progress = 1f - millisUntilFinished.toFloat() / totalMillis
                },
                onFinish = {
                    timeLeft = 0
                    progress = 1f
                    isRunning = false
                    buttonState = "Start"
                    playNotificationSound(context)
                }
            )
        }
    }

    fun pauseTimer() {
        isRunning = false
        buttonState = "Restart"
        timerJob?.cancel() // 타이머 정지
    }

    fun cancelTimer() {
        isRunning = false
        buttonState = "Start"
        timeLeft = 0L
        progress = 0f
        timerJob?.cancel() // 타이머 정지 및 초기화
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp)) // 원과 버튼 사이 간격
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                //.background(Color.Red)
                .aspectRatio(1f) // 가로/세로 비율을 1:1로 설정
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val diameter = size.minDimension // 정사각형의 최소 크기
                val strokeWidth = 12.dp.toPx()

                drawCircle(
                    color = Color.LightGray,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    radius = diameter / 2, // - strokeWidth / 2,
                    center = size.center
                )

                drawArc(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xff63C6C4), Color(0xff97CA49)
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    ),
                    startAngle = -90f,
                    sweepAngle = 360 * progress,
                    useCenter = false,
                    size = Size(diameter, diameter), // 정사각형 크기 설정
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
//                    topLeft = Offset(
//                        (size.width - diameter) / 2, // 가로 중심
//                        (size.height - diameter) / 2 // 세로 중심
//                    )
                )
            }
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isRunning || buttonState == "Restart") {
                    // 중앙 텍스트
                    Text(
                        text = "${(timeLeft / 1000 / 60).toInt()} : ${(timeLeft / 1000 % 60).toInt()}",
                        fontSize = 24.sp
                    )
                } else {
                    NumberPicker(
                        value = selectedMinutes,
                        range = 0..90,
                        onValueChange = {
                            selectedMinutes = it
                        }
                    )
                    Text(text = "Min")
                }
                Spacer(modifier = Modifier.width(10.dp))
                when (buttonState) {
                    "Start" -> Button(
                        onClick = { startTimer() }
                    ) {
                        Text("Start")
                    }

                    "Running" -> {
                        Button(
                            onClick = { pauseTimer() }
                        ) {
                            Text("Pause")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { cancelTimer() }
                        ) {
                            Text("Cancel")
                        }
                    }

                    "Restart" -> {
                        Button(
                            onClick = { startTimer() }
                        ) {
                            Text("Restart")
                        }
                        Button(
                            onClick = { cancelTimer() }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp)) // 원과 버튼 사이 간격
    }
}



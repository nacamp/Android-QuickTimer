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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chargemap.compose.numberpicker.*

fun playNotificationSound(context: Context) {
    // 기본 알림 소리 URI 가져오기
    val notification: Uri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)//.TYPE_ALARM)

    // Ringtone 객체 생성 및 알림 소리 재생
    val ringtone: Ringtone = RingtoneManager.getRingtone(context, notification)
    ringtone.play()
    //ringtone.stop()
}

@Composable
fun QuickTimerScreen(modifier: Modifier = Modifier, viewModel: QuickTimerViewModel = viewModel()) {
    val timeLeft by viewModel.timeLeft
    val isRunning by viewModel.isRunning
    val buttonState by viewModel.buttonState
    val selectedMinutes by viewModel.selectedMinutes
    val progress by remember { derivedStateOf { 1f - timeLeft.toFloat() / (selectedMinutes * 1000L) } }
    val onTimerFinish by viewModel.onTimerFinish.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(onTimerFinish) {
        if (onTimerFinish) {
            playNotificationSound(context)
            viewModel.resetTimer() // 알람 후 타이머 초기화
        }
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
                if (isRunning || buttonState == "Paused") {
                    // 중앙 텍스트
                    Text(
                        text = "${(timeLeft / 1000 / 60).toInt()} : ${(timeLeft / 1000 % 60).toInt()}",
                        fontSize = 24.sp
                    )
                } else {
                    NumberPicker(
                        value = selectedMinutes,
                        range = 0..90,
                        onValueChange = { newMinutes ->
                            viewModel.updateSelectedMinutes(newMinutes)
                        }
                    )
                    Text(text = "Min")
                }
                Spacer(modifier = Modifier.width(10.dp))
                when (buttonState) {
                    "Start" -> Button(
                        onClick = { viewModel.startTimer() }
                    ) {
                        Text("Start")
                    }

                    "Running" -> {
                        Button(
                            onClick = { viewModel.pauseTimer() }
                        ) {
                            Text("Pause")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.cancelTimer() }
                        ) {
                            Text("Cancel")
                        }
                    }

                    "Paused" -> {
                        Button(
                            onClick = { viewModel.startTimer() }
                        ) {
                            Text("Restart")
                        }
                        Button(
                            onClick = { viewModel.cancelTimer() }
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


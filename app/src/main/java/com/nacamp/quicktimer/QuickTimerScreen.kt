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
import android.content.Context
import android.content.Intent
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.provider.Settings
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun requestOverlayPermission(context: Context) {
    if (!Settings.canDrawOverlays(context)) {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }
}

@Composable
fun QuickTimerScreen(modifier: Modifier = Modifier, viewModel: QuickTimerViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val timeLeft by viewModel.timeLeft
//    val isRunning by viewModel.isRunning
//    val buttonState by viewModel.buttonState
//    val selectedMinutes by viewModel.selectedMinutes
    val progress by remember { derivedStateOf { 1f - uiState.timeLeft.toFloat() / (uiState.selectedMinutes  * 1000L) } }
//    val startTime by viewModel.startTime
//    val endTime by viewModel.endTime
//    val onTimerFinish by viewModel.onTimerFinish.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (!Settings.canDrawOverlays(context)) {
            requestOverlayPermission(context) // 권한 요청
        }
    }


//    val elapsedTime = remember(startTime, endTime) {
//        startTime?.let { start ->
//            endTime?.let { end ->
//                end - start
//            }
//        }
//    }


//    LaunchedEffect(uiState.onTimerFinish) {
//        if (uiState.onTimerFinish) {
//            //playNotificationSound(context)
//            viewModel.resetTimer() // 알람 후 타이머 초기화
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

//        // 시작 시간, 종료 시간, 차이 표시
//        Text(
//            text = "Start Time: ${startTime?.let { SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(
//                Date(it)
//            ) } ?: "Not Started"}",
//            fontSize = 16.sp
//        )
//        Text(
//            text = "End Time: ${endTime?.let { SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(it)) } ?: "Not Ended"}",
//            fontSize = 16.sp
//        )
//        Text(
//            text = "Elapsed Time: ${
//                elapsedTime?.let { millis ->
//                    String.format("%02d:%02d:%02d", millis / 3600000, (millis % 3600000) / 60000, (millis % 60000) / 1000)
//                } ?: "N/A"
//            }",
//            fontSize = 16.sp
//        )

        Spacer(modifier = Modifier.height(16.dp))

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
                if ( uiState.isDone) {
                    NumberPicker(
                        value = uiState.selectedMinutes,
                        range = 0..90,
                        onValueChange = { newMinutes ->
                            viewModel.updateSelectedMinutes(newMinutes)
                        }
                    )
                    Text(text = "Min")

                } else {
                    // 중앙 텍스트
                    Text(
                        text = "${(uiState.timeLeft / 1000 / 60).toInt()} : ${(uiState.timeLeft / 1000 % 60).toInt()}",
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                if(uiState.isRunning){
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
                }else{
                    if(uiState.isDone) {
                        Button(
                            onClick = { viewModel.startTimer() }
                        ) {
                            Text("Start")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.restartTimer() }
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


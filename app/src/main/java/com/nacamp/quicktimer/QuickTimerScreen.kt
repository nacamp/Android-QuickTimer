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

@Composable
fun QuickTimerScreen(modifier: Modifier = Modifier) {
    var selectedMinutes by remember { mutableStateOf(1) }
    var timeLeft by remember { mutableStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(1f) }
    var buttonText by remember { mutableStateOf("Start") }

    var timer: CountDownTimer? by remember { mutableStateOf<CountDownTimer?>(null) }

    fun startTimer(minutes: Int) {
        val totalMillis = minutes * 60 * 1000L
        timeLeft = totalMillis

        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                progress = 1 - millisUntilFinished.toFloat() / totalMillis // 시계방향으로 채워지게 설정
            }

            override fun onFinish() {
                timeLeft = 0
                isRunning = false
                buttonText = "Restart"
                // 알람과 소리 추가 (추후 구현)
            }
        }.start()
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
                DropdownMenu(selectedMinutes, onMinutesSelected = { selectedMinutes = it })
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
                drawArc(
                    color = Color.LightGray,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = true,  // 회색 바탕 전체를 채움
                )
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = 360 * progress, // 시계 방향으로 흰색 채워짐
                    useCenter = true,
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
                    startTimer(selectedMinutes)
                    isRunning = true
                    buttonText = "Stop"
                } else {
                    // 타이머 정지
                    timer?.cancel()
                    isRunning = false
                    buttonText = "Start"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(buttonText)
        }
    }
}

@Composable
fun DropdownMenu(selectedMinutes: Int, onMinutesSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val minutesRange = (1..60).toList()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        // 드롭다운 메뉴를 표시하는 버튼
        Button(onClick = { expanded = true }) {
            Text(text = "$selectedMinutes 분")
        }

        // 드롭다운 메뉴 내용
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            minutesRange.forEach { minute ->
                DropdownMenuItem(
                    text = { Text(text = "$minute 분") },
                    onClick = {
                    onMinutesSelected(minute)  // 선택한 값 전달
                    expanded = false
                })
            }
        }
    }
}

//fun QuickTimerScreen(modifier: Modifier = Modifier) {
//    var minutesInput by remember { mutableStateOf(TextFieldValue("0")) }
//    var timeLeft by remember { mutableStateOf(0L) }
//    var isRunning by remember { mutableStateOf(false) }
//    var progress by remember { mutableStateOf(1f) }
//    var buttonText by remember { mutableStateOf("Start") }
//
//    val timer: CountDownTimer? = remember {
//        null
//    }
//
//    fun startTimer(minutes: Int) {
//        val totalMillis = minutes * 60 * 1000L
//        timeLeft = totalMillis
//
//        object : CountDownTimer(totalMillis, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                timeLeft = millisUntilFinished
//                progress = millisUntilFinished.toFloat() / totalMillis
//            }
//
//            override fun onFinish() {
//                timeLeft = 0
//                isRunning = false
//                buttonText = "Restart"
//                // 알람과 소리 추가 (추후 구현)
//            }
//        }.start()
//    }
//
//    Column(
//        modifier = Modifier.fillMaxSize().padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // 첫 번째 Row: 시간 입력 필드
//        TextField(
//            value = minutesInput,
//            onValueChange = { minutesInput = it },
//            label = { Text("Minutes (Max 60)") },
//            modifier = Modifier.fillMaxWidth(),
//            singleLine = true
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 두 번째 Row: 원형 타이머
//        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
//            Canvas(modifier = Modifier.size(200.dp)) {
//                drawCircle(
//                    color = Color.LightGray,
//                    style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
//                )
//                drawArc(
//                    color = Color.White,
//                    startAngle = -90f,
//                    sweepAngle = 360 * progress,
//                    useCenter = false,
//                    style = Stroke(12.dp.toPx(), cap = StrokeCap.Round)
//                )
//            }
//            Text(
//                text = "${(timeLeft / 1000 / 60).toInt()} : ${(timeLeft / 1000 % 60).toInt()}",
//                fontSize = 24.sp
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 세 번째 Row: 시작/정지/종료 버튼
//        Button(
//            onClick = {
//                if (!isRunning) {
//                    val minutes = minutesInput.text.toIntOrNull()?.coerceIn(0, 60) ?: 0
//                    if (minutes > 0) {
//                        startTimer(minutes)
//                        isRunning = true
//                        buttonText = "Stop"
//                    }
//                } else {
//                    // 타이머 정지
//                    isRunning = false
//                    timer?.cancel()
//                    buttonText = "Start"
//                }
//            },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(buttonText)
//        }
//    }
//}

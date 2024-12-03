
## ㅇㅇㅇ
```
https://developer.android.com/codelabs/android-workmanager?hl=ko#3

```

## 결과 알림
```
풀화면 앱알람
SYSTEM_ALERT_WINDOW
USE_FULL_SCREEN_INTENT

locked 된 화면위에 디스플레이
setShowWhenLocked(true)
setTurnScreenOn(true)

주의: 동일이름의 알림의 속성이 바뀌면 앱을 삭제 후 테스트 혹은 다름 채널이름을 사용하자.
NotificationCompat.Builder(context, "TIMER_CHANNEL")
```
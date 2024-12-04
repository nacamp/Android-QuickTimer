
## WorkManager
```
https://developer.android.com/topic/libraries/architecture/workmanager?hl=ko
https://developer.android.com/codelabs/android-workmanager?hl=ko#3

장기 실행 작업자
https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/long-running?hl=ko

Factory
https://developer.android.com/codelabs/android-adv-workmanager?hl=ko#3

androidx.work.impl.WorkManagerInitializer => androidx.startup.InitializationProvider
https://developer.android.com/develop/background-work/background-tasks/persistent/configuration/custom-configuration
https://stackoverflow.com/questions/64588254/classnotfoundexception-androidx-work-impl-workmanagerinitializer
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

## ListPicker
```
https://gist.github.com/inidamleader/7bcc273afe6b885738556d190582a815
https://github.com/kez-lab/Compose-DateTimePicker
https://github.com/ChargeMap/Compose-NumberPicker
```

## Canvas
```
https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/drawscope/DrawScope
https://velog.io/@cksgodl/AndroidCompose-Compose%EC%97%90%EC%84%9C-Canvas%EB%A5%BC-%EC%82%AC%EC%9A%A9%ED%95%B4%EB%B3%B4%EC%9E%90
```

## reference
```
Clock
https://github.com/yassineAbou/Clock
```
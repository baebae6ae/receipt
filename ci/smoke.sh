#!/bin/bash
# 에뮬레이터에 실제 APK 를 깔고 화면을 눌러 본다 (CI 전용). 사용: ci/smoke.sh app-debug.apk ci-out
set -x
APK=$1
OUT=$2
PKG=com.baebae.receipt
mkdir -p "$OUT"

adb wait-for-device
adb install -r -g "$APK"                               # -g: 걸음·알림 런타임 권한 허용
adb shell appops set $PKG GET_USAGE_STATS allow       # 사용 기록 접근(설정 화면에서 켜는 것과 같음)
adb shell cmd locale set-app-locales $PKG --locales ko-KR

# 실제 사용 기록 만들기: 다른 앱 몇 개를 열었다 닫는다.
for i in 1 2 3; do
  adb shell am start -a android.settings.SETTINGS; sleep 6
  adb shell am start -a android.intent.action.DIAL; sleep 4
  adb shell input keyevent HOME; sleep 2
done

adb logcat -c
adb shell am start -W -n $PKG/.MainActivity
python3 ci/drive.py "$OUT" first || FAILED=1

# 일요일 21시로 시계를 옮겨 발행 경로를 실제로 탄다.
if [ -z "$FAILED" ]; then
  adb root; sleep 3
  adb shell settings put global auto_time 0
  SUNDAY=$(adb shell 'd=$(date +%u); echo $(( (7 - d) % 7 ))' | tr -d '\r')
  TARGET=$(adb shell "date -d @\$(( \$(date +%s) + ${SUNDAY} * 86400 )) +%m%d" | tr -d '\r')
  YEAR=$(adb shell date +%Y | tr -d '\r')
  adb shell "date ${TARGET}2100${YEAR}.00"
  adb shell date
  adb shell am force-stop $PKG
  adb shell am start -W -n $PKG/.MainActivity
  python3 ci/drive.py "$OUT" second || FAILED=1
fi

adb logcat -d > "$OUT/logcat.txt"
adb shell content query --uri content://media/external/images/media --projection _display_name:relative_path > "$OUT/gallery.txt" 2>&1
if grep -E "FATAL EXCEPTION|AndroidRuntime: Process: $PKG" "$OUT/logcat.txt"; then
  echo "CRASH" >> "$OUT/steps.txt"
  exit 1
fi
[ -z "$FAILED" ]

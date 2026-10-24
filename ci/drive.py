#!/usr/bin/env python3
"""에뮬레이터에서 실제 APK 를 화면 텍스트 기준으로 눌러 가며 스크린샷을 남긴다 (CI 전용)."""
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

OUT = sys.argv[1]
PKG = "com.baebae.receipt"
log = open(f"{OUT}/steps.txt", "a", encoding="utf-8")


def adb(*args, check=True):
    return subprocess.run(["adb", *args], capture_output=True, text=True, check=check).stdout


def note(msg):
    print(msg, flush=True)
    log.write(msg + "\n")
    log.flush()


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", check=False)
    xml = adb("exec-out", "cat", "/sdcard/ui.xml", check=False)
    try:
        root = ET.fromstring(xml)
    except ET.ParseError:
        return []
    out = []
    for n in root.iter("node"):
        b = re.findall(r"\d+", n.get("bounds", ""))
        if len(b) == 4:
            x1, y1, x2, y2 = map(int, b)
            out.append((n.get("text", "") or n.get("content-desc", ""), (x1 + x2) // 2, (y1 + y2) // 2))
    return out


def find(text, exact=True):
    for t, x, y in nodes():
        if (t == text) if exact else (text in t):
            return x, y
    return None


def wait(text, timeout=30, exact=True):
    end = time.time() + timeout
    while time.time() < end:
        pos = find(text, exact)
        if pos:
            return pos
        time.sleep(1)
    raise SystemExit(f"FAIL: '{text}' not found within {timeout}s")


def tap(text, timeout=30, exact=True, scroll=False):
    end = time.time() + timeout
    while time.time() < end:
        pos = find(text, exact)
        if pos:
            adb("shell", "input", "tap", str(pos[0]), str(pos[1]))
            note(f"tap '{text}'")
            time.sleep(1.2)
            return
        if scroll:
            swipe_up()
        time.sleep(1)
    shot("zz_fail")
    raise SystemExit(f"FAIL: '{text}' not found within {timeout}s")


def wait_regex(pattern, timeout=30):
    end = time.time() + timeout
    while time.time() < end:
        for t, _, _ in nodes():
            if re.search(pattern, t):
                note(f"found /{pattern}/ in '{t}'")
                return t
        time.sleep(1)
    shot("zz_fail")
    raise SystemExit(f"FAIL: /{pattern}/ not found within {timeout}s")


def close_fullscreen_ad():
    """AdMob 전면/보상형 광고는 BACK 으로 닫히지 않아 X 버튼을 누른다."""
    for _ in range(20):
        for t, x, y in nodes():
            if re.search(r"(?i)close|닫기", t):
                adb("shell", "input", "tap", str(x), str(y))
                note(f"tap ad close '{t}'")
                time.sleep(2)
                return
        time.sleep(1)
    adb("shell", "input", "tap", "1020", "190")  # 우상단 X 위치
    note("tap ad close (fallback)")
    time.sleep(2)


def swipe_up():
    adb("shell", "input", "swipe", "540", "1700", "540", "700", "400")
    time.sleep(0.8)


shots = 0


def shot(name):
    global shots
    shots += 1
    path = f"{OUT}/{shots:02d}_{name}.png"
    with open(path, "wb") as f:
        f.write(subprocess.run(["adb", "exec-out", "screencap", "-p"], capture_output=True, check=True).stdout)
    note(f"shot {path}")


def main():
    stage = sys.argv[2] if len(sys.argv) > 2 else "first"
    if stage == "first":
        # 온보딩 → 질문형 체험
        wait("다음", 60)
        shot("onboarding_intro")
        tap("다음")
        shot("onboarding_privacy")
        tap("질문 5개로 먼저 해보기")
        shot("quiz_q1")
        for answer in ["6시간 이상", "영상", "새벽 2시 이후", "거의 안 걷는다", "손에서 놓지 않는다"]:
            tap(answer)
        time.sleep(2)
        shot("quiz_result")
        tap("저장")
        time.sleep(2)
        shot("quiz_saved_toast")
        tap("진짜 내 데이터로 뽑아보기", scroll=True)
        # 권한 화면 (설치 시 -g, appops 로 모두 허용해 둠)
        wait("권한 설정")
        shot("permissions")
        tap("완료", scroll=True)
        # 홈: 실제 사용 기록으로 계산된 이번 주 카드
        wait("이번 주 성분표")
        time.sleep(8)
        shot("home_live_real_data")
        # 실제 사용 기록 반영 확인: 테스트 전에 연 '설정' 앱이 원재료에 있어야 한다.
        wait_regex(r"설정 [0-9.]+%")
        # 갓 부팅한 에뮬레이터에는 밤잠 기록이 없으므로 '수면'이 원재료에 나오면 안 된다.
        fake = [t for t, _, _ in nodes() if re.search(r"수면 [0-9.]+%", t)]
        if fake:
            shot("zz_fake_sleep")
            raise SystemExit(f"FAIL: sleep appears without any night record: {fake}")
        tap("미리 보기 · 공유", scroll=True)
        time.sleep(3)
        shot("result_live_story")
        tap("정사각 1:1", scroll=True)
        time.sleep(1)
        shot("result_live_square")
        tap("크라프트 · 광고", scroll=True)
        shot("reward_dialog")
        tap("광고 보기")
        time.sleep(12)
        shot("rewarded_test_ad")
        time.sleep(20)  # 테스트 보상형 영상이 끝나 보상이 지급될 때까지
        close_fullscreen_ad()
        time.sleep(2)
        shot("after_reward")
        adb("shell", "input", "keyevent", "BACK")
        time.sleep(2)
    else:
        # 시계를 일요일 21시로 옮긴 뒤: 앱을 열면 밀린 발행이 처리되고 배너가 떠야 한다.
        wait("이번 주 성분표", 60)
        time.sleep(10)
        shot("home_after_publish")
        tap("개봉하기", timeout=40, scroll=True)
        time.sleep(8)
        shot("interstitial_test_ad")
        if not find("공유", exact=True):  # 결과 화면의 공유 버튼
            close_fullscreen_ad()
        wait("공유", 30)
        time.sleep(2)
        shot("published_result")
        adb("shell", "input", "keyevent", "BACK")
        time.sleep(2)
        tap("보관함")
        time.sleep(2)
        shot("archive")
        tap("설정")
        time.sleep(2)
        shot("settings")
    note(f"OK {stage}")


main()

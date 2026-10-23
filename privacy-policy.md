---
title: 나 성분표 개인정보처리방침
---

# 나 성분표 개인정보처리방침

시행일: [YYYY-MM-DD]
문의: [개발자 연락처 이메일]

「나 성분표」(이하 "앱")는 서버를 운영하지 않습니다. 앱이 읽는 휴대폰 사용 기록은 사용자의 기기 안에서만 계산·저장되며, 개발자를 포함한 누구에게도 전송되지 않습니다.

## 1. 앱이 기기 안에서 처리하는 정보

| 정보 | 사용하는 권한 | 용도 |
|---|---|---|
| 앱별 사용 시간, 잠금 해제 횟수, 화면 켜짐·꺼짐 시각 | 사용 기록 접근 (`PACKAGE_USAGE_STATS`) | 성분표의 원재료 함량, 화면 시간, 잠금 해제 횟수, 수면 시간 추정 |
| 설치된 앱의 이름 (런처에 표시되는 앱만) | 없음 (`<queries>` 선언) | 원재료명에 앱 이름 표시 |
| 걸음 센서 누적값 | 신체 활동 (`ACTIVITY_RECOGNITION`) | 걸음 수, '걷기' 함량 |
| 사용자가 입력한 제품명, 숨길 앱 목록, 카드 편집 상태 | 없음 | 카드 표시 |

- 위 정보는 기기 내부 저장소(앱 전용 데이터베이스 및 설정 파일)에만 저장됩니다.
- 앱의 코드는 위 정보를 인터넷으로 전송하지 않습니다.
- 앱 데이터는 기기 백업(`allowBackup`)에서도 제외되어 있습니다.

## 2. 카드 이미지

카드 이미지는 사용자가 **저장** 또는 **공유** 버튼을 누를 때에만 만들어집니다.

- 저장: 기기의 사진 보관함(`Pictures/NaLabel`)에 저장됩니다.
- 공유: 사용자가 고른 앱으로 이미지가 전달되며, 이후의 처리는 해당 앱의 정책을 따릅니다.

## 3. 광고 (Google AdMob)

앱은 무료로 운영되며 Google AdMob 광고를 표시합니다(홈·설정 화면 하단 배너, 새 성분표를 처음 열 때의 전면 광고, 사용자가 선택한 보상형 광고).

- 광고 SDK는 광고 제공, 성과 측정, 부정 행위 방지를 위해 광고 ID 등 기기 식별자, IP 주소(대략적인 위치), 광고와의 상호작용, 기기·앱 진단 정보를 Google에 전송할 수 있습니다.
- 앱은 사용 기록, 걸음 수, 수면 추정, 카드 내용 등 1항의 정보를 광고 SDK에 전달하지 않습니다.
- Google의 데이터 처리 방식: <https://policies.google.com/technologies/partner-sites>
- 광고 ID는 휴대폰 **설정 > Google > 광고**에서 재설정하거나 삭제할 수 있습니다.

## 4. 보관 기간과 삭제

| 정보 | 보관 기간 |
|---|---|
| 일별 사용 요약 | 120일 후 자동 삭제 |
| 걸음 센서 스냅샷 | 60일 후 자동 삭제 |
| 발행된 주간 성분표 | 앱을 삭제할 때까지 |

앱을 삭제하면 기기에 저장된 모든 정보가 함께 삭제됩니다. 갤러리에 저장한 카드 이미지는 사용자가 직접 삭제해야 합니다.

## 5. 권한 철회

- 사용 기록 접근: 휴대폰 **설정 > 사용 기록 접근**(기기마다 '특별한 앱 접근' 아래에 있을 수 있음)
- 신체 활동, 알림: **설정 > 애플리케이션 > 나 성분표 > 권한**

권한을 끄면 해당 항목이 카드에서 빠지며, 앱의 다른 기능은 계속 사용할 수 있습니다.

## 6. 아동

앱은 만 14세 미만 아동을 대상으로 하지 않습니다.

## 7. 변경

이 방침이 바뀌면 앱 업데이트와 이 페이지를 통해 알립니다.

---

# Privacy Policy — My Nutrition Facts

Effective date: [YYYY-MM-DD]
Contact: [developer contact email]

My Nutrition Facts ("the app") has no server. The phone usage data it reads is calculated and stored **only on your device** and is never sent to anyone, including the developer.

## 1. Data processed on your device

| Data | Permission | Purpose |
|---|---|---|
| Per-app screen time, unlocks, screen on/off times | Usage access (`PACKAGE_USAGE_STATS`) | Ingredients, screen time, unlocks, sleep estimate |
| Names of installed apps (launcher apps only) | None (`<queries>` declaration) | App names in the ingredients |
| Step counter value | Physical activity (`ACTIVITY_RECOGNITION`) | Steps, "Walking" ingredient |
| Product name, hidden apps, card edits you set | None | Rendering the card |

This data stays in the app's private storage, is excluded from device backup, and is never transmitted by the app's code.

## 2. Card images

Images are created only when you tap **Save** (stored in `Pictures/NaLabel`) or **Share** (passed to the app you choose, whose own policy then applies).

## 3. Advertising (Google AdMob)

The app shows Google AdMob ads (banner on Home/Settings, an interstitial when opening a newly published label, and rewarded ads you choose to watch). The ads SDK may send device identifiers such as the advertising ID, IP address (approximate location), ad interactions and diagnostics to Google for ad delivery, measurement and fraud prevention. The app never passes the data in section 1 to the ads SDK. See <https://policies.google.com/technologies/partner-sites>. You can reset or delete your advertising ID in **Settings > Google > Ads**.

## 4. Retention and deletion

Daily summaries are deleted after 120 days and step snapshots after 60 days. Published weekly labels stay until you uninstall the app, which deletes all app data. Images you saved to the gallery must be deleted by you.

## 5. Revoking access

Usage access: **Settings > Usage access**. Physical activity and notifications: **Settings > Apps > My Nutrition Facts > Permissions**.

## 6. Children

The app is not directed to children under 14.

## 7. Changes

Changes will be announced through app updates and this page.

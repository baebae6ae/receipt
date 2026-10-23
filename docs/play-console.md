# 플레이 콘솔 제출용 정리

이 문서는 플레이 콘솔의 **데이터 보안(Data safety)** 양식과 권한 관련 설명 작성용 초안입니다.
광고 SDK 항목은 제출 시점의 구글 공식 안내([Google Mobile Ads SDK 데이터 공개](https://developers.google.com/admob/android/privacy/play-data-disclosure))와 반드시 대조하세요. SDK 버전에 따라 항목이 바뀔 수 있습니다.

## 1. 핵심 원칙 (모든 설명에 공통)

> 사용 기록, 걸음 수, 수면 추정은 **기기 내부에서만 처리**하며 **외부로 전송하지 않습니다.** 앱에는 서버가 없습니다.

플레이 정책상 "수집(collected)"은 **기기 밖으로 전송**하는 것을 뜻합니다. 기기 안에서만 처리하는 데이터는 데이터 보안 양식에서 '수집'으로 신고하지 않습니다. 단, 광고 SDK가 전송하는 데이터는 신고 대상입니다.

## 2. 데이터 보안 양식 답변 초안

### 데이터 수집 및 보안

| 질문 | 답변 |
|---|---|
| 앱이 필수 사용자 데이터 유형을 수집하거나 공유하나요? | **예** (광고 SDK 때문) |
| 전송 중 데이터가 암호화되나요? | **예** (AdMob SDK 는 HTTPS 사용) |
| 사용자가 데이터 삭제를 요청할 수 있나요? | 앱 자체는 서버에 데이터를 보관하지 않음. 광고 ID 재설정/삭제 안내로 갈음 (구글 안내 확인 후 답변) |

### 데이터 유형 (광고 SDK 기준 초안)

| 데이터 유형 | 수집 | 공유 | 처리 목적 | 선택 사항? |
|---|---|---|---|---|
| 위치 > 대략적인 위치 (IP 기반) | 예 | 확인 필요 | 광고 또는 마케팅, 분석, 사기 예방·보안·규정 준수 | 아니요 |
| 기기 또는 기타 ID > 광고 ID 등 | 예 | 확인 필요 | 광고 또는 마케팅, 분석, 사기 예방·보안·규정 준수 | 아니요 |
| 앱 활동 > 앱 상호작용 (광고 상호작용) | 예 | 확인 필요 | 광고 또는 마케팅, 분석 | 아니요 |
| 앱 정보 및 성능 > 비정상 종료 로그, 진단 | 예 | 확인 필요 | 분석, 사기 예방·보안·규정 준수 | 아니요 |

'공유' 여부는 구글의 SDK 데이터 공개 문서 기준을 따르세요(서비스 제공자 전송은 공유가 아닐 수 있음).

### 신고하지 않는 것 (기기 내부 처리, 전송 없음)

- 앱 활동 > 기타 사용자 생성 콘텐츠·앱 사용 기록 (UsageStats)
- 건강 및 피트니스 > 피트니스 정보 (걸음 수)
- 설치된 앱 목록

## 3. 권한별 설명 문구

플레이 콘솔의 앱 콘텐츠·스토어 등록정보·심사 메모에 그대로 쓸 수 있는 문장입니다.

### 사용 기록 접근 (`PACKAGE_USAGE_STATS`)

> 이 앱은 매주 사용자의 휴대폰 사용 패턴을 식품 성분표 형태의 이미지로 만들어 주는 앱입니다. 사용 기록 접근 권한은 앱별 사용 시간, 잠금 해제 횟수, 화면이 꺼져 있던 시간(수면 추정)을 계산하는 데에만 사용하며, 모든 계산은 기기 내부에서만 이루어지고 외부로 전송하지 않습니다. 앱에는 서버가 없습니다. 권한은 사용자가 시스템 설정 화면에서 직접 켜며, 켜기 전에도 질문형 체험으로 앱을 사용할 수 있습니다.

(영문) *This app turns the user's weekly phone usage into a food-label style image. Usage access is used only to calculate per-app screen time, unlock count and screen-off time (sleep estimate). All processing happens on the device; nothing is transmitted and the app has no server. The user turns it on in system settings, and the app can be tried without it through a question-based demo.*

### 패키지 공개 상태

`QUERY_ALL_PACKAGES` 를 쓰지 않습니다. manifest `<queries>` 에 `MAIN/LAUNCHER`(앱 이름 표시)와 `MAIN/HOME`(홈 런처를 화면 시간에서 제외)만 선언했습니다. 별도 선언 양식이 필요 없습니다.

### 신체 활동 (`ACTIVITY_RECOGNITION`)

> 휴대폰의 걸음 센서(TYPE_STEP_COUNTER) 누적값을 읽어 하루 걸음 수를 계산합니다. 기기 내부에서만 처리하며 외부로 전송하지 않습니다.

### 알림 (`POST_NOTIFICATIONS`)

> 매주 일요일 저녁 그 주의 성분표가 발행되면 알립니다. 알림 내용에는 사용 기록이 표시되지 않습니다.

### 광고 ID (`com.google.android.gms.permission.AD_ID`)

광고 SDK 가 자동으로 추가합니다. 앱 콘텐츠 > 광고 ID 항목에서 "예, 광고 목적으로 사용"으로 신고합니다.

## 4. 기타 앱 콘텐츠 항목

| 항목 | 답변 |
|---|---|
| 광고 포함 | 예 |
| 타겟층 | 만 18세 이상 권장 (아동 대상 아님) |
| 건강 앱 | 건강 기능을 주장하지 않음. 수면은 '화면 꺼짐 구간 추정'이며 의학적 측정이 아님을 스토어 설명에 명시 |
| 개인정보처리방침 URL | `docs/privacy-policy.md` 를 GitHub Pages 등 무료 정적 호스팅에 게시한 주소 |

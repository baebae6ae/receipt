# 나 성분표 (가칭)

폰 사용 데이터를 기기 안에서만 처리해 매주 "이번 주의 나"를 **식품 성분표 이미지 카드**로 발행하는 안드로이드 앱.
서버·유료 API 없음. 사용자 데이터는 기기 밖으로 나가지 않는다. 수익은 AdMob 광고만.

## 구성

```
app/src/main/
├─ assets/
│  ├─ phrases_ko.json, phrases_en.json   문구 은행 (카테고리 → 조건 그룹 → 문구 10개+)
│  └─ licenses/                          번들 폰트 라이선스 (SIL OFL 1.1)
├─ res/font/                             Pretendard, JetBrains Mono
└─ java/com/baebae/receipt/
   ├─ domain/      순수 로직: WeeklyStats → LabelCard (LabelBuilder), 문구 조건 엔진, 질문형 체험
   ├─ data/        UsageEvents 수집, 걸음 센서, 수면 추정, Room(일별 캐시·주간 카드), 문구 엔진
   ├─ work/        CollectWorker(매시간 스냅샷·캐시·밀린 발행), PublishWorker(일요일 20:00 발행)
   ├─ ads/         AdMob (테스트 ID) 노출 규칙
   ├─ export/      MediaStore 저장, FileProvider 공유
   └─ ui/          card(라벨 컴포저블), onboarding, result, archive, settings, permissions
```

- 문구를 고치려면 `assets/phrases_*.json` 만 수정하면 된다. 그룹 id·문구 순서는 두 언어가 같아야 한다(단위 테스트가 확인).
- 카드 디자인은 `ui/card/LabelCardView.kt`. 카드는 항상 1080px 기준으로 배치되어 화면 미리보기와 저장 이미지가 같다.

## 빌드

Android SDK(platform 36)가 있는 환경에서:

```
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

출시 전 할 일: [docs/release-checklist.md](docs/release-checklist.md) · 플레이 콘솔 양식: [docs/play-console.md](docs/play-console.md) · 개인정보처리방침: [docs/privacy-policy.md](docs/privacy-policy.md)

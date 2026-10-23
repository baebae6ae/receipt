# 출시 체크리스트

## 출시 전 반드시 바꿀 것

- [ ] **applicationId / namespace**: 지금은 임시값 `com.baebae.receipt`. 플레이에 한 번 올리면 바꿀 수 없음 (`app/build.gradle.kts`).
- [ ] **AdMob ID**: `app/build.gradle.kts` 의 `admobAppId`, `AD_UNIT_*` 네 곳을 실제 ID 로 교체.
      개발·내부 테스트 중에는 테스트 ID 유지 (실제 광고를 직접 누르면 계정 정지 위험).
      권장: release 빌드 타입에서만 실제 ID 를 쓰도록 `buildTypes.release` 에 `buildConfigField` 를 따로 둔다.
- [ ] **개인정보처리방침**: `docs/privacy-policy.md` 의 시행일·연락처를 채우고 공개 URL 에 게시.
- [ ] **앱 이름**: `app_name`(ko/en), 워터마크 문구 `card_watermark`, 갤러리 폴더 `gallery_folder`.

## 서명·빌드

- [ ] 업로드 키 생성, Play App Signing 사용
- [ ] `./gradlew bundleRelease` (R8 축소 켜져 있음) 후 실제 기기에서 release 빌드 동작 확인
- [ ] Room 스키마(`app/schemas/`)를 커밋해 두고, 이후 DB 변경 시 마이그레이션 작성

## 실기기 확인 (이 저장소의 코드는 Android SDK 없이 작성·검증됨)

- [ ] Android 9 / 10 / 14 / 15 이상에서 사용 기록 권한 화면 이동, 걸음 권한, 알림 권한
- [ ] 제조사별(삼성, 샤오미 등) 사용 기록 접근 설정 화면 진입
- [ ] 백그라운드 걸음 스냅샷: 기기에 따라 백그라운드 센서 이벤트가 오지 않을 수 있음.
      그 경우 앱을 열 때 남는 스냅샷만으로 계산됨(합계는 맞고, 날짜별 배분만 거칠어짐).
      부족하면 대안: 포그라운드 서비스(알림 필요) 또는 Google Play 서비스의 Recording API.
- [ ] 일요일 20:00 발행 알림 (Doze 로 지연될 수 있음, 앱을 열면 즉시 보충 발행)
- [ ] 저장 이미지가 1080×1920 / 1080×1080 인지, 갤러리 `Pictures/NaLabel` 에 보이는지
- [ ] 인스타그램 스토리 공유 시트 동작

## 플레이 콘솔

- [ ] 개인 개발자 계정: **테스터 12명 이상 × 14일 연속** 비공개 테스트 후 프로덕션 신청 가능
- [ ] 데이터 보안 양식, 권한 설명: `docs/play-console.md`
- [ ] 광고 포함 표시, 광고 ID 신고
- [ ] 스토어 설명에 "모든 기록은 기기 안에서만 처리, 외부 전송 없음" 명시

## 글로벌(영어권) 확장 시

- [ ] **EEA·영국·스위스 사용자 동의**: AdMob 은 이 지역에 구글 인증 CMP 동의 메시지가 필요.
      Google UMP SDK(`com.google.android.ump:user-messaging-platform`) 연동 후 동의 전에는 광고 요청 금지.
      한국만 배포하는 동안에는 국가 제한으로 대응 가능.
- [ ] 영어 문구 은행(`assets/phrases_en.json`) 원어민 검수

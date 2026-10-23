# 출시 체크리스트

## 출시 전 반드시 바꿀 것

- [ ] **applicationId / namespace**: 지금은 임시값 `com.baebae.receipt`. 플레이에 한 번 올리면 바꿀 수 없음 (`app/build.gradle.kts`).
- [ ] **AdMob ID**: `app/build.gradle.kts` 의 `admobAppId`, `AD_UNIT_*` 네 곳을 실제 ID 로 교체.
      개발·내부 테스트 중에는 테스트 ID 유지 (실제 광고를 직접 누르면 계정 정지 위험).
      권장: release 빌드 타입에서만 실제 ID 를 쓰도록 `buildTypes.release` 에 `buildConfigField` 를 따로 둔다.
- [ ] **개인정보처리방침**: `docs/privacy-policy.md` 의 시행일·연락처를 채우고 공개 URL 에 게시.
- [ ] **앱 이름**: `app_name`(ko/en), 워터마크 문구 `card_watermark`, 갤러리 폴더 `gallery_folder`.

## 서명·빌드 (AAB)

플레이 스토어에는 **AAB** 만 올릴 수 있다. GitHub Actions(`Android build`)가 push 마다 `app-release.aab` 를 만든다.
업로드 키가 등록돼 있지 않으면 debug 키로 서명돼 아티팩트 이름이 `nalabel-release-aab-debug-NOT-for-upload` 로 나오고, 이 파일은 플레이 콘솔이 거부한다.

### 업로드 키 만들기 (한 번만, 본인 컴퓨터에서)

- [ ] 키 만들기. JDK 가 있는 컴퓨터에서 (Android Studio 를 설치했다면 Build → Generate Signed App Bundle → Create new 로도 가능):
      ```
      keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
      ```
      비밀번호 두 개(키 저장소, 키)를 정하게 된다. **`upload.jks` 와 비밀번호는 저장소에 절대 올리지 말고 따로 백업.**
- [ ] 키 파일을 base64 텍스트로 바꾸기
      - Mac: `base64 -i upload.jks | pbcopy`
      - Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload.jks")) | Set-Clipboard`
- [ ] GitHub 저장소 → Settings → Secrets and variables → Actions → New repository secret 으로 4개 등록
      | 이름 | 값 |
      |---|---|
      | `UPLOAD_KEYSTORE_BASE64` | 위에서 복사한 base64 텍스트 |
      | `UPLOAD_KEYSTORE_PASSWORD` | 키 저장소 비밀번호 |
      | `UPLOAD_KEY_ALIAS` | `upload` |
      | `UPLOAD_KEY_PASSWORD` | 키 비밀번호 |
- [ ] 다음 빌드부터 아티팩트 이름이 `nalabel-release-aab-upload` 로 나오면 성공. 이 파일을 플레이 콘솔에 올린다.

### 플레이 콘솔

- [ ] **Play App Signing** 사용(기본값). 실제 앱 서명 키는 구글이 보관하고, 위 키는 '업로드 키'로만 쓰인다.
      업로드 키를 잃어버려도 콘솔에서 재설정을 요청할 수 있다.
- [ ] 올릴 때마다 `app/build.gradle.kts` 의 `versionCode` 를 1 씩 올린다(같은 번호는 거부됨).
- [ ] Room 스키마(`app/schemas/`)를 커밋해 두고, 이후 DB 변경 시 마이그레이션 작성

## 실기기 확인

CI 가 매 push 마다 release 빌드를 안드로이드 14 에뮬레이터에서 처음부터 끝까지 눌러 본다(스크린샷: `ci-screens` 브랜치).
에뮬레이터로 확인할 수 없는 것만 실제 폰으로 확인하면 된다.

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

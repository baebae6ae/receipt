# 나 성분표 (가칭)

폰 사용 데이터를 기기 안에서만 처리해 매주 "이번 주의 나"를 식품 성분표 이미지 카드로 발행하는 안드로이드 앱.
서버·유료 API 없음, 사용자 데이터는 기기 밖으로 나가지 않는다.

## 빌드

Android SDK(platform 36)가 설치된 환경에서:

```
./gradlew assembleDebug
```

`local.properties`에 `sdk.dir=...`를 지정하거나 `ANDROID_HOME`을 설정한다.

# TravelFoodie

**TravelFoodie**는 여행 계획을 세우고, AI가 추천한 명소를 기반으로 그 지역의 최고 맛집 10개를 자동으로 제시하는 안드로이드 애플리케이션입니다.

## 주요 기능

> **구현 상태**: ✅ Implemented | ⚠️ Partially Implemented | ❌ Not Implemented

### 1. 여행 계획 작성 (Trip Planning) ✅
- 출발일/도착일 선택 (DatePicker)
- 여행지 입력 및 여행 주제 선택 (액티브/문화/휴식/쇼핑/맛집 투어)
- 함께 가는 사람 선택 (팀원 태그)
- D-day 자동 계산 및 표시

### 2. AI 명소 추천 (Attraction Recommendations) ✅
- 여행지 입력 후 자동으로 TOP 5 명소 추천
- 명소별 카드 형태로 표시 (이미지 + 설명 + 별점)
- 지역별 맞춤 추천 (서울, 부산, 제주 등)

### 3. 자동 맛집 리스트 제공 (Restaurant Recommendations) ✅
- 여행 계획 완료 시 해당 지역 맛집 자동 검색
- 명소 기반 추천 맛집 10개 리스트 제시
- 각 맛집: 이름 / 별점 / 거리 / 음식 카테고리 표시
- Kakao Maps API 연동

### 4. 지역별 맛집 상세 조회 (Restaurant Details) ✅
- 지역 클릭 시 하단에서 슬라이드 업 → 맛집 리스트 팝업
- 상세 정보: 메뉴 / 영업시간 / 예약 가능 여부
- Google Maps 연동으로 길찾기 지원

### 5. 음성 명령 계획 수정 (Voice Commands) ✅
> **Status**: Implemented with VoiceCommandHelper for STT and TTS integration.

- STT로 "3월 15일로 변경해줘" → 자동 수정 ✅
- "서울 추가" → 여행지 추가 ✅
- "팀원 추가: 철수" → 팀원 태그 추가 ✅
- TTS로 명소 설명 읽어주기 ✅

### 6. 홈 위젯 (Home Widget) ✅
> **Status**: Fully implemented with database integration and real-time trip data.

- 다음 예정된 여행 정보 실시간 표시 ✅
- 여행까지 D-day 카운트다운 ✅
- 명소 및 맛집 개수 표시 ✅
- 위젯 클릭 시 앱 내 여행 상세 화면으로 이동 ✅

### 7. 푸시 알림 (Push Notifications) ✅
- 여행 1주일 전: "OOO님 여행까지 7일 남았어요!"
- 여행 3일 전: "즐거운 마음으로 여행 준비를 시작해보세요 🎒"
- 여행 당일: "오늘이 여행 떠나는 날입니다. 즐거운 시간 되세요!"

### 8. 여행 계획 관리 (Trip Management) ✅
- 긴 터치(Long Press) → 수정/삭제 메뉴 팝업 ✅
- 진동 피드백으로 긴 터치 감지 ✅
- 스와이프로 계획 삭제 가능 ✅

### 9. 흔들기로 랜덤 맛집 추천 (Shake to Recommend) ✅
> **Status**: Fully implemented with GPS geofencing, Lottie animations, and random selection.

- 가속도센서로 폰 흔들면 자동 감지 ✅
- GPS 기능으로 여행지 도착 시 활성화 (지오펜스 1km 이내) ✅
- 현재 여행 계획 기준으로 맛집 리스트에서 랜덤 3개 선택 ✅
- 진동으로 선택 완료 피드백 ✅
- Lottie 애니메이션으로 슬롯머신 효과 ✅

### 10. Google 로그인 (User Authentication) ✅
- Google 로그인 API로 계정 연동
- 닉네임 생성 및 프로필 관리
- 기록 유지 및 푸시 알림 시 닉네임 사용

### 11. 보드/채팅 (Board/Chat) ✅
> **Status**: Implemented with Firebase Realtime Database for real-time messaging.

- Firebase Realtime Database로 팀원 간 실시간 채팅 ✅
- 이미지 첨부 기능 (Firebase Storage) ⚠️ (structure ready, UI not added)
- @mention 기능으로 FCM 푸시 알림 ⚠️ (basic structure, notifications not implemented)

### 12. OCR 영수증 스캔 (Receipt OCR) ✅
> **Status**: Implemented with ML Kit Text Recognition for receipt scanning.

- ML Kit으로 영수증 텍스트 인식 ✅
- 가맹점명 및 금액 자동 추출 ✅
- 맛집과 자동 매칭 ⚠️ (manual matching ready, auto-matching logic not added)

## 기술 스택

### 언어 및 프레임워크
- **Kotlin** 2.0.20
- **Android Gradle Plugin** 8.5.2
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

### 아키텍처
- **Clean Architecture** with modular structure
- **MVVM** pattern with ViewModels
- **Repository Pattern** for data layer
- **Dependency Injection** with Hilt

### 모듈 구조
```
:app                    # Main application module ✅ (includes OCR)
:core:ui                # Shared UI components ✅
:core:domain            # Domain models and use cases ✅
:core:data              # Data layer (Room, Retrofit, repositories) ✅
:core:sync              # Firebase sync and authentication ✅
:core:sensors           # Sensor handling (accelerometer) ✅
:feature:trip           # Trip management feature ✅ (includes voice commands)
:feature:attraction     # Attraction recommendations ✅ (includes TTS)
:feature:restaurant     # Restaurant listings ✅ (includes shake feature)
:feature:widget         # Home screen widget ✅
:feature:board          # Board/chat feature ✅ (Firebase Realtime Database)
```

### 주요 라이브러리

#### UI
- **Material Design 3** 1.12.0
- **ConstraintLayout** 2.1.4
- **RecyclerView** with ListAdapter
- **ViewBinding**
- **Lottie** 6.4.0 for animations

#### Database & Storage
- **Room** 2.6.1 (Local database)
- **Firebase Realtime Database** (Cloud sync)
- **Firebase Storage** (Image storage)

#### Networking
- **Retrofit** 2.11.0
- **OkHttp** 4.12.0
- **Moshi** 1.15.1 (JSON parsing)

#### Dependency Injection
- **Hilt** 2.51.1

#### Asynchronous
- **Coroutines** 1.8.1
- **Flow** for reactive streams

#### Location & Maps
- **FusedLocationProviderClient** 21.3.0
- **Google Maps** 18.2.0
- **Google Places** 3.5.0

#### Firebase
- **Firebase Auth** (Google Sign-In)
- **Firebase Realtime Database**
- **Firebase Cloud Messaging** (Push notifications)
- **Firebase Storage**

#### Other
- **WorkManager** 2.9.0 (Background tasks)
- **Paging3** 3.3.2 (Pagination)
- **ML Kit Text Recognition** 16.0.0 (OCR)

## 설치 및 실행

### 1. 사전 요구사항
- **Android Studio** Hedgehog (2023.1.1) 이상
- **JDK** 17
- **Android SDK** 34
- **Gradle** 8.5+

### 2. 프로젝트 클론
```bash
git clone <repository-url>
cd TravelFoodie
```

### 3. API 키 설정
`local.properties` 파일을 열고 다음 API 키를 입력하세요:

```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
PLACES_API_KEY=YOUR_GOOGLE_PLACES_API_KEY_HERE
KAKAO_API_KEY=YOUR_KAKAO_API_KEY_HERE
NAVER_CLIENT_ID=YOUR_NAVER_CLIENT_ID_HERE
NAVER_CLIENT_SECRET=YOUR_NAVER_CLIENT_SECRET_HERE
```

#### API 키 발급 방법:

**Google Maps & Places API:**
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 새 프로젝트 생성
3. "APIs & Services" → "Enable APIs and Services"
4. "Maps SDK for Android" 및 "Places API" 활성화
5. "Credentials" → "Create Credentials" → "API Key"

**Kakao API:**
1. [Kakao Developers](https://developers.kakao.com/) 접속
2. 애플리케이션 추가
3. "앱 키" → "REST API 키" 복사

**Naver API:**
1. [Naver Developers](https://developers.naver.com/) 접속
2. "Application" → "애플리케이션 등록"
3. "Client ID" 및 "Client Secret" 복사

### 4. Firebase 설정

#### 4.1 Firebase 프로젝트 생성
1. [Firebase Console](https://console.firebase.google.com/) 접속
2. "프로젝트 추가" 클릭
3. 프로젝트 이름 입력 (예: TravelFoodie)

#### 4.2 Android 앱 추가
1. Firebase 프로젝트에서 "Android 앱 추가" 클릭
2. 패키지 이름: `com.travelfoodie`
3. `google-services.json` 다운로드
4. `app/` 디렉토리에 `google-services.json` 파일 복사

#### 4.3 Firebase 기능 활성화
- **Authentication** → Google 제공업체 활성화
- **Realtime Database** → 데이터베이스 생성 (테스트 모드)
- **Cloud Messaging** → 자동 활성화됨
- **Storage** → 스토리지 버킷 생성

#### 4.4 Firebase 보안 규칙 설정
**Realtime Database Rules:**
```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

**Storage Rules:**
```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 5. 빌드 및 실행
```bash
./gradlew clean
./gradlew assembleDebug
```

또는 Android Studio에서:
1. "File" → "Sync Project with Gradle Files"
2. "Run" → "Run 'app'"

## 권한 (Permissions)

앱에서 요청하는 권한:
- `ACCESS_FINE_LOCATION` - 맛집까지의 거리 계산
- `ACCESS_COARSE_LOCATION` - 대략적인 위치 정보
- `POST_NOTIFICATIONS` (Android 13+) - 푸시 알림
- `VIBRATE` - 진동 피드백
- `INTERNET` - 네트워크 통신
- `RECEIVE_BOOT_COMPLETED` - 부팅 후 알림 재설정
- `SCHEDULE_EXACT_ALARM` (Android 12+) - 정확한 알림 예약

## 프로젝트 구조

```
TravelFoodie/
├── app/                                # Main application module
│   ├── src/main/
│   │   ├── java/com/travelfoodie/
│   │   │   ├── TravelFoodieApp.kt      # Application class with Hilt
│   │   │   ├── MainActivity.kt         # Main activity with navigation
│   │   │   ├── ProfileFragment.kt      # User profile screen
│   │   │   ├── di/                     # Dependency injection modules
│   │   │   ├── receiver/               # Broadcast receivers
│   │   │   └── service/                # Firebase messaging service
│   │   ├── res/                        # Resources
│   │   │   ├── layout/                 # XML layouts
│   │   │   ├── values/                 # Strings, colors, themes
│   │   │   ├── navigation/             # Navigation graph
│   │   │   └── xml/                    # Widget info, backup rules
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json            # Firebase configuration
├── core/
│   ├── data/                           # Data layer
│   │   └── src/main/java/com/travelfoodie/core/data/
│   │       ├── local/                  # Room database
│   │       │   ├── entity/             # Database entities
│   │       │   ├── dao/                # Data access objects
│   │       │   └── AppDatabase.kt
│   │       ├── remote/                 # API services
│   │       │   └── api/                # Retrofit interfaces
│   │       ├── repository/             # Repository implementations
│   │       └── di/                     # Data module DI
│   ├── domain/                         # Domain layer
│   │   └── src/main/java/com/travelfoodie/core/domain/
│   │       └── model/                  # Domain models
│   ├── sensors/                        # Sensor handling
│   │   └── src/main/java/com/travelfoodie/core/sensors/
│   │       └── ShakeDetector.kt        # Accelerometer shake detection
│   ├── sync/                           # Firebase sync
│   │   └── src/main/java/com/travelfoodie/core/sync/
│   │       └── AuthManager.kt          # Firebase authentication
│   └── ui/                             # Shared UI components
├── feature/
│   ├── trip/                           # Trip management
│   │   └── src/main/java/com/travelfoodie/feature/trip/
│   │       ├── TripListFragment.kt
│   │       ├── TripViewModel.kt
│   │       └── TripAdapter.kt
│   ├── attraction/                     # Attraction recommendations
│   │   └── src/main/java/com/travelfoodie/feature/attraction/
│   │       ├── AttractionListFragment.kt
│   │       ├── AttractionViewModel.kt
│   │       └── AttractionAdapter.kt
│   ├── restaurant/                     # Restaurant listings
│   │   └── src/main/java/com/travelfoodie/feature/restaurant/
│   │       ├── RestaurantListFragment.kt
│   │       ├── RestaurantViewModel.kt
│   │       └── RestaurantAdapter.kt
│   ├── voice/                          # Voice commands (STT/TTS)
│   ├── widget/                         # Home screen widget
│   │   └── src/main/java/com/travelfoodie/feature/widget/
│   │       └── TripWidgetProvider.kt
│   └── board/                          # Board/chat (optional)
├── gradle/
│   └── libs.versions.toml              # Version catalog
├── build.gradle.kts                    # Root build script
├── settings.gradle.kts                 # Module configuration
├── gradle.properties                   # Gradle properties
├── local.properties                    # API keys (not in VCS)
└── README.md                           # This file
```

## 데이터베이스 스키마

### Room Database Tables

#### users
- `userId` (PK): String
- `nickname`: String
- `email`: String
- `createdAt`: Long

#### trips
- `tripId` (PK): String
- `userId` (FK): String
- `title`: String
- `startDate`: Long
- `endDate`: Long
- `theme`: String
- `createdAt`: Long

#### members
- `memberId` (PK): Long (auto-increment)
- `tripId` (FK): String
- `name`: String
- `role`: String

#### regions
- `regionId` (PK): String
- `tripId` (FK): String
- `name`: String
- `lat`: Double
- `lng`: Double
- `order`: Int

#### pois (Points of Interest / Attractions)
- `poiId` (PK): String
- `regionId` (FK): String
- `name`: String
- `category`: String
- `rating`: Float
- `imageUrl`: String?
- `description`: String?

#### restaurants
- `restaurantId` (PK): String
- `regionId` (FK): String
- `name`: String
- `category`: String
- `rating`: Float
- `distance`: Double?
- `lat`: Double
- `lng`: Double
- `menu`: String?
- `hours`: String?
- `reservable`: Boolean
- `imageUrl`: String?

#### favorites
- `favoriteId` (PK): Long (auto-increment)
- `userId` (FK): String
- `restaurantId` (FK): String
- `createdAt`: Long

#### notif_schedules
- `scheduleId` (PK): Long (auto-increment)
- `tripId` (FK): String
- `fireAt`: Long
- `type`: String (D-7, D-3, D-0)
- `sent`: Boolean

#### receipts
- `receiptId` (PK): String
- `restaurantId` (FK): String?
- `merchantName`: String
- `total`: Double
- `imageUrl`: String
- `createdAt`: Long

## 사용 방법

### 1. 첫 실행 및 로그인
1. 앱 실행
2. "Google로 로그인" 버튼 클릭
3. Google 계정 선택
4. 닉네임 입력 후 "프로필 만들기"

### 2. 여행 계획 작성
1. 하단 네비게이션에서 "여행" 탭 선택
2. 우측 하단 "+" 버튼 클릭
3. 여행 제목, 출발일, 도착일 입력
4. 여행 테마 선택 (액티브/문화/휴식/쇼핑/맛집 투어)
5. 여행지 추가 (예: 서울, 부산)
6. 함께 가는 사람 추가 (선택사항)
7. "저장" 버튼 클릭

### 3. 명소 및 맛집 확인
1. 여행 계획 저장 후 자동으로 명소 TOP 5 추천
2. "명소" 탭에서 추천 명소 확인
3. "맛집" 탭에서 추천 맛집 TOP 10 확인
4. 맛집 카드 클릭 시 상세 정보 표시

### 4. 음성 명령 사용
1. 여행 계획 화면에서 마이크 버튼 클릭
2. "3월 20일로 변경해줘" 또는 "서울 추가" 등 음성 명령
3. 자동으로 계획 수정

### 5. 흔들기로 랜덤 맛집 추천
1. 여행지에 도착 (GPS로 1km 이내 감지)
2. 폰을 세게 흔들기
3. 랜덤 3개 맛집 추천 팝업 표시
4. 원하는 맛집 선택 후 지도에서 확인

### 6. 홈 위젯 추가
1. 홈 화면 길게 누르기
2. "위젯" 선택
3. "TravelFoodie" 위젯 찾기
4. 홈 화면에 드래그하여 추가
5. 다음 여행 D-day 자동 표시

## 테스트

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Lint Check
```bash
./gradlew lint
```

## 빌드 변형 (Build Variants)

- **debug**: 개발용 빌드 (디버그 로그 활성화)
- **release**: 배포용 빌드 (ProGuard 활성화, 최적화)

## 문제 해결 (Troubleshooting)

### 1. Gradle Sync 실패
- Android Studio에서 "File" → "Invalidate Caches / Restart"
- `~/.gradle/caches` 폴더 삭제 후 재시도

### 2. Firebase 연동 오류
- `google-services.json` 파일이 `app/` 디렉토리에 있는지 확인
- Firebase Console에서 패키지 이름이 `com.travelfoodie`인지 확인

### 3. API 키 오류
- `local.properties` 파일에 모든 API 키가 올바르게 입력되었는지 확인
- API 키에 불필요한 공백이나 따옴표가 없는지 확인

### 4. 위치 권한 오류
- 설정 → 앱 → TravelFoodie → 권한 → 위치 → "앱 사용 중에만 허용"

### 5. 알림이 오지 않음
- 설정 → 앱 → TravelFoodie → 알림 → 모든 알림 허용
- Android 13 이상: 알림 권한 허용 필요

## 기여 (Contributing)

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 라이선스 (License)

This project is licensed under the MIT License - see the LICENSE file for details.

## 연락처 (Contact)

프로젝트 관련 문의: [your-email@example.com](mailto:your-email@example.com)

## 감사의 말 (Acknowledgments)

- Google Maps Platform
- Kakao Developers
- Naver Developers
- Firebase
- Material Design
- Android Jetpack

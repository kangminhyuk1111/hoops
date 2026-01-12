# IntelliJ에서 모노레포 프로젝트 인식 문제

> 마지막 업데이트: 2026-01-12

## 문제 상황

모노레포 구조로 전환 후 IntelliJ가 `backend/` 폴더를 Spring Boot 프로젝트로 인식하지 못하는 문제.

**증상:**
- Spring Boot 실행 버튼이 나타나지 않음
- Gradle 태스크가 보이지 않음
- 의존성 자동완성이 동작하지 않음
- `@SpringBootApplication` 등 어노테이션에 빨간 줄

---

## 해결 방법

### 방법 1: backend 폴더를 직접 열기 (가장 간단)

백엔드만 작업할 때 권장하는 방법입니다.

**단계:**
1. `File` > `Open`
2. `hoops/backend` 폴더 선택
3. `Open as Project` 클릭

**장점:** 설정 변경 없이 바로 동작
**단점:** 프론트엔드와 docs를 같이 보려면 별도 창 필요

---

### 방법 2: 루트에서 Gradle 모듈로 Import (권장)

모노레포 전체를 열고 백엔드를 모듈로 추가합니다.

**단계:**
1. IntelliJ에서 루트 폴더(`hoops/`) 열기
2. `File` > `Project Structure` (단축키: `⌘ + ;` 또는 `Ctrl + Alt + Shift + S`)
3. 왼쪽 메뉴에서 `Modules` 선택
4. `+` 버튼 클릭 > `Import Module`
5. `backend/build.gradle.kts` 파일 선택
6. Import 옵션에서 `Gradle` 선택
7. `OK` 클릭
8. Gradle sync 완료 대기

**결과:** 백엔드가 모듈로 추가되어 Spring Boot 기능 활성화

---

### 방법 3: 루트 settings.gradle.kts 사용 (모노레포 표준)

루트에 Gradle 설정을 추가하여 자동 인식되게 합니다.

**설정 파일:** `hoops/settings.gradle.kts`
```kotlin
rootProject.name = "hoops"

// 백엔드 모듈 포함
includeBuild("backend")
```

**단계:**
1. 위 파일이 이미 생성되어 있음
2. IntelliJ에서 `File` > `Invalidate Caches and Restart`
3. 또는 Gradle 탭에서 🔄 (Reload All Gradle Projects) 클릭

---

### 방법 4: .idea 폴더 초기화 (문제 지속 시)

IntelliJ 캐시가 꼬인 경우 사용합니다.

**단계:**
1. IntelliJ 종료
2. `.idea` 폴더 삭제:
   ```bash
   rm -rf .idea
   ```
3. IntelliJ에서 프로젝트 다시 열기
4. `Import Gradle Project` 선택

**주의:** `.idea` 삭제 시 Run Configuration 등 개인 설정이 사라짐

---

## Gradle 동기화 강제 실행

설정 후에도 인식이 안 되면 Gradle 동기화를 강제로 실행합니다.

**방법 1: UI에서**
- 오른쪽 Gradle 탭 > 🔄 아이콘 클릭

**방법 2: 단축키**
- `⌘ + Shift + A` (Action 검색) > "Reload All Gradle Projects" 입력

**방법 3: 터미널에서**
```bash
cd backend && ./gradlew --refresh-dependencies
```

---

## Run Configuration 수동 생성

Spring Boot 실행 버튼이 나타나지 않을 때 수동으로 생성합니다.

**단계:**
1. `Run` > `Edit Configurations`
2. `+` > `Spring Boot`
3. 설정:
   - **Name:** HoopsApplication
   - **Main class:** `com.hoops.HoopsApplication`
   - **Use classpath of module:** `backend.main` 또는 `hoops.backend.main`
   - **JRE:** 17 이상
4. `Apply` > `OK`

---

## 확인 체크리스트

설정 후 다음 항목을 확인하세요:

| 항목 | 확인 방법 |
|------|----------|
| Gradle 태스크 표시 | 오른쪽 Gradle 탭에 `backend` 프로젝트 표시 |
| 의존성 인식 | `build.gradle.kts` 열고 의존성에 빨간 줄 없음 |
| Spring Boot 인식 | `HoopsApplication.java`에 실행 버튼(▶) 표시 |
| 자동완성 | `@Autowired`, `@Service` 등 어노테이션 자동완성 |

---

## 관련 이슈

### JDK 버전 문제

IntelliJ가 잘못된 JDK를 사용하는 경우:

1. `File` > `Project Structure` > `Project`
2. SDK: Java 17 이상 선택
3. Language level: 17 선택

### Lombok 인식 문제

Lombok 어노테이션에 빨간 줄이 나타나는 경우:

1. `Settings` > `Plugins` > `Lombok` 설치 확인
2. `Settings` > `Build, Execution, Deployment` > `Compiler` > `Annotation Processors`
3. `Enable annotation processing` 체크

---

## 참고

- IntelliJ 버전: 2023.x 이상 권장
- Gradle 버전: 8.x
- Java 버전: 17+

---

## 변경 이력

| 날짜 | 변경 내용 | 작성자 |
|------|----------|--------|
| 2026-01-12 | 최초 작성 - 모노레포 전환 후 IntelliJ 설정 가이드 | Claude |

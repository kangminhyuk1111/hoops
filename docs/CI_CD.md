# CI/CD 전략

## 브랜치 전략

### 브랜치 구조

- `main`: 프로덕션 배포 브랜치 (보호됨)
- `feature/*`: 기능 개발 브랜치

### 브랜치 보호 규칙

| 규칙 | 설정 |
|------|------|
| 직접 푸시 | 금지 |
| PR 필수 | O |
| CI 통과 필수 | O |
| 리뷰 승인 | 불필요 (솔로 개발) |

## CI (Continuous Integration)

### 트리거

- PR 생성/업데이트 시
- main 브랜치 푸시 시

### 파이프라인

```
코드 체크아웃 → JDK 17 설정 → Gradle 빌드 → 테스트 실행
```

### 설정 파일

`.github/workflows/ci.yml`

## CD (Continuous Deployment)

### 트리거

- CI 워크플로우 성공 후 자동 실행 (main 브랜치만)

### 파이프라인

```
CI 성공 → SSH로 EC2 접속 → deploy.sh 실행
```

### deploy.sh 동작

1. `git pull` - 최신 코드 가져오기
2. `docker build` - Backend/Frontend 이미지 빌드
3. `docker-compose up` - Backend/Frontend만 재시작 (MySQL 유지)
4. `docker image prune` - 불필요한 이미지 정리

### 설정 파일

`.github/workflows/cd.yml`

## 개발 플로우

### 1. 기능 개발 시작

```bash
git checkout main
git pull origin main
git checkout -b feature/기능명
```

### 2. 개발 및 커밋

```bash
git add .
git commit -m "feat: 기능 설명"
```

### 3. 푸시 및 PR 생성

```bash
git push origin feature/기능명
```

GitHub에서 PR 생성

### 4. CI 확인

- PR 페이지에서 CI 상태 확인
- 실패 시 수정 후 재푸시

### 5. PR 머지

- CI 통과 후 머지
- 자동으로 CD 실행되어 프로덕션 배포

### 6. 브랜치 정리

```bash
git checkout main
git pull origin main
git branch -d feature/기능명
```

## GitHub Secrets

CD 실행에 필요한 시크릿:

| Secret | 설명 |
|--------|------|
| `EC2_HOST` | EC2 퍼블릭 IP |
| `EC2_SSH_KEY` | SSH 프라이빗 키 |

## 인프라 구성

### EC2 서버

- 인스턴스: t3.small (2 vCPU, 2GB RAM)
- OS: Amazon Linux 2023
- Swap: 2GB
- 스토리지: 30GB gp3

### Docker 컨테이너

| 컨테이너 | 포트 | 설명 |
|----------|------|------|
| hoops-mysql | 3306 | MySQL 8.0 |
| hoops-backend | 8080 | Spring Boot API |
| hoops-frontend | 3000 | Next.js |

## 배포 URL

- Frontend: http://{EC2_IP}:3000
- Backend API: http://{EC2_IP}:8080

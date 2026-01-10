# Hoops

## 실행 방법

### 1. 환경 변수 설정

```bash
cp .env .env
```

`.env` 파일에 카카오 API 키 설정:

```
KAKAO_CLIENT_ID=your-kakao-rest-api-key
KAKAO_CLIENT_SECRET=your-kakao-client-secret
JWT_SECRET=your-secret-key-must-be-at-least-32-characters-long
```

### 2. 실행

```bash
docker-compose up -d
```

서버: http://localhost:8080

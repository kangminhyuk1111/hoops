# 패키지 구조 규칙

## DTO 위치

DTO는 반드시 `adapter/in/web/dto/` 패키지에 배치한다.

```
adapter/
└── in/
    └── web/
        ├── UserController.java
        └── dto/
            └── UserResponse.java   <- DTO는 여기에
```

**잘못된 예:**
```
adapter/
└── in/
    └── web/
        ├── UserController.java
        └── UserResponse.java   <- X (dto 폴더 누락)
```

## 새 도메인 추가 시 패키지 구조

```
{domain}/
├── domain/
│   ├── {Domain}.java              # Domain Model (Pure POJO)
│   └── repository/
│       └── {Domain}Repository.java  # Repository Port
│
├── application/
│   ├── port/
│   │   └── in/
│   │       └── {UseCase}UseCase.java
│   ├── service/
│   │   └── {Domain}Service.java
│   └── exception/
│       └── {Domain}NotFoundException.java
│
├── adapter/
│   └── in/
│       └── web/
│           ├── {Domain}Controller.java
│           └── dto/
│               ├── {Domain}Request.java
│               └── {Domain}Response.java
│
└── infrastructure/
    ├── {Domain}Entity.java
    ├── jpa/
    │   └── Jpa{Domain}Repository.java
    ├── adapter/
    │   └── {Domain}RepositoryImpl.java
    └── mapper/
        └── {Domain}Mapper.java
```

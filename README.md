# Coupang Clone Backend

Spring Boot 기반의 쿠팡 이커머스 플랫폼 클론 백엔드 API 서버입니다.

멀티모듈 구조와 헥사고날 아키텍처(Port-Adapter 패턴)를 적용하여 확장성과 유지보수성을 고려한 설계를 구현했습니다.

## Tech Stack

| 분류 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2, Spring Security 6, Spring Data JPA |
| **Database** | MySQL, Redis |
| **Cloud** | AWS EC2, AWS S3 |
| **CI/CD** | GitHub Actions |
| **Infra** | Docker Compose |
| **Monitoring** | ELK Stack (Elasticsearch + Kibana), MDC 기반 요청 추적 |
| **Code Quality** | SonarQube, JaCoCo |
| **Documentation** | SpringDoc OpenAPI 3.0 (Swagger) |
| **Testing** | JUnit 5, Mockito, MockMvc |

## Architecture

### Multi-Module Structure

```
coupangclone/
├── api/          # REST 컨트롤러, 필터, 보안 설정, Swagger
├── domain/       # 엔티티, 서비스, 리포지토리, Port 인터페이스
├── common/       # 공통 예외, 응답 DTO, 유틸리티
└── infra/        # Redis Adapter, S3 Adapter, 로깅
```

### Module Dependencies

```
api → common, domain, infra
infra → common, domain
domain → common
common → (standalone)
```

### Hexagonal Architecture (Port-Adapter)

도메인 레이어에서 Port(인터페이스)를 정의하고, 인프라 레이어에서 Adapter로 구현합니다.

```
Domain Layer (Ports)          Infra Layer (Adapters)
─────────────────────         ──────────────────────
JwtPort          ──────────>  JwtProvider
RedisPort        ──────────>  RedisAdapter
S3UploadPort     ──────────>  S3Uploader
```

> 외부 기술(Redis, S3, JWT)이 변경되어도 도메인 로직에 영향을 주지 않습니다.

## ERD

![ERD](docs/erd.png)

## Key Features

### 1. JWT 이중 토큰 인증/인가

- **Access Token** (15분) + **Refresh Token** (14일) 전략
- Redis 기반 Refresh Token 저장 및 로그아웃 시 토큰 **블랙리스트** 처리
- 만료된 Access Token 자동 갱신 메커니즘
- Spring Security 연동 역할 기반 접근 제어 (USER / ADMIN)

### 2. 상품 관리

- AWS S3 연동 멀티파트 이미지 업로드 (UUID 파일명, 확장자 검증)
- 카테고리 계층 구조 (부모-자식)
- 브랜드 관리

### 3. 상품 검색

- JPQL 기반 대소문자 무시 검색 (상품명 + 브랜드명)
- 정렬 옵션 (최신순, 가격 오름차순/내림차순)
- 페이지네이션
- 검색 로그 기반 **연관 키워드 추천**

### 4. 리뷰 시스템

- 별점 + 텍스트 리뷰
- 리뷰 이미지 첨부
- 상품별 평균 평점 집계

### 5. 주문 / 장바구니 / 위시리스트 / 문의

- 장바구니 상품 관리
- 주문 및 주문 상품 관리
- 위시리스트
- 고객 문의 및 답변 (Inquiry / InquiryComment)

## API Documentation

Swagger UI를 통해 API 명세를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

### 주요 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/signup` | 회원가입 | - |
| POST | `/api/login` | 로그인 | - |
| POST | `/api/logout` | 로그아웃 | Bearer |
| GET | `/api/items` | 상품 목록 조회 (페이지네이션) | Bearer |
| POST | `/api/items` | 상품 등록 (이미지 업로드) | Bearer |
| GET | `/api/items/search` | 상품 검색 | Bearer |
| POST | `/admin/item/category` | 카테고리 생성 | ADMIN |
| POST | `/admin/item/brand` | 브랜드 생성 | ADMIN |

## Infrastructure

### CI/CD Pipeline (GitHub Actions)

```
Push to main
    │
    ▼
┌─────────────────┐     ┌─────────────────┐
│   Build Stage   │────>│  Deploy Stage   │
│                 │     │                 │
│ - Checkout      │     │ - SSH into EC2  │
│ - JDK 17 Setup  │     │ - Generate      │
│ - Gradle Cache  │     │   secret.yml    │
│ - Build JAR     │     │ - Restart App   │
│ - SCP to EC2    │     │ - Monitor Logs  │
└─────────────────┘     └─────────────────┘
```

- GitHub Secrets로 민감 정보(DB 비밀번호, JWT Secret, S3 키) 관리
- `application-secret.yml`을 배포 시점에 동적 생성

### Docker Compose (개발 환경)

```yaml
# SonarQube  - localhost:9000  (정적 코드 분석)
# Elasticsearch - localhost:9200 (로그 저장)
# Kibana     - localhost:5601  (로그 시각화)
```

### Monitoring & Logging

- **MDC (Mapped Diagnostic Context)**: 요청별 `traceId`, `userId` 부여
- **LoggingInterceptor**: 요청/응답 상세 로깅
- **ELK Stack**: Elasticsearch에 로그 수집, Kibana로 시각화

## Getting Started

### Prerequisites

- Java 17
- MySQL 8.x
- Redis
- (Optional) Docker & Docker Compose

### Run Locally

```bash
# 1. Clone
git clone https://github.com/SongHyeonJin/coupang-clone-backend.git
cd coupang-clone-backend

# 2. MySQL, Redis 실행 후 application-local.yml 설정

# 3. application-secret.yml 생성 (api/src/main/resources/yaml/)
# spring.datasource.password, jwt.secret, cloud.aws.s3.bucket, credentials 설정

# 4. Build & Run
./gradlew :api:clean :api:build -x test
java -jar api/build/libs/api-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Run Analysis Tools (Docker)

```bash
docker-compose up -d   # SonarQube + ELK Stack
```

## Project Structure

```
coupangclone/
├── api/
│   └── src/main/java/.../
│       ├── controller/         # REST 컨트롤러
│       │   ├── item/           # 상품, 관리자 상품 API
│       │   └── user/           # 회원 API
│       ├── config/             # Security, Swagger, CORS, Async 설정
│       ├── filter/             # JWT 인증 필터
│       └── advice/             # 응답 헤더 토큰 자동 주입 Advice
│
├── domain/
│   └── src/main/java/.../
│       ├── entity/             # JPA 엔티티
│       │   ├── user/           # User, Address
│       │   ├── item/           # Item, Category, Brand, ItemImage, SearchLog
│       │   ├── order/          # Order, OrderItem
│       │   ├── review/         # Review, ReviewImage
│       │   ├── cart/           # CartItem
│       │   ├── wish/           # Wish
│       │   └── inquriy/        # Inquiry, InquiryComment
│       ├── service/            # 비즈니스 로직
│       ├── repository/         # Spring Data JPA 리포지토리
│       └── auth/               # Port 인터페이스 (JwtPort, RedisPort, S3UploadPort)
│
├── common/
│   └── src/main/java/.../
│       ├── exception/          # 커스텀 예외, ExceptionEnum
│       ├── dto/                # 공통 응답 DTO (BasicResponseDto, ErrorResponseDto)
│       └── util/               # TokenHolder (ThreadLocal)
│
├── infra/
│   └── src/main/java/.../
│       ├── adapter/            # Port 구현체 (RedisAdapter, S3Uploader)
│       ├── config/             # Redis, S3 설정
│       └── logging/            # LoggingInterceptor, MDC 설정
│
├── .github/workflows/ci-cd.yml
├── docker-compose.yml
└── build.gradle
```

## Design Decisions

| 결정 | 이유 |
|------|------|
| **멀티모듈 구조** | 모듈 간 의존성 방향 제어, 빌드 단위 분리 |
| **Port-Adapter 패턴** | 도메인 로직의 외부 기술 독립성 확보 |
| **Command/Result 패턴** | 계층 간 결합도 최소화, 엔티티 직접 노출 방지 |
| **이중 토큰 + Redis** | 보안(짧은 Access Token)과 UX(자동 갱신) 동시 확보 |
| **ThreadLocal TokenHolder** | 필터 → Advice 간 토큰 전달, 응답 헤더 자동 주입 |
| **REQUIRES_NEW 전파** | 검색 로그 저장 실패가 메인 트랜잭션에 영향 없도록 분리 |
| **프로파일 분리** | local / test / prod 환경별 독립 설정 |

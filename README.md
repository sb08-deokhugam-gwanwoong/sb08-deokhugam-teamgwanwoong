# 📚 덕후감 (Deokhugam) - Backend

[![codecov](https://codecov.io/github/sb08-deokhugam-gwanwoong/sb08-deokhugam-teamgwanwoong/graph/badge.svg?token=FV04OHEYFK)](https://codecov.io/github/sb08-deokhugam-gwanwoong/sb08-deokhugam-teamgwanwoong)
![Java](https://img.shields.io/badge/Java-17-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-6DB33F?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?logo=amazons3)

> **독서 기록 및 리뷰 공유 플랫폼 '덕후감'의 백엔드 API 서버입니다.**

## 🛠 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.4.0
- **Database:** PostgreSQL (Prod), H2 (Local/Test)
- **ORM / Data Access:** Spring Data JPA, QueryDSL (5.0.0)
- **Infrastructure:** AWS S3 (File Storage), GitHub Actions (CI)
- **Documentation:** Swagger (Springdoc OpenAPI)
- **Others:** MapStruct, Spring Retry, Caffeine Cache

<br/>

## 🚀 Getting Started

### Prerequisites
프로젝트를 로컬에서 실행하기 위해 아래의 환경 변수 설정이 필요합니다. (IntelliJ Environment Variables 또는 시스템 환경 변수)

```env
# AWS S3 연동
AWS_BUCKET_NAME=your_bucket_name
AWS_REGION=your_aws_region
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key

# 네이버 도서 API
NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
```

Run Locally
```Bash
$./gradlew clean build$ ./gradlew bootRun
```

## 🧪 Testing & Coverage
- 단위 테스트 및 통합 테스트: JUnit5, Mockito, MockRestServiceServer 활용

- 테스트 커버리지: JaCoCo를 통해 라인 커버리지 80% 이상 유지 (Codecov CI 연동)

- 테스트 실행 및 리포트 확인:
```Bash
$ ./gradlew clean test jacocoTestReport
```
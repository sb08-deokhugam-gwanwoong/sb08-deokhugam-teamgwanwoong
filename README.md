## 📚 관웅

[관웅 노션 링크](https://ohgiraffers.notion.site/2-306649136c1180b5b9c8c948670916e0?source=copy_link)
***

## 👥 팀원 구성

|                                   프로필                                   |                   이름                   |              역할              | 맡은 기능                                                                                                                                                   |
|:-----------------------------------------------------------------------:|:--------------------------------------:|:----------------------------:|:--------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/idktomorrow.png" width="100" height="100"> | **[선웅제](https://github.com/본인깃허브아이디)** |       **형상관리(Github)**       | - 댓글 기능 <br> - Kafka 비동기 댓글 알림 시스템 구축<br>- Prometheus + Grafana 서버 모니터링 데이터 시각화                                                                         |
|    <img src="https://github.com/jaejo.png" width="100" height="100">    |  **[이재준](https://github.com/팀원2아이디)**  |      **Notion 관리 / 서기**      | - 리뷰 기능<br>- ElasticSearch 검색 최적화<br>- 검색 하이라이트 기능(UX)<br>- LogStash 데이터 동기화<br>- Kafka 비동기 처리(낙관적 응답)<br>- DB 비관적 락 처리<br>- 하이브리드 동시성 제어(비동기 파이프라인 구축) |
|   <img src="https://github.com/junkov0.png" width="100" height="100">   |  **[최준영](https://github.com/팀원3아이디)**  |        **배포 관리(AWS)**        | - 사용자 기능<br>- 알림 기능<br>- Spring Security 암호화 및 변경 기능<br> - 이메일 인증번호를 통한 비밀번호 찾기 기능<br>- Kafka 기반 알림 기능 비동기 처리<br>- Spring Batch 대규모 휴면 데이터 일괄 삭제 자동화    |
| <img src="https://github.com/seungwon00.png" width="100" height="100">  |  **[현승원](https://github.com/팀원4아이디)**  |          **DB 관리**           | - 대시보드 기능<br>- Spring Batch 대시보드 배치 작업<br>- Redis 활용 대시보드 조회 캐시 처리 성능 최적화                                                                               |
|  <img src="https://github.com/SungHuii.png" width="100" height="100">   |  **[홍성휘](https://github.com/팀원5아이디)**  | **PM<br/>(Project Manager)** | - 도서 기능<br>- Naver API & OCR Space API 연동<br>- Redis 도서 ISBN 캐시 처리<br>- S3 로그 주기적 업로드 기능<br>- 이미지 리사이징 파이프라인 구축                                         |

## 프로젝트 소개
### 덕후감 (Deokhugam)
> **독서 기록 및 리뷰 공유 플랫폼 '덕후감'의 백엔드 API 서버입니다.**  

- 프로젝트 기간 : 2026.02.27(금) - 2026.03.23(월)

## 🛠 Tech Stack  
### Backend  
<img src="https://img.shields.io/badge/Java 17-007396?style=for-the-badge&logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/Spring Boot 3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL 5.0.0-0769AD?style=for-the-badge&logo=querydsl&logoColor=white">

### Database & Search Engine  
<img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/H2 Database-003B57?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/Elasticsearch 8.12.0-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"> <img src="https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=elastic&logoColor=white">

### Infrastructure & Monitoring  
<img src="https://img.shields.io/badge/AWS EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"> <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"> <img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">

### Libraries
[![codecov](https://codecov.io/github/sb08-deokhugam-gwanwoong/sb08-deokhugam-teamgwanwoong/branch/dev/graph/badge.svg?token=FV04OHEYFK)](https://codecov.io/github/sb08-deokhugam-gwanwoong/sb08-deokhugam-teamgwanwoong)  
MapStruct, Spring Retry, Caffeine Cache, Thumbnailator (Image Resizing)  
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

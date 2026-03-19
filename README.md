## 📚 관웅

### [관웅 노션 링크](https://ohgiraffers.notion.site/2-306649136c1180b5b9c8c948670916e0?source=copy_link)
***

## 👥 팀원 구성

|                                   프로필                                   |                   이름                   |              역할              | 담당 기능                                                                                                                                                                                         |
|:-----------------------------------------------------------------------:|:--------------------------------------:|:----------------------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/idktomorrow.png" width="100" height="100"> | **[선웅제](https://github.com/idktomorrow)** |       **형상관리(Github)**       | **[주요 기능]** <br>- **댓글 기능** <br> **[추가/인프라 기능]**<br>- Kafka 비동기 댓글 알림 시스템 구축<br>- Prometheus + Grafana 서버 모니터링 데이터 시각화                                                                        |
|    <img src="https://github.com/jaejo.png" width="100" height="100">    |  **[이재준](https://github.com/jaejo)**  |      **Notion 관리 / 서기**      | **[주요 기능]**<br>- **리뷰 기능**<br>**[추가/인프라 기능]**<br>- ElasticSearch 검색 최적화<br>- 검색 하이라이트 기능(UX)<br>- LogStash 데이터 동기화<br>- Kafka 비동기 처리(낙관적 응답)<br>- DB 비관적 락 처리<br>- 하이브리드 동시성 제어(비동기 파이프라인 구축) |
|   <img src="https://github.com/junkov0.png" width="100" height="100">   |  **[최준영](https://github.com/junkov0)**  |        **배포 관리(AWS)**        | **[주요 기능]**<br>- **사용자 기능**<br>- **알림 기능**<br>**[추가/인프라 기능]**<br>- Spring Security 암호화 및 변경 기능<br> - 이메일 기반 비밀번호 재설정<br>- Kafka 기반 알림 기능 비동기 처리<br>- Spring Batch 기반 대용량 데이터 삭제               |
| <img src="https://github.com/seungwon00.png" width="100" height="100">  |  **[현승원](https://github.com/seungwon00)**  |          **DB 관리**           | **[주요 기능]**<br>- **대시보드 기능**<br>**[추가/인프라 기능]**<br>- Spring Batch 대시보드 배치 작업<br>- Redis 활용 대시보드 조회 캐시 처리 성능 최적화                                                                               |
|  <img src="https://github.com/SungHuii.png" width="100" height="100">   |  **[홍성휘](https://github.com/SungHuii)**  | **PM<br/>(Project Manager)** | **[주요 기능]**<br>- **도서 기능**<br>**[추가/인프라 기능]**<br>- Naver API & OCR Space API 연동<br>- Redis 도서 ISBN 캐시 처리<br>- S3 로그 주기적 업로드 기능<br>- 이미지 리사이징 파이프라인 구축                                         |

## 프로젝트 소개
### 덕후감 (Deokhugam)
> **독서 기록 및 리뷰 공유 플랫폼 '덕후감'의 백엔드 API 서버입니다.**  
- 배포링크 : [덕후감](http://gwanwoong.kro.kr/#/) http://gwanwoong.kro.kr/
- 프로젝트 기간 : 2026.02.27(금) - 2026.03.23(월)

## 🛠 기술 스택  
### Backend  
<img src="https://img.shields.io/badge/Java 17-007396?style=for-the-badge&logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/Spring Boot 3.4.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"> <img src="https://img.shields.io/badge/Spring Data JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white"> <img src="https://img.shields.io/badge/QueryDSL 5.0.0-0769AD?style=for-the-badge&logo=querydsl&logoColor=white">

### Database & Search Engine  
<img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"> <img src="https://img.shields.io/badge/H2 Database-003B57?style=for-the-badge&logo=java&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white"> <img src="https://img.shields.io/badge/Elasticsearch 8.12.0-005571?style=for-the-badge&logo=elasticsearch&logoColor=white"> <img src="https://img.shields.io/badge/Logstash-005571?style=for-the-badge&logo=elastic&logoColor=white">

### Infrastructure & Monitoring  
<img src="https://img.shields.io/badge/AWS EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white"> <img src="https://img.shields.io/badge/AWS S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white"> <img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white"> <img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white"> <img src="https://img.shields.io/badge/GitHub Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">

### Libraries
[![codecov](https://codecov.io/github/sb08-deokhugam-gwanwoong/sb08-deokhugam-teamgwanwoong/branch/dev/graph/badge.svg?token=FV04OHEYFK)](https://codecov.io/github/sb08-deokhugam-gwanwoong/sb08-deokhugam-teamgwanwoong)  
#### MapStruct, Spring Retry, Caffeine Cache, Thumbnailator, Crypto
<br/>

## 📂 Project Structure

<details>
<summary><b>패키지 구조 자세히 보기 (클릭)</b></summary>
<br>

```text
📦 sb08-deokhugam-teamgwanwoong
 ┣ 📂 .github/workflows  # GitHub Actions를 활용한 CI/CD 자동화 파이프라인
 ┣ 📂 src
 ┃ ┣ 📂 main
 ┃ ┃ ┣ 📂 java/.../sb08deokhugamteamgwanwoong
 ┃ ┃ ┃ ┣ 📂 common       # 공통 응답 포맷 및 유틸리티 클래스
 ┃ ┃ ┃ ┣ 📂 component    # 커스텀 스프링 빈 컴포넌트
 ┃ ┃ ┃ ┃ ┣ 📂 batch      # Spring Batch 기반 대용량 데이터 처리 로직
 ┃ ┃ ┃ ┃ ┗ 📂 scheduler  # S3 로그 업로드 등 주기적 스케줄링 작업
 ┃ ┃ ┃ ┣ 📂 config       # Security, Swagger, S3, Redis, Kafka 등 환경 설정
 ┃ ┃ ┃ ┣ 📂 controller   # 클라이언트 요청을 처리하는 API 엔드포인트
 ┃ ┃ ┃ ┃ ┗ 📂 docs       # Swagger API 명세 및 문서화 관련 설정
 ┃ ┃ ┃ ┣ 📂 dto          # 계층 간 데이터 전송 객체 (Request/Response)
 ┃ ┃ ┃ ┣ 📂 entity       # JPA 엔티티 및 데이터베이스 매핑 모델
 ┃ ┃ ┃ ┃ ┣ 📂 base       # 공통 Auditing 엔티티 (생성일, 수정일 등)
 ┃ ┃ ┃ ┃ ┗ 📂 enums      # 도메인 상태값 관리를 위한 Enum 클래스
 ┃ ┃ ┃ ┣ 📂 event        # Spring Application Event 및 Kafka 메시지 처리
 ┃ ┃ ┃ ┣ 📂 exception    # 전역 예외 처리(GlobalExceptionHandler) 및 커스텀 에러
 ┃ ┃ ┃ ┃ ┗ 📂 enums      # 에러 코드 및 예외 메시지 관리를 위한 Enum
 ┃ ┃ ┃ ┣ 📂 mapper       # MapStruct를 활용한 Entity ↔ DTO 변환 로직
 ┃ ┃ ┃ ┣ 📂 repository   # Spring Data JPA 기반 DB 접근 계층
 ┃ ┃ ┃ ┃ ┗ 📂 impl       # QueryDSL을 활용한 동적 쿼리 및 커스텀 구현체
 ┃ ┃ ┃ ┗ 📂 service      # 핵심 비즈니스 로직 및 트랜잭션 관리
 ┃ ┃ ┃   ┣ 📂 external   # Naver API, OCR Space 등 외부 서비스 연동 로직
 ┃ ┃ ┃   ┗ 📂 impl       # 서비스 인터페이스의 실제 비즈니스 로직 구현체
 ┃ ┃ ┗ 📂 resources
 ┃ ┃   ┣ 📂 elastic                # Elasticsearch 인덱스 매핑/세팅 파일
 ┃ ┃   ┣ 📜 application.yaml       # 공통 환경 설정
 ┃ ┃   ┣ 📜 application-prod.yaml  # 운영(EC2) 환경 전용 설정
 ┃ ┃   ┣ 📜 application-local.yaml # 로컬 개발 환경 전용 설정
 ┃ ┃   ┗ 📜 schema-pg.sql          # DB 테이블 및 스키마 초기화 스크립트
 ┃ ┗ 📂 test
 ┃   ┣ 📂 java/.../sb08deokhugamteamgwanwoong
 ┃   ┃ ┣ 📂 controller   # MockMvc 기반 컨트롤러 슬라이스 테스트
 ┃   ┃ ┣ 📂 integration  # 전 계층 통합 테스트
 ┃   ┃ ┗ 📂 service      # Mockito 기반 핵심 비즈니스 로직 단위 테스트
 ┃   ┗ 📂 resources
 ┃     ┗ 📜 application-test.yaml  # 테스트 격리용(H2 등) 환경 설정
 ┣ 📂 logstash           # Elasticsearch 데이터 동기화를 위한 Logstash 파이프라인
 ┣ 📜 build.gradle       # 의존성 관리 및 JaCoCo 테스트 커버리지 검증 설정
 ┣ 📜 docker-compose.yml # 애플리케이션 및 인프라(Redis, ES 등) 컨테이너 오케스트레이션
 ┣ 📜 Dockerfile         # Spring Boot 애플리케이션 이미지 빌드 설정
 ┗ 📜 prometheus.yml     # Prometheus 매트릭 수집 설정
```
</details>

### .env 파일 설정
> 프로젝트 폴더 내 env.example 파일을 확인해주세요
 
## ⚙️인프라 아키텍처 다이어그램
<img width="1400" height="784" alt="최종 drawio" src="https://github.com/user-attachments/assets/c6034391-1b5f-4ada-8c9b-cf3855c30836" />

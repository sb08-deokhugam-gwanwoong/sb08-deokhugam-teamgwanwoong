-- 기존 테이블 삭제 (의존성 순서 고려: 자식 테이블부터 삭제)
DROP TABLE IF EXISTS "notifications" CASCADE;
DROP TABLE IF EXISTS "review_likes" CASCADE;
DROP TABLE IF EXISTS "comments" CASCADE;
DROP TABLE IF EXISTS "reviews" CASCADE;
DROP TABLE IF EXISTS "books" CASCADE;
DROP TABLE IF EXISTS "users" CASCADE;
DROP TABLE IF EXISTS "dashboard" CASCADE;

--------------------------------------------------
-- 1. Users Table
--------------------------------------------------
CREATE TABLE "users" (
                         "id"            UUID            NOT NULL,
                         "email"         VARCHAR(50)     NOT NULL,
                         "nickname"      VARCHAR(50)     NOT NULL,
                         "password"      VARCHAR(255)    NOT NULL, -- 해싱된 비밀번호 저장을 위해 길이 상향
                         "created_at"    TIMESTAMPTZ     DEFAULT NOW() NOT NULL,
                         "updated_at"    TIMESTAMPTZ     NULL,
                         "deleted_at"    TIMESTAMPTZ     NULL,
                         CONSTRAINT "PK_USERS" PRIMARY KEY ("id"),
                         CONSTRAINT "UQ_USERS_EMAIL" UNIQUE ("email"),
                         CONSTRAINT "UQ_USERS_NICKNAME" UNIQUE ("nickname")
);

COMMENT ON COLUMN "users"."nickname" IS 'UNIQUE, (2 ~ 10)';
COMMENT ON COLUMN "users"."password" IS '정규표현식 적용 필요';

--------------------------------------------------
-- 2. Books Table
--------------------------------------------------
CREATE TABLE "books" (
                         "id"             UUID            NOT NULL,
                         "title"          VARCHAR(100)    NOT NULL,
                         "author"         VARCHAR(50)     NOT NULL,
                         "description"    TEXT            NOT NULL,
                         "publisher"      VARCHAR(50)     NOT NULL,
                         "published_date" DATE            NOT NULL,
                         "isbn"           VARCHAR(50)     NULL,
                         "thumbnail_url"  TEXT            NULL,
                         "review_count"   INTEGER         DEFAULT 0 NOT NULL,
                         "rating"         DOUBLE PRECISION DEFAULT 0.0 NOT NULL,
                         "created_at"     TIMESTAMPTZ     DEFAULT NOW() NOT NULL,
                         "updated_at"     TIMESTAMPTZ     NOT NULL,
                         "deleted_at"     TIMESTAMPTZ     NULL,
                         CONSTRAINT "PK_BOOKS" PRIMARY KEY ("id"),
                         CONSTRAINT "UQ_BOOKS_ISBN" UNIQUE ("isbn")
);

--------------------------------------------------
-- 3. Reviews Table
--------------------------------------------------
CREATE TABLE "reviews" (
                           "id"            UUID            NOT NULL,
                           "book_id"       UUID            NOT NULL,
                           "user_id"       UUID            NOT NULL,
                           "rating"        INTEGER         DEFAULT 0 NOT NULL,
                           "content"       TEXT            NOT NULL,
                           "like_count"    INTEGER         DEFAULT 0 NOT NULL,
                           "comment_count" INTEGER         DEFAULT 0 NOT NULL,
                           "created_at"    TIMESTAMPTZ     DEFAULT NOW() NOT NULL,
                           "updated_at"    TIMESTAMPTZ     NULL,
                           "deleted_at"    TIMESTAMPTZ     NULL,
                           CONSTRAINT "PK_REVIEWS" PRIMARY KEY ("id"),
    -- 한 사용자가 한 도서에 대해 하나의 리뷰만 작성 가능하도록 설정
                           CONSTRAINT "unique_user_book_review" UNIQUE ("book_id", "user_id")
);

--------------------------------------------------
-- 4. Comments Table
--------------------------------------------------
CREATE TABLE "comments" (
                            "id"            UUID            NOT NULL,
                            "user_id"       UUID            NOT NULL,
                            "review_id"     UUID            NOT NULL,
                            "content"       TEXT            NOT NULL,
                            "created_at"    TIMESTAMPTZ     DEFAULT NOW() NOT NULL,
                            "updated_at"    TIMESTAMPTZ     NULL,
                            "deleted_at"    TIMESTAMPTZ     NULL,
                            CONSTRAINT "PK_COMMENTS" PRIMARY KEY ("id")
);

--------------------------------------------------
-- 5. Review Likes Table
--------------------------------------------------
CREATE TABLE "review_likes" (
                                "id"            UUID            NOT NULL,
                                "review_id"     UUID            NOT NULL,
                                "user_id"       UUID            NOT NULL,
                                "created_at"    TIMESTAMPTZ     DEFAULT NOW() NOT NULL,
                                CONSTRAINT "PK_REVIEW_LIKES" PRIMARY KEY ("id"),
    -- 한 사용자가 한 리뷰에 대해 좋아요를 한 번만 누를 수 있도록 유니크 설정
                                CONSTRAINT "UQ_REVIEW_LIKE_USER" UNIQUE ("review_id", "user_id")
);

--------------------------------------------------
-- 6. Notifications Table
--------------------------------------------------
CREATE TABLE "notifications" (
                                 "id"            UUID            NOT NULL,
                                 "review_id"     UUID            NOT NULL,
                                 "user_id"       UUID            NOT NULL,
                                 "content"       VARCHAR(100)    NOT NULL,
                                 "is_confirmed"  BOOLEAN         DEFAULT FALSE NOT NULL,
                                 "review_title"  TEXT            NOT NULL,
                                 "created_at"    TIMESTAMPTZ     DEFAULT NOW() NOT NULL,
                                 "updated_at"    TIMESTAMPTZ     NULL,
                                 CONSTRAINT "PK_NOTIFICATIONS" PRIMARY KEY ("id")
);

--------------------------------------------------
-- 7. Dashboard Table (Ranking Data)
--------------------------------------------------
CREATE TABLE "dashboard" (
                             "id"            UUID            NOT NULL,
                             "target_id"     UUID            NOT NULL,
                             "target_type"   VARCHAR(20)     NOT NULL, -- 'BOOK', 'USER' 등
                             "period_type"   VARCHAR(20)     NOT NULL, -- 'DAILY', 'WEEKLY' 등
                             "score"         DOUBLE PRECISION NOT NULL,
                             "ranking_pos"   INTEGER         NOT NULL,
                             "created_at"    DATE            NOT NULL,
                             CONSTRAINT "PK_DASHBOARD" PRIMARY KEY ("id")
);

--------------------------------------------------
-- 8. Indexes (Performance Optimization)
--------------------------------------------------

-- [Users] 이메일과 닉네임은 UNIQUE 제약으로 자동 인덱싱됨

-- [Books] 도서명 검색 및 최신순 정렬 최적화
CREATE INDEX "IDX_BOOKS_TITLE" ON "books" ("title");
CREATE INDEX "IDX_BOOKS_CREATED_AT" ON "books" ("created_at" DESC);

-- [Reviews] 특정 도서의 리뷰 목록 조회 (최신순) - 복합 인덱스
-- soft delete를 고려하여 deleted_at이 NULL인 데이터를 빠르게 찾습니다.
CREATE INDEX "IDX_REVIEWS_BOOK_CREATED" ON "reviews" ("book_id", "created_at" DESC)
    WHERE "deleted_at" IS NULL;

-- [Comments] 특정 리뷰의 댓글 목록 조회
CREATE INDEX "IDX_COMMENTS_REVIEW_ID" ON "comments" ("review_id", "created_at" ASC);

-- [Review Likes] 유저가 누른 좋아요 목록 조회
-- 이미 UQ_REVIEW_LIKE_USER가 있으므로 중복 생성은 피함

-- [Notifications] 특정 유저의 미확인 알림 조회 (성능 향상)
-- 알림은 '읽지 않음' 상태를 먼저 필터링하는 경우가 많음
CREATE INDEX "IDX_NOTIFICATIONS_USER_UNCONFIRMED" ON "notifications" ("user_id", "is_confirmed")
    WHERE "is_confirmed" = FALSE;

-- [Dashboard] 랭킹 데이터 필터링 (타입 + 주기 + 점수순)
-- 통계 데이터를 가져올 때 매우 강력한 성능을 발휘합니다.
CREATE INDEX "IDX_DASHBOARD_RANKING" ON "dashboard" ("target_type", "period_type", "score" DESC);

-- [Soft Delete 공통 인덱스]
-- 삭제되지 않은 데이터만 조회하는 쿼리가 많을 경우 유용합니다.
CREATE INDEX "IDX_USERS_ACTIVE" ON "users" ("id") WHERE "deleted_at" IS NULL;
package com.codeit.project.sb08deokhugamteamgwanwoong.dto.review;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;

/*
    1. 검색(완전 일치, 부분 일치)
    - FieldType.Text: 부분 일치
    - FieldType.Keyword: 완전 일치 Ex) 스프링부트 가이드, 스프링부트, 가이드 이런식으로 검색

    2. 검색 부분 일치를 위한 필터
    - 리뷰 내용 (Multi-Field)
    - 기본 검색은 nori(형태소 분석), 한 글자 부분 일치를 위해 ngram 서브 필드로 생성

    3. 정렬에 필요한 내용
     - 정렬에 필요한 것은 createdAt, rating
     - updatedAt은 필요 없음
 */

// 검색에 필요한 데이터만 모아서 ES 읽기 전용 DTO
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "reviews")
@Setting(settingPath = "elastic/es-settings.json")
public class ReviewDocument {

    // UUID 객체 대신 String으로 변환해서 저장하는 것이 보편적
    @Id
    private String id;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "custom_nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "custom_ngram_analyzer", searchAnalyzer = "standard")
            }
    )
    private String content;

    @Field(type = FieldType.Object)
    private BookInfo book;

    @Field(type = FieldType.Object)
    private UserInfo user;

    // 내부 클래스로 정의(엔티티 연관 관계)
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class BookInfo {
        @Field(type = FieldType.Keyword)
        private String id;

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "custom_nori_analyzer"),
                otherFields = {
                        @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "custom_ngram_analyzer", searchAnalyzer = "standard")
                }
        )
        private String title;

        public BookInfo(Book book) {
            this.id = book.getId().toString();
            this.title = book.getTitle();
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class UserInfo {
        @Field(type = FieldType.Keyword)
        private String id;

        @MultiField(
                mainField = @Field(type = FieldType.Text, analyzer = "custom_nori_analyzer"),
                otherFields = {
                        @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "custom_ngram_analyzer", searchAnalyzer = "standard")
                }
        )
        private String nickname;

        public UserInfo(User user) {
            this.id = user.getId().toString();
            this.nickname = user.getNickname();
        }
    }

    @Builder
    public ReviewDocument(Review review) {
        this.id = review.getId().toString();
        this.rating = review.getRating();
        this.createdAt = review.getCreatedAt();
        this.content = review.getContent();
        this.book = new BookInfo(review.getBook());
        this.user = new UserInfo(review.getUser());
    }
}

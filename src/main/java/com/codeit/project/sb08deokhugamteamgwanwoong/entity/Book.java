package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseRemovableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "books")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseRemovableEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "review_count", nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Builder
    public Book(String title, String author, String isbn, String publisher, LocalDate publishedDate, String description, String thumbnailUrl) {
      this.title = title;
      this.author = author;
      this.isbn = isbn;
      this.publisher = publisher;
      this.publishedDate = publishedDate;
      this.description = description;
      this.thumbnailUrl = thumbnailUrl;
    }

  // 도서 정보 수정 (더티 체킹)
  public void update(BookUpdateRequest request) {
    if (request.title() != null) this.title = request.title();
    if (request.author() != null) this.author = request.author();
    if (request.description() != null) this.description = request.description();
    if (request.publisher() != null) this.publisher = request.publisher();
    if (request.publishedDate() != null) this.publishedDate = request.publishedDate();
  }

  // 썸네일 이미지 수정
  public void updateThumbnailUrl(String thumbnailUrl) {
      if (thumbnailUrl != null) this.thumbnailUrl = thumbnailUrl;
  }

  // 리뷰 개수 증가
  public void increaseReviewCount() {
    this.reviewCount++;
  }

  // 리뷰 개수 감소 (삭제 로직을 위해 미리 추가)
  public void decreaseReviewCount() {
    if (this.reviewCount > 0) {
      this.reviewCount--;
    }
  }

  // 새로운 리뷰가 추가될 때 평점과 개수 업데이트
  public void addReviewRating(Integer newRating) {
    // 기존 총점 = 기존 평균 * 기존 리뷰 수
    double totalRating = this.rating * this.reviewCount;

    this.reviewCount++; // 리뷰 수 증가

    // 새로운 평균 = (기존 총점 + 새 리뷰 점수) / 새로운 리뷰 수
    this.rating = Math.round(((totalRating + newRating) / this.reviewCount) * 10) / 10.0; // 소수점 첫째 자리까지
  }

  // 리뷰가 삭제될 때 평점과 개수 업데이트
  public void removeReviewRating(Integer deletedRating) {
    if (this.reviewCount <= 1) {
      this.reviewCount = 0;
      this.rating = 0.0;
      return;
    }
    double totalRating = this.rating * this.reviewCount;
    this.reviewCount--;
    this.rating = Math.round(((totalRating - deletedRating) / this.reviewCount) * 10) / 10.0;
  }
}

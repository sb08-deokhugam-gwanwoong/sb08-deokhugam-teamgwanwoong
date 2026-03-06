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
}

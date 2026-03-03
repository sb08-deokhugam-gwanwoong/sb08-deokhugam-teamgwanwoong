package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseRemovableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

  @Column(columnDefinition = "TEXT")
  private String description;

  @Builder
  public Book(String title, String author, String isbn, String publisher, String description) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.publisher = publisher;
    this.description = description;
  }

}

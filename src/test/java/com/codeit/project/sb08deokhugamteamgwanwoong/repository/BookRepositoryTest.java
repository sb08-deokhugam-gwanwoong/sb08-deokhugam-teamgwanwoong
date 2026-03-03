package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class BookRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private BookRepository bookRepository;

  @DisplayName("도서 정보를 저장한다")
  @Test
  void saveBook() {
    // given
    Book book = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .publishedDate(LocalDate.now())
        .description("자바의 정석 기초편")
        .build();

    // when
    Book savedBook = bookRepository.save(book);

    // then
    assertThat(savedBook.getId()).isNotNull();
    assertThat(savedBook.getTitle()).isEqualTo("자바의 정석");
    assertThat(savedBook.getIsbn()).isEqualTo("9788994492032");
  }

  @DisplayName("ISBN이 중복된 도서는 저장할 수 없다.")
  @Test
  void saveBookWithDuplicateIsbn() {
    // given
    Book book1 = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .publishedDate(LocalDate.now())
        .description("설명1")
        .build();

    bookRepository.save(book1);

    Book book2 = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032") // 같은 ISBN
        .publisher("도우출판")
        .publishedDate(LocalDate.now())
        .description("설명2")
        .build();

    // when & then
    assertThatThrownBy(() -> bookRepository.save(book2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }
}

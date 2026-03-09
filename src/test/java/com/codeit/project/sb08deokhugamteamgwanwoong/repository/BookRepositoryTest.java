package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.project.sb08deokhugamteamgwanwoong.config.QuerydslConfig;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(QuerydslConfig.class)
public class BookRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private BookRepository bookRepository;

  private Book book1;
  private Book book2;
  private Book book3;

  @BeforeEach
  void setUp() throws InterruptedException {
    // 테스트용 도서 데이터 세팅 (생성 시간을 다르게 세팅)
    book1 = bookRepository.save(Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("ISBN001")
        .publisher("도우 출판")
        .description("자바 기본서")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build());

    // ReflectionTestUtils로 private 필드 값 강제 주입
    ReflectionTestUtils.setField(book1, "rating", 4.5);
    ReflectionTestUtils.setField(book1, "reviewCount", 100);
    bookRepository.save(book1);

    Thread.sleep(10);

    book2 = bookRepository.save(Book.builder()
        .title("모던 자바")
        .author("라울")
        .isbn("ISBN002")
        .publisher("한빛 미디어")
        .description("자바 8~11")
        .publishedDate(LocalDate.of(2019, 8, 1))
        .build());
    ReflectionTestUtils.setField(book2, "rating", 5.0);
    ReflectionTestUtils.setField(book2, "reviewCount", 200);
    bookRepository.save(book2);

    Thread.sleep(10);

    book3 = bookRepository.save(Book.builder()
        .title("클린 코드")
        .author("로버트")
        .isbn("ISBN003")
        .publisher("인사이트")
        .description("클린 코드")
        .publishedDate(LocalDate.of(2013, 12, 24))
        .build());
    ReflectionTestUtils.setField(book3, "rating", 4.5);
    ReflectionTestUtils.setField(book3, "reviewCount", 300);
    bookRepository.save(book3);
  }

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
    assertThatThrownBy(() -> bookRepository.saveAndFlush(book2))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  /*
  * 커서 페이징 관련 테스트
  * */
  @DisplayName("키워드(제목, 저자, ISBN)로 검색하면 해당 키워드가 포함된 도서만 조회된다.")
  @Test
  void findAllByCursor_KeywordSearch() {
    // given
    BookSearchCondition condition = BookSearchCondition.builder()
        .keyword("자바") // 자바의 정석, 모던 자바 2개 검색
        .orderBy("createdAt")
        .direction(Sort.Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Book::getTitle).containsExactlyInAnyOrder("자바의 정석", "모던 자바");
  }

  @DisplayName("평점순(rating) 오름차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByRating_Asc() {
    // given
    // 평점 4.5인 자바의 정석(book1)을 기준으로 다음 페이지 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("4.5")
        // ASC이므로 1밀리초 더하기
        .after(book1.getCreatedAt().plus(1, ChronoUnit.MILLIS))
        .orderBy("rating")
        .direction(Sort.Direction.ASC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    // 평점 오름차순(4.5 -> 5.0) 및 동점 시 생성시간 오름차순(book1 -> book3)
    // 따라서 book1 다음은 book3(4.5), 그다음은 book2(5.0)가 나와야 함
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Book::getTitle).containsExactly("클린 코드", "모던 자바");
  }

  @DisplayName("평점순(rating) 내림차순 정렬 시 평점이 높은 순으로 조회된다.")
  @Test
  void findAllByCursor_OrderByRating() {
    // given
    BookSearchCondition condition = BookSearchCondition.builder()
        .orderBy("rating")
        .direction(Sort.Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(3);
    // 모던 자바(5.0) -> 클린 코드(4.5, 최신) -> 자바의 정석(4.5, 과거) 순서 검증
    assertThat(result.get(0).getTitle()).isEqualTo("모던 자바");
  }

  @DisplayName("평점이 동일할 때 커서 기반 페이지네이션이 잘 동작한다.")
  @Test
  void findAllByCursor_CursorPagination_WithSameRating() {
    // given
    // 1페이지에서 모던 자바(5.0)와 클린 코드(4.5)를 읽었다고 가정하고,
    // 다음 페이지를 위해서 클린 코드의 데이터(평점 4.5, 생성 시간)를 커서로 넘김
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("4.5")
        // 나노초 정밀도 차이로 본인이 검색되는 것을 막기 위해 밀리초 단위로 자름
        .after(book3.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)) // 클린코드 생성시간
        .orderBy("rating")
        .direction(Sort.Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    // 똑같은 평점 4.5라도 클린 코드의 생성시간보다 더 이전인 자바의 정석만 반환
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("자바의 정석");
  }

  @DisplayName("리뷰 개수순(reviewCount) 오름차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByReviewCount_Asc() {
    // given
    // 리뷰 200개인 모던 자바를 기준으로 다음 페이지( > 200)를 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("200")
        .after(book2.getCreatedAt().plus(1, ChronoUnit.MILLIS))
        .orderBy("reviewCount")
        .direction(Direction.ASC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("클린 코드"); // 리뷰 300개
  }

  @DisplayName("리뷰개수순(reviewCount) 내림차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByReviewCount_Desc() {
    // given
    // 리뷰 200개인 모던 자바(book2)를 기준으로 다음 페이지(< 200) 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("200")
        // DESC이므로 1밀리초 빼기
        .after(book2.getCreatedAt().minus(1, ChronoUnit.MILLIS))
        .orderBy("reviewCount")
        .direction(Sort.Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("자바의 정석"); // 리뷰 100개
  }

  @DisplayName("출판일순(publishedDate) 오름차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByPublishedDate_Asc() {
    // given
    // 2016년 출판된 자바의 정석(book1)을 기준으로 다음 페이지(> 2016) 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("2016-01-01")
        .after(book1.getCreatedAt().plus(1, ChronoUnit.MILLIS)) // ASC
        .orderBy("publishedDate")
        .direction(Sort.Direction.ASC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("모던 자바"); // 2019년 출판
  }

  @DisplayName("출판일순(publishedDate) 내림차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByPublishedDate_Desc() {
    // given
    // 자바의 정석(2016-01-01) 기준으로 다음 페이지 ( < 2016-01-01 ) 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("2016-01-01")
        .after(book1.getCreatedAt().truncatedTo(ChronoUnit.MILLIS))
        .orderBy("publishedDate")
        .direction(Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("클린 코드"); // 2013년 출판
  }

  @DisplayName("제목순(title) 오름차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByTitle_Asc() {
    // given
    // 모던 자바를 기준으로 다음 페이지(사전순 오름차순) 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("모던 자바")
        //DB의 반올림 시간보다 무조건 커지도록 1밀리초를 더함
        .after(book2.getCreatedAt().plus(1, ChronoUnit.MILLIS))
        .orderBy("title")
        .direction(Direction.ASC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Book::getTitle).containsExactlyInAnyOrder("클린 코드", "자바의 정석");
  }

  @DisplayName("제목순(title) 내림차순 정렬 시 커서 페이징이 정상 동작한다.")
  @Test
  void findAllByCursor_OrderByTitle_Desc() {
    // given
    // 제목 내림차순 정렬 중 자바의 정석(book1)을 기준으로 다음 페이지 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("자바의 정석")
        .after(book1.getCreatedAt().minus(1, ChronoUnit.MILLIS)) // DESC
        .orderBy("title")
        .direction(Sort.Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("모던 자바");
  }

  @DisplayName("커서는 존재하지만 after 값이 null일 경우에도 에러 없이 기본 페이징이 동작한다.")
  @Test
  void findAllByCursor_WithCursor_NullAfter() {
    // given
    // after를 고의로 null 세팅 -> 삼항 연산자의 false 분기
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor("4.5")
        .after(null) // null 처리
        .orderBy("rating")
        .direction(Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    // QueryDSL은 .and(null)을 무시하므로, 평점이 4.5 이하인 모든 책(클린 코드, 자바의 정석)이 정상 조회됨
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Book::getTitle).containsExactlyInAnyOrder("클린 코드", "자바의 정석");
  }

  @DisplayName("기본 정렬(createdAt) 내림차순 조건으로 정상 조회된다.")
  @Test
  void findAllByCursor_DefaultOrderByCreatedAt() {
    // given
    // 생성 시간 자체가 커서가 되는 경우
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor(book2.getCreatedAt().truncatedTo(ChronoUnit.MILLIS).toString())
        .orderBy("createdAt") // 명시적 createdAt
        .direction(Direction.DESC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("자바의 정석"); // book2보다 먼저 만들어진 데이터
  }

  @DisplayName("기본 정렬(createdAt) 오름차순 조건으로 정상 조회된다.")
  @Test
  void findAllByCursor_DefaultOrderByCreatedAt_Asc() {
    // given
    // 모던 자바(book2) 생성 시간을 커서로 하여 다음 생성된 책 조회
    BookSearchCondition condition = BookSearchCondition.builder()
        .cursor(book2.getCreatedAt().plus(1, ChronoUnit.MILLIS).toString())
        .orderBy("createdAt")
        .direction(Sort.Direction.ASC)
        .build();
    Pageable pageable = PageRequest.of(0, 10);

    // when
    List<Book> result = bookRepository.findAllByCursor(condition, pageable);

    // then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getTitle()).isEqualTo("클린 코드"); // book2 다음에 생성된 책
  }
}

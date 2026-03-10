package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class CommentRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private BookRepository bookRepository;
  @Autowired
  private ReviewRepository reviewRepository;
  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private EntityManager entityManager;

  @Test
  @DisplayName("댓글이 정상적으로 등록되어야 한다")
  void saveCommentTest() {

    //given
    User user = User.builder()
        .email("test@test.com")
        .nickname("테스터 one")
        .password("testPass1234!")
        .build();
    User savedUser = userRepository.save(user);

    Book book = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .publishedDate(LocalDate.now())
        .description("자바의 정석 기초편")
        .build();
    Book savedBook = bookRepository.save(book);

    Review review = Review.builder()
        .rating(5)
        .content("정말 재밌어요!!!")
        .user(user)
        .book(book)
        .build();
    Review savedReview = reviewRepository.save(review);

    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content("테스트 comment 입니다")
        .build();

    //when
    Comment savedComment = commentRepository.save(comment);

    //then
    assertThat(savedComment.getId()).isNotNull();
    assertThat(savedComment.getContent()).isEqualTo("테스트 comment 입니다");
  }

  @Test
  @DisplayName("댓글 등록 시 내용이 없으면 에러가 발생해야 한다")
  void saveCommentWithoutContentFail() {
    //given
    User user = User.builder()
        .email("test12@test.com")
        .nickname("테스터 박")
        .password("testPass1234!")
        .build();
    User savedUser = userRepository.save(user);

    Book book = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publishedDate(LocalDate.now())
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .build();
    Book savedBook = bookRepository.save(book);

    Review review = Review.builder()
        .rating(5)
        .content("정말 재밌어요!!!")
        .user(user)
        .book(book)
        .build();
    Review savedReview = reviewRepository.save(review);

    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content(null)
        .build();

    //when & then
    assertThatThrownBy(() -> commentRepository.saveAndFlush(comment))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("댓글 내용을 수정하면 정상적으로 반영되어야 한다")
  void updateCommentTest() {
    //given
    User user = User.builder()
        .email("test12@test.com")
        .nickname("테스터 박")
        .password("testPass1234!")
        .build();
    User savedUser = userRepository.save(user);

    Book book = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publishedDate(LocalDate.now())
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .build();
    Book savedBook = bookRepository.save(book);

    Review review = Review.builder()
        .rating(5)
        .content("정말 재밌어요!!!")
        .user(user)
        .book(book)
        .build();
    Review savedReview = reviewRepository.save(review);

    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content("수정 전 내용입니다")
        .build();
    commentRepository.save(comment);

    //when
    comment.updateContent("수정 후 내용입니다");

    commentRepository.flush();
    entityManager.clear();

    //then
    Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
    assertThat(updatedComment.getContent()).isEqualTo("수정 후 내용입니다");
  }

  @Test
  @DisplayName("댓글 내용이 500자를 초과하면 예외가 발생해야 한다")
  void saveCommentOverLengthFail() {
    //given
    User user = User.builder()
        .email("test123@test.com")
        .nickname("테스터 김")
        .password("testPass1234!")
        .build();
    userRepository.save(user);

    Book book = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publishedDate(LocalDate.now())
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .build();
    bookRepository.save(book);

    Review review = Review.builder()
        .rating(5)
        .content("정말 재밌어요!!!")
        .user(user)
        .book(book)
        .build();
    reviewRepository.save(review);

    //when
    String content = "a".repeat(501);
    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content(content)
        .build();

    //then
    assertThatThrownBy(() -> {
      commentRepository.save(comment);
      commentRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("댓글 삭제 시 논리 삭제가 적용되어야 한다")
  void softDeleteCommentTest() {
    //given
    User user = userRepository.save(User.builder().email("soft@test.com").nickname("논리삭제").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97901").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    Comment comment = commentRepository.save(Comment.builder()
        .user(user)
        .review(review)
        .content("삭제될 댓글")
        .build());
    commentRepository.flush();

    // when
    comment.delete();
    commentRepository.flush();
    entityManager.clear();

    // then
    Comment found = commentRepository.findById(comment.getId()).orElseThrow();
    assertThat(found.getDeletedAt()).isNotNull();
    assertThat(found.getContent()).isEqualTo("삭제될 댓글");
  }

  @Test
  @DisplayName("특정 리뷰의 댓글 목록을 페이징하여 조회한다")
  void findAllByReviewIdPaginationTest() {
    //given
    User user = userRepository.save(User.builder().email("page@test.com").nickname("페이징").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97902").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    for (int i = 1; i <= 10; i++) {
      commentRepository.save(Comment.builder()
          .user(user)
          .review(review)
          .content("댓글 " + i)
          .build());
    }
    commentRepository.flush();
    entityManager.clear();

    //when 첫 번째 페이지 조회 (사이즈 5)
    CommentSearchCondition condition = new CommentSearchCondition(review.getId(), null, null, 5);
    List<Comment> comments = commentRepository.findAllByCursor(condition);

    //then
    assertThat(comments).hasSize(6);
    assertThat(comments.get(0).getContent()).isEqualTo("댓글 10"); // 최신순(DESC) 정렬 검증
  }

  @Test
  @DisplayName("특정 리뷰의 모든 댓글을 논리 삭제한다")
  void softDeleteAllByReviewIdTest() {
    //given
    User user = userRepository.save(User.builder().email("softall@test.com").nickname("전체논리삭제").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97903").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 1").build());
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 2").build());
    commentRepository.flush();

    //when
    commentRepository.softDeleteAllByReviewId(review.getId(), Instant.now());
    entityManager.clear();

    //then
    List<Comment> comments = commentRepository.findAllByReviewId(review.getId());
    assertThat(comments).allMatch(comment -> comment.getDeletedAt() != null);
  }

  @Test
  @DisplayName("특정 리뷰의 모든 댓글을 물리 삭제한다")
  void hardDeleteAllByReviewIdTest() {
    //given
    User user = userRepository.save(User.builder().email("hardall@test.com").nickname("전체물리삭제").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97904").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 1").build());
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 2").build());
    commentRepository.flush();

    //when
    commentRepository.hardDeleteAllByReviewId(review.getId());
    entityManager.clear();

    //then
    List<Comment> comments = commentRepository.findAllByReviewId(review.getId());
    assertThat(comments).isEmpty();
  }

  @Test
  @DisplayName("특정 리뷰의 모든 댓글을 조회한다")
  void findAllByReviewIdTest() {
    //given
    User user = userRepository.save(User.builder().email("findall@test.com").nickname("전체조회").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97905").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 1").build());
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 2").build());
    commentRepository.flush();
    entityManager.clear();

    //when
    List<Comment> comments = commentRepository.findAllByReviewId(review.getId());

    //then
    assertThat(comments).hasSize(2);
  }

  @Test
  @DisplayName("커서 기반 페이징 조회 시 삭제된 댓글은 결과에서 제외되어야 한다")
  void findAllByCursorExcludeDeletedTest() {
    //given
    User user = userRepository.save(User.builder().email("cursor_del@test.com").nickname("커서삭제").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97906").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    Comment comment1 = commentRepository.save(Comment.builder().user(user).review(review).content("댓글 1").build());
    try { Thread.sleep(100); } catch (InterruptedException e) {}
    Comment comment2 = commentRepository.save(Comment.builder().user(user).review(review).content("댓글 2").build());
    try { Thread.sleep(100); } catch (InterruptedException e) {}
    Comment comment3 = commentRepository.save(Comment.builder().user(user).review(review).content("댓글 3").build());
    commentRepository.flush();

    //when
    comment2.delete();
    commentRepository.flush();
    entityManager.clear();

    CommentSearchCondition condition = new CommentSearchCondition(review.getId(), null, null, 10);
    List<Comment> comments = commentRepository.findAllByCursor(condition);

    //then
    assertThat(comments).hasSize(2);
    assertThat(comments).extracting("content").containsExactly("댓글 3", "댓글 1");
  }

  @Test
  @DisplayName("커서 기반 페이징 조회 시 다음 페이지를 정상적으로 조회해야 한다")
  void findAllByCursorNextPageTest() {
    //given
    User user = userRepository.save(User.builder().email("cursor_next@test.com").nickname("커서다음").password("pass1234!").build());
    Book book = bookRepository.save(Book.builder().title("테스트 책").author("테스트 작가").isbn("97907").description("책 설명").publisher("테스트 출판사").publishedDate(LocalDate.now()).build());
    Review review = reviewRepository.save(Review.builder().rating(5).content("조와용").user(user).book(book).build());

    for (int i = 1; i <= 10; i++) {
      commentRepository.save(Comment.builder().user(user).review(review).content("댓글 " + i).build());
      try {
        Thread.sleep(100); // 10ms -> 100ms로 늘려서 시간 차이를 확실하게 둠
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    commentRepository.flush();
    entityManager.clear();

    //when
    CommentSearchCondition firstCondition = new CommentSearchCondition(review.getId(), null, null, 5);
    List<Comment> firstPage = commentRepository.findAllByCursor(firstCondition);

    // 마지막 요소의 createdAt을 커서로 사용
    String cursor = firstPage.get(4).getCreatedAt().toString();
    Instant after = firstPage.get(4).getCreatedAt();

    // after에도 값을 넣어줌
    CommentSearchCondition secondCondition = new CommentSearchCondition(review.getId(), cursor, after, 5);
    List<Comment> secondPage = commentRepository.findAllByCursor(secondCondition);

    //then
    assertThat(secondPage).hasSize(5);
    assertThat(secondPage.get(0).getContent()).isEqualTo("댓글 5");
    assertThat(secondPage.get(4).getContent()).isEqualTo("댓글 1");
  }
}

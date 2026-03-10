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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

public class CommentRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private EntityManager entityManager;

  private User user;
  private Book book;
  private Review review;

  @BeforeEach
  public void setUp() {
    user = createUser("test@codeit.com", "testUser");
    book = createBook("testBook", "testAuthor", "9788994492032", "testPublisher", "testDescription",
        "testThumbnailUrl");
    review = createReview(5, "정말 재밌어요!!!", user, book);

    entityManager.persist(user);
    entityManager.persist(book);
    entityManager.persist(review);

    flushAndClear();
  }

  @Test
  @DisplayName("댓글이 정상적으로 등록되어야 한다")
  void saveCommentTest() {
    //given
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
    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content("수정 전 내용입니다")
        .build();
    commentRepository.save(comment);

    //when
    comment.updateContent("수정 후 내용입니다");
    flushAndClear();

    //then
    Comment updatedComment = commentRepository.findById(comment.getId()).orElseThrow();
    assertThat(updatedComment.getContent()).isEqualTo("수정 후 내용입니다");
  }

  @Test
  @DisplayName("댓글 내용이 500자를 초과하면 예외가 발생해야 한다")
  void saveCommentOverLengthFail() {
    //given
    String content = "a".repeat(501);
    Comment comment = Comment.builder()
        .user(user)
        .review(review)
        .content(content)
        .build();

    //when & then
    assertThatThrownBy(() -> {
      commentRepository.save(comment);
      commentRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("댓글 삭제 시 논리 삭제가 적용되어야 한다")
  void softDeleteCommentTest() {
    //given
    Comment comment = commentRepository.save(Comment.builder()
        .user(user)
        .review(review)
        .content("삭제될 댓글")
        .build());
    commentRepository.flush();

    // when
    comment.delete();
    flushAndClear();

    // then
    Comment found = commentRepository.findById(comment.getId()).orElseThrow();
    assertThat(found.getDeletedAt()).isNotNull();
    assertThat(found.getContent()).isEqualTo("삭제될 댓글");
  }

  @Test
  @DisplayName("특정 리뷰의 댓글 목록을 페이징하여 조회한다")
  void findAllByReviewIdPaginationTest() throws Exception {
    //given
    for (int i = 1; i <= 10; i++) {
      commentRepository.save(Comment.builder()
          .user(user)
          .review(review)
          .content("댓글 " + i)
          .build());
        Thread.sleep(100);
    }
    flushAndClear();

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
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 1").build());
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 2").build());
    commentRepository.flush();

    //when
    commentRepository.softDeleteAllByReviewId(review.getId(), Instant.now());
    entityManager.clear();

    //then
    List<Comment> comments = commentRepository.findAllByReviewId(review.getId());
    assertThat(comments).allMatch(c -> c.getDeletedAt() != null);
  }

  @Test
  @DisplayName("특정 리뷰의 모든 댓글을 물리 삭제한다")
  void hardDeleteAllByReviewIdTest() {
    //given
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
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 1").build());
    commentRepository.save(Comment.builder().user(user).review(review).content("댓글 2").build());
    flushAndClear();

    //when
    List<Comment> comments = commentRepository.findAllByReviewId(review.getId());

    //then
    assertThat(comments).hasSize(2);
  }

  @Test
  @DisplayName("커서 기반 페이징 조회 시 삭제된 댓글은 결과에서 제외되어야 한다")
  void findAllByCursorExcludeDeletedTest() throws Exception {
    //given
    Comment comment1 = commentRepository.save(
        Comment.builder().user(user).review(review).content("댓글 1").build());
      Thread.sleep(100);

    Comment comment2 = commentRepository.save(
        Comment.builder().user(user).review(review).content("댓글 2").build());
      Thread.sleep(100);

    Comment comment3 = commentRepository.save(
        Comment.builder().user(user).review(review).content("댓글 3").build());
    commentRepository.flush();

    //when
    comment2.delete();
    flushAndClear();

    CommentSearchCondition condition = new CommentSearchCondition(review.getId(), null, null, 10);
    List<Comment> comments = commentRepository.findAllByCursor(condition);

    //then
    assertThat(comments).hasSize(2);
    assertThat(comments).extracting("content").containsExactly("댓글 3", "댓글 1");
  }

  @Test
  @DisplayName("커서 기반 페이징 조회 시 다음 페이지를 정상적으로 조회해야 한다")
  void findAllByCursorNextPageTest() throws Exception {
    //given
    for (int i = 1; i <= 10; i++) {
      commentRepository.save(
          Comment.builder().user(user).review(review).content("댓글 " + i).build());
        Thread.sleep(100);
    }
    flushAndClear();

    //when
    CommentSearchCondition firstCondition = new CommentSearchCondition(review.getId(), null, null,
        5);
    List<Comment> firstPage = commentRepository.findAllByCursor(firstCondition);

    // 마지막 요소의 createdAt을 커서로 사용
    String cursor = firstPage.get(4).getCreatedAt().toString();
    Instant after = firstPage.get(4).getCreatedAt();

    // after에도 값을 넣어줌
    CommentSearchCondition secondCondition = new CommentSearchCondition(review.getId(), cursor,
        after, 5);
    List<Comment> secondPage = commentRepository.findAllByCursor(secondCondition);

    //then
    assertThat(secondPage).hasSize(5);
    assertThat(secondPage.get(0).getContent()).isEqualTo("댓글 5");
    assertThat(secondPage.get(4).getContent()).isEqualTo("댓글 1");
  }

  //
  private User createUser(String email, String nickname) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password("testPassword!")
        .build();
  }

  private Book createBook(String title, String author, String isbn, String publisher,
      String description, String thumbnailUrl) {
    return Book.builder()
        .title(title)
        .author(author)
        .isbn(isbn)
        .publisher(publisher)
        .publishedDate(LocalDate.now())
        .description(description)
        .thumbnailUrl(thumbnailUrl)
        .build();
  }

  private Review createReview(int rating, String content, User author, Book book) {
    return Review.builder()
        .rating(rating)
        .content(content)
        .user(author)
        .book(book)
        .build();
  }

  private void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }

}

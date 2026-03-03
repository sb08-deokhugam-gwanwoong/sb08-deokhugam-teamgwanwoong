package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private BookRepository bookRepository;
  @Autowired
  private ReviewRepository reviewRepository;
  @Autowired
  private CommentRepository commentRepository;

  @Test
  @DisplayName("댓글이 정상적으로 등록되어야 한다")
  void saveCommentTest() {

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
}

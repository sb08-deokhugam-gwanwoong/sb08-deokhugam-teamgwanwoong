package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.CommentServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private CommentServiceImpl commentService;

  private UUID userId;
  private UUID bookId;
  private UUID reviewId;
  private User user;
  private Book book;
  private Review review;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    bookId = UUID.randomUUID();
    reviewId = UUID.randomUUID();

    user = User.builder()
        .email("test@test.com")
        .nickname("웅제")
        .password("testPassword123!")
        .build();
    ReflectionTestUtils.setField(user, "id", userId);

    book = Book.builder()
        .title("testBook")
        .author("testAuthor")
        .isbn("9788994492032")
        .publisher("testPublisher")
        .publishedDate(LocalDate.now())
        .description("testDescription")
        .thumbnailUrl("https://test-thumbnail.url/image.jpg")
        .build();
    ReflectionTestUtils.setField(book, "id", bookId);

    review = Review.builder()
        .rating(5)
        .content("좋은 책 입니다")
        .user(user)
        .book(book)
        .build();
    ReflectionTestUtils.setField(review, "id", reviewId);
  }

  @Test
  @DisplayName("댓글 작성 성공")
  void createComment_Success() {

    //given
    CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "테스트 댓글입니다");

    Comment comment = Comment.builder()
        .content(request.content())
        .user(user)
        .review(review)
        .build();
    ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());

    //stubbing
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
    given(commentRepository.save(any(Comment.class))).willReturn(comment);

    //when
    CommentDto result = commentService.create(request);

    //then
    assertThat(result.content()).isEqualTo("테스트 댓글입니다");
    assertThat(result.userNickname()).isEqualTo("웅제");

    verify(commentRepository, times(1)).save(any(Comment.class));
  }
}
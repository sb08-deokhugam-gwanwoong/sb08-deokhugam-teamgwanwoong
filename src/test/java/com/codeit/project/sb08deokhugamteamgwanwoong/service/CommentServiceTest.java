package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.CommentErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.CommentMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.CommentServiceImpl;
import java.time.Instant;
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
  @Mock
  private CommentMapper commentMapper;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private CommentServiceImpl commentService;

  private UUID userId;
  private UUID bookId;
  private UUID reviewId;
  private User user;
  private Book book;
  private Review review;
  private Comment comment;

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

    comment = Comment.builder()
        .content("기존 댓글 내용")
        .user(user)
        .review(review)
        .build();
    ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
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

    CommentDto expectedDto = new CommentDto(
        comment.getId(),
        reviewId,
        userId,
        "웅제",
        "테스트 댓글입니다",
        Instant.now(),
        Instant.now()
    );
    given(commentMapper.toDto(any(Comment.class))).willReturn(expectedDto);

    //when
    CommentDto result = commentService.create(request);

    //then
    assertThat(result.content()).isEqualTo("테스트 댓글입니다");
    assertThat(result.userNickname()).isEqualTo("웅제");

    then(commentRepository).should(times(1)).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void updateComment_Success() {
    //given
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("수정된 댓글 내용입니다.");
    UUID commentId = comment.getId();

    //Stubbing
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    CommentDto expectedDto = new CommentDto(
        comment.getId(),
        reviewId,
        userId,
        "웅제",
        "수정된 댓글 내용입니다.",
        Instant.now(),
        Instant.now()
    );
    given(commentMapper.toDto(any(Comment.class))).willReturn(expectedDto);

    //when
    CommentDto updatedComment = commentService.update(commentId, userId, updateRequest);

    //then
    assertThat(updatedComment.content()).isEqualTo("수정된 댓글 내용입니다.");
    assertThat(updatedComment.userNickname()).isEqualTo("웅제");

    assertThat(comment.getContent()).isEqualTo("수정된 댓글 내용입니다.");
  }

  @Test
  @DisplayName("댓글 수정 실패 - 작성자가 아닐 경우 예외 발생")
  void updateComment_Fail_Unauthorized() {
    // given
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("불법 수정 시도!");

    UUID commentId = comment.getId();
    UUID anotherUserId = UUID.randomUUID();

    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

    // when & then
    assertThatThrownBy(() ->
        commentService.update(commentId, anotherUserId, updateRequest))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(CommentErrorCode.COMMENT_UPDATE_DENIED);
  }

  @Test
  @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글일 경우 예외 발생")
  void updateComment_Fail_NotFound() {
    //given
    UUID notFoundId = UUID.randomUUID();
    CommentUpdateRequest updateRequest = new CommentUpdateRequest("빈 내용");

    given(commentRepository.findById(notFoundId)).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() ->
        commentService.update(notFoundId, userId, updateRequest))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);
  }
}
package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CursorPageResponseCommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.CommentErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.CommentMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.CommentServiceImpl;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

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
    ReflectionTestUtils.setField(comment, "createdAt", Instant.now());
  }


  @Nested
  @DisplayName("댓글 생성")
  class CreateComment {

    @Test
    @DisplayName("성공 : 댓글 작성 ")
    void createComment_Success() {
      //given
      CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "테스트 댓글입니다");

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
    @DisplayName("성공 : 남이 내 리뷰에 댓글을 달면 알림이 생성되어야 한다")
    void createComment_ShouldNotify_WhenOtherUserComments() {
      //given
      User reviewAuthor = User.builder()
          .email("author@test.com")
          .nickname("리뷰어")
          .build();
      ReflectionTestUtils.setField(reviewAuthor, "id", UUID.randomUUID()); // 서로 다른 ID 보장

      ReflectionTestUtils.setField(review, "user", reviewAuthor);

      CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "알림 가는지 확인!");

      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(commentRepository.save(any(Comment.class))).willReturn(comment);

      CommentDto dto = new CommentDto(comment.getId(), reviewId, userId, "웅제", "알림!", Instant.now(), Instant.now());
      given(commentMapper.toDto(any(Comment.class))).willReturn(dto);

      //when
      commentService.create(request);

      //then
      then(notificationService).should(times(1)).createNotification(any(), any(), any());
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 리뷰에 댓글을 달 경우 예외 발생")
    void createComment_Fail_ReviewNotFound() {
      //given
      CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "테스트");

      // 유저는 존재하지만, 리뷰는 존재하지 않음!
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> commentService.create(request))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode")
          .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("댓글 수정")
  class UpdateComment {

    @Test
    @DisplayName("성공 : 댓글 수정")
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
    @DisplayName("실패 : 작성자가 아닐 경우 댓글 수정 시 예외 발생")
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
    @DisplayName("실패 : 존재하지 않는 댓글을 수정 할 경우 예외 발생")
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

  @Nested
  @DisplayName("댓글 조회")
  class GetComments {

    @Test
    @DisplayName("성공 : 댓글 목록 조회")
    void findAllComments_Success() {
      //given
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

      List<Comment> comments = List.of(comment);
      given(commentRepository.findAllByCursor(any(CommentSearchCondition.class))).willReturn(comments);

      CommentDto commentDto = new CommentDto(
          comment.getId(),
          reviewId,
          userId,
          "웅제",
          "기존 댓글 내용",
          Instant.now(),
          Instant.now()
      );
      given(commentMapper.toDto(any(Comment.class))).willReturn(commentDto);

      //when
      CursorPageResponseCommentDto response = commentService.findAllComments(reviewId, null, null, 10);

      //then
      assertThat(response.content()).hasSize(1);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.content().get(0).content()).isEqualTo("기존 댓글 내용");

      then(reviewRepository).should(times(1)).findById(reviewId);
      then(commentMapper).should(times(1)).toDto(comment);
    }

    @Test
    @DisplayName("실패 : 리뷰가 존재하지 않을 경우 예외 발생")
    void findAllComments_Fail_ReviewNotFound() {
      //given
      UUID notFoundReviewId = UUID.randomUUID();
      given(reviewRepository.findById(notFoundReviewId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> commentService.findAllComments(notFoundReviewId, null, null, 10))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode")
          .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("성공 : 댓글 상세 조회 시 해당 댓글의 DTO를 반환한다")
    void findById_Success() {
      //given
      UUID targetId = comment.getId(); // commentId 대신 필드의 comment에서 ID 추출

      CommentDto expectedDto = new CommentDto(
          targetId, reviewId, userId, "웅제", "기존 댓글 내용", Instant.now(), Instant.now()
      );

      given(commentRepository.findById(targetId)).willReturn(Optional.of(comment));
      given(commentMapper.toDto(comment)).willReturn(expectedDto);

      //when
      CommentDto result = commentService.findById(targetId);

      //then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(targetId);
      then(commentRepository).should().findById(targetId);
    }

    @Test
    @DisplayName("성공 : 다음 페이지가 존재하는 경우")
    void findAllComments_HasNext_True() {
      // given
      List<Comment> comments = List.of(comment, comment);
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(commentRepository.findAllByCursor(any())).willReturn(comments);

      // when
      CursorPageResponseCommentDto response = commentService.findAllComments(reviewId, null, null, 1);

      // then
      assertThat(response.hasNext()).isTrue();
      assertThat(response.content()).hasSize(1);
    }

    @Test
    @DisplayName("성공 : 조회 결과가 아예 없는 경우")
    void findAllComments_EmptyResult() {
      // given
      given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
      given(commentRepository.findAllByCursor(any())).willReturn(List.of());

      // when
      CursorPageResponseCommentDto response = commentService.findAllComments(reviewId, null, null, 10);

      // then
      assertThat(response.content()).isEmpty();
      assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 댓글일 경우 예외 발생")
    void findById_Fail_NotFound() {
      //given
      UUID notFoundId = UUID.randomUUID();
      given(commentRepository.findById(notFoundId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> commentService.findById(notFoundId))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode")
          .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("댓글 삭제")
  class DeleteComment {
    @Test
    @DisplayName("성공 : 댓글 논리 삭제")
    void softDelete_Success() {
      //given
      UUID commentId = comment.getId();
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

      //when
      commentService.softDelete(commentId, userId);

      //then
      assertThat(comment.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("실패 : 작성자가 아닐 경우 댓글 삭제 시 예외 발생")
    void softDelete_Fail_Unauthorized() {
      //given
      UUID commentId = comment.getId();
      UUID anotherUserId = UUID.randomUUID();
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

      //when & then
      assertThatThrownBy(() -> commentService.softDelete(commentId, anotherUserId))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode")
          .isEqualTo(CommentErrorCode.COMMENT_DELETE_DENIED);
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 댓글을 삭제 할 경우 예외 발생")
    void softDelete_Fail_NotFound() {
      //given
      UUID notFoundId = UUID.randomUUID();
      given(commentRepository.findById(notFoundId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> commentService.softDelete(notFoundId, userId))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode")
          .isEqualTo(CommentErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("성공 : 댓글 물리 삭제")
    void hardDelete_Success() {
      //given
      UUID commentId = comment.getId();
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

      //when
      commentService.hardDelete(commentId, userId);

      //then
      then(commentRepository).should(times(1)).delete(comment);
    }

    @Test
    @DisplayName("실패 : 작성자가 아닐 경우 댓글 삭제 시 예외 발생")
    void hardDelete_Fail_Unauthorized() {
      //given
      UUID commentId = comment.getId();
      UUID anotherUserId = UUID.randomUUID();
      given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

      //when & then
      assertThatThrownBy(() -> commentService.hardDelete(commentId, anotherUserId))
          .isInstanceOf(BusinessException.class)
          .extracting("errorCode")
          .isEqualTo(CommentErrorCode.COMMENT_DELETE_DENIED);
    }
  }


}
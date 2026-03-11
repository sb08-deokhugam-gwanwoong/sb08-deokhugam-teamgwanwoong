package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CursorPageResponseCommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.CommentErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.CommentMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.CommentService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public CommentDto create(CommentCreateRequest request) {
    log.info("[댓글 등록] 시작 - userId: {}, reviewId: {}", request.userId(), request.reviewId());

    User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    Review review = reviewRepository.findById(request.reviewId())
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

    Comment comment = Comment.builder()
            .content(request.content())
            .user(user)
            .review(review)
            .build();

    Comment savedComment = commentRepository.saveAndFlush(comment);
    reviewRepository.increaseCommentCount(review.getId());

    User reviewAuthor = review.getUser();
    if (!reviewAuthor.getId().equals(user.getId())) {
      log.info("[알림] 알림 발송 - 수신자: {}, 발신자: {}", reviewAuthor.getId(), user.getId());
      String message = String.format("[%s]님이 내 리뷰에 댓글을 남겼습니다.", user.getNickname());
      notificationService.createNotification(reviewAuthor, review, message);
    }

    log.info("[댓글 등록] 완료 - commentId: {}", savedComment.getId());
    return commentMapper.toDto(savedComment);
  }

  @Override
  public CursorPageResponseCommentDto findAllComments(UUID reviewId, String cursor, Instant after, int size) {
    log.info("[댓글 목록 조회] 시작 - 리뷰ID: {}, 커서: {}, 사이즈: {}", reviewId, cursor, size);

    Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

    CommentSearchCondition condition = CommentSearchCondition.builder()
            .reviewId(reviewId)
            .cursor(cursor)  // String 타입
            .after(after)    // Instant 타입
            .limit(size)
            .build();

    List<Comment> comments = commentRepository.findAllByCursor(condition);

    boolean hasNext = comments.size() > size;
    List<Comment> resultComments = hasNext ? comments.subList(0, size) : comments;

    List<CommentDto> content = resultComments.stream()
            .map(commentMapper::toDto)
            .toList();

    String nextCursor = resultComments.isEmpty() ? null : resultComments.get(resultComments.size() - 1).getCreatedAt().toString();
    Instant nextAfter = resultComments.isEmpty() ? null : resultComments.get(resultComments.size() - 1).getCreatedAt();

    log.info("[댓글 목록 조회] 완료 - 조회된 개수: {}, 다음 페이지 존재: {}", content.size(), hasNext);

    return new CursorPageResponseCommentDto(
            content,
            nextCursor,
            nextAfter,
            size,
            review.getCommentCount().longValue(),
            hasNext
    );
  }

  @Override
  public CommentDto findById(UUID commentId) {
    log.info("[댓글 상세 조회] 시작 - 댓글ID: {}", commentId);
    Comment comment = findComment(commentId);

    log.info("[댓글 상세 조회] 완료 - 댓글ID: {}, 작성자: {}", commentId, comment.getUser().getNickname());
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request) {
    log.info("[댓글 수정] 시작 - 댓글ID: {}, 유저ID: {}", commentId, userId);

    Comment comment = findComment(commentId);
    validateCommentOwner(comment, userId, CommentErrorCode.COMMENT_UPDATE_DENIED);

    comment.updateContent(request.content());

    log.info("[댓글 수정] 완료 - 댓글ID: {}", commentId);
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public void softDelete(UUID commentId, UUID userId) {
    log.info("[댓글 논리 삭제] 시작 - 댓글ID: {}, 유저ID: {}", commentId, userId);

    Comment comment = findComment(commentId);
    validateCommentOwner(comment, userId, CommentErrorCode.COMMENT_DELETE_DENIED);

    comment.delete();
    commentRepository.flush();
    reviewRepository.decreaseCommentCount(comment.getReview().getId());

    log.info("[댓글 논리 삭제] 완료 - 댓글ID: {}", commentId);
  }

  @Override
  @Transactional
  public void hardDelete(UUID commentId, UUID userId) {
    log.info("[댓글 물리 삭제] 시작 - 댓글ID: {}, 유저ID: {}", commentId, userId);

    Comment comment = findComment(commentId);
    validateCommentOwner(comment, userId, CommentErrorCode.COMMENT_DELETE_DENIED);

    commentRepository.delete(comment);
    commentRepository.flush();

    //논리 삭제된 상태가 아닐 때만 감소시키려면 체크 로직 필요(중요)
    if (comment.getDeletedAt() == null) {
      reviewRepository.decreaseCommentCount(comment.getReview().getId());
    }

    log.info("[댓글 물리 삭제] 완료 - 댓글ID: {}", commentId);
  }

  // 댓글 존재 여부 확인 공통 메서드
  private Comment findComment(UUID commentId) {
    return commentRepository.findById(commentId)
            .orElseThrow(() -> {
              log.warn("[조회 실패] 존재하지 않는 댓글 - 댓글ID: {}", commentId);
              return new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
            });
  }

  // 작성자 본인 확인 공통 메서드
  private void validateCommentOwner(Comment comment, UUID userId, CommentErrorCode errorCode) {
    if (!comment.getUser().getId().equals(userId)) {
      log.warn("[권한 거부] 작성자 불일치 - 댓글ID: {}, 요청유저: {}, 실제작성자: {}",
              comment.getId(), userId, comment.getUser().getId());
      throw new BusinessException(errorCode);
    }
  }
}


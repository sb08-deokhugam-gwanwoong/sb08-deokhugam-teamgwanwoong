package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  @Override
  @Transactional
  public CommentDto create(CommentCreateRequest request) {

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    Review review = reviewRepository.findById(request.reviewId())
        .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

    Comment comment = Comment.builder()
        .content(request.content())
        .user(user)
        .review(review)
        .build();

    Comment savedComment = commentRepository.save(comment);

    review.increaseCommentCount();

    //TODO 알림 기능 머지 후 주석 해제 예정
//    User reviewAuthor = review.getUser();
//    if (!reviewAuthor.getId().equals(user.getId())) {
//      String message = String.format("[%s]님이 내 리뷰에 댓글을 남겼습니다.", user.getNickname());
//      notificationService.createNotification(reviewAuthor, review, message);

    return commentMapper.toDto(savedComment);
  }

  @Override
  public CursorPageResponseCommentDto findAllComments(UUID reviewId, Instant cursorCreatedAt, int size) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

    PageRequest pageable = PageRequest.of(0, size + 1);
    List<Comment> comments = commentRepository.findCommentsByCursor(reviewId, cursorCreatedAt, pageable);

    boolean hasNext = comments.size() > size;
    List<Comment> resultComments = hasNext ? comments.subList(0, size) : comments;

    List<CommentDto> content = resultComments.stream()
        .map(commentMapper::toDto)
        .toList();

    UUID nextCursor = resultComments.isEmpty() ? null : resultComments.get(resultComments.size() - 1).getId();
    Instant nextAfter = resultComments.isEmpty() ? null : resultComments.get(resultComments.size() - 1).getCreatedAt();

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
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));
    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request) {

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new BusinessException(CommentErrorCode.COMMENT_UPDATE_DENIED);
    }

    comment.updateContent(request.content());

    return commentMapper.toDto(comment);
  }

  @Override
  @Transactional
  public void softDelete(UUID commentId, UUID userId) {

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new BusinessException(CommentErrorCode.COMMENT_DELETE_DENIED);
    }

    comment.delete();
    comment.getReview().decreaseCommentCount();
  }

  @Override
  @Transactional
  public void hardDelete(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new BusinessException(CommentErrorCode.COMMENT_DELETE_DENIED);
    }

    //논리 삭제된 상태가 아닐 때만 감소시키려면 체크 로직 필요(중요)
    if (comment.getDeletedAt() == null) {
      comment.getReview().decreaseCommentCount();
    }

    commentRepository.delete(comment);
  }
}


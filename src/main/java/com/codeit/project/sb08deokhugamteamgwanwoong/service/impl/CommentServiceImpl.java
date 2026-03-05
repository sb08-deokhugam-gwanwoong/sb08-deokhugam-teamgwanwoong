package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.CommentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;

  @Override
  @Transactional
  public CommentDto create(CommentCreateRequest request) {

    //TODO : BusinessException 및 CommentErrorCode 리팩토링 처리 예정
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new RuntimeException("USER NOT FOUND"));

    Review review = reviewRepository.findById(request.reviewId())
        .orElseThrow(() -> new RuntimeException("REVIEW NOT FOUND"));

    Comment comment = Comment.builder()
        .content(request.content())
        .user(user)
        .review(review)
        .build();

    Comment savedComment = commentRepository.save(comment);

    return new CommentDto(
        savedComment.getId(),
        review.getId(),
        user.getId(),
        user.getNickname(),
        savedComment.getContent(),
        savedComment.getCreatedAt(),
        savedComment.getUpdatedAt()
    );
  }

  @Override
  @Transactional
  public CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request) {

    //TODO : BusinessException 및 CommentErrorCode 리팩토링 처리 예정
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("COMMENT NOT FOUND"));

    if (!comment.getUser().getId().equals(userId)) {
      throw new RuntimeException("UNAUTHORIZED");
    }

    comment.updateContent(request.content());

    return new CommentDto(
        comment.getId(),
        comment.getReview().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getCreatedAt(),
        comment.getUpdatedAt()
    );
  }
}


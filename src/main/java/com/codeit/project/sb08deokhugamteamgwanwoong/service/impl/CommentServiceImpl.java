package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.CommentErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.CommentMapper;
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
  private final CommentMapper commentMapper;

  @Override
  @Transactional
  public CommentDto create(CommentCreateRequest request) {

    //TODO : User error code 만들어지면 적용 예정
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new RuntimeException("USER NOT FOUND"));

    Review review = reviewRepository.findById(request.reviewId())
        .orElseThrow(() -> new BusinessException(CommentErrorCode.REVIEW_NOT_FOUND));

    Comment comment = Comment.builder()
        .content(request.content())
        .user(user)
        .review(review)
        .build();

    Comment savedComment = commentRepository.save(comment);

    return commentMapper.toDto(savedComment);
  }

  @Override
  @Transactional
  public CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request) {

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new BusinessException(CommentErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    comment.updateContent(request.content());

    return commentMapper.toDto(comment);
  }
}


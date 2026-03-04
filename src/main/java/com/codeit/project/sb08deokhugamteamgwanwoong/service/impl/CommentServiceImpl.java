package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;
  private final CommentRepository commentRepository;

  @Override
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
}

package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewLikeDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.ReviewLike;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewLikeMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;

    private final ReviewMapper reviewMapper;
    private final ReviewLikeMapper reviewLikeMapper;

    @Override
    @Transactional
    public ReviewDto createReview(ReviewCreateRequest request) {
        UUID bookId = request.bookId();
        UUID userId = request.userId();
        log.info("Service: 리뷰 생성 로직 시작 - bookId: {}, userId: {}", bookId, userId);

        // 이미 작성한 리뷰가 있는 경우
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }
        User user = findUser(userId);
        Book book = findBook(bookId);

        Review review = Review.builder()
                .rating(request.rating())
                .content(request.content())
                .user(user)
                .book(book)
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("Service: 리뷰 생성 성공 - ID: {}", savedReview.getId());

        return reviewMapper.toDto(savedReview, false, book.getThumbnailUrl());
    }

    @Override
    @Transactional
    public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId) {
        log.info("Service: 리뷰 수정 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReview(reviewId);
        findUser(requestUserId);
        validateUpdatePermission(review, requestUserId);

        review.update(request.rating(), request.content());
        log.info("Service: 리뷰 수정 완료 - ID: {}", reviewId);

        return reviewMapper.toDto(review, false, review.getBook().getThumbnailUrl());
    }

    @Override
    @Transactional
    public void softDeleteReview(UUID reviewId, UUID requestUserId) {
        log.info("Service: 리뷰 논리 삭제 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReview(reviewId);
        findUser(requestUserId);
        validateDeletePermission(review, requestUserId);

        // 리뷰와 연관된 댓글 한번에 논리 삭제(벌크 연산)
        commentRepository.softDeleteAllByReviewId(reviewId, Instant.now());

        // 논리 삭제를 위해 deletedAt 갱신
        review.delete();

        // 명시적으로 save() 호출해서 변경된 상태를 DB에 강제로 반영
        reviewRepository.save(review);
        log.info("Service: 리뷰 논리 삭제 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
    }

    @Override
    @Transactional
    public void hardDeleteReview(UUID reviewId, UUID requestUserId) {
        log.info("Service: 리뷰 물리 삭제 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = reviewRepository.findByIdIncludeDeleted(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
        findUser(requestUserId);
        validateDeletePermission(review, requestUserId);

        // 리뷰와 연관된 댓글 한번에 물리 삭제(벌크 연산)
        commentRepository.hardDeleteAllByReviewId(reviewId);

        // 리뷰와 연관된 좋아요 한번에 물리 삭제(벌크 연산)
        reviewLikeRepository.hardDeleteAllByReviewId(reviewId);

        // @SQLRestriction을 통해서 deleted_at이 null이 아닌 경우 삭제 시 해당 Review를 찾을 수 없음
        reviewRepository.hardDeleteById(reviewId);
        log.info("Service: 리뷰 물리 삭제 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
    }

    @Override
    @Transactional
    public ReviewLikeDto createReviewLike(UUID reviewId, UUID requestUserId) {
        log.info("Service: 리뷰 좋아요 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReview(reviewId);
        User user = findUser(requestUserId);

        Optional<ReviewLike> existingReviewLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId);

        boolean isLikedNow;
        if (existingReviewLike.isPresent()) {
            reviewLikeRepository.delete(existingReviewLike.get());
            review.decreaseLikeCount();
            isLikedNow = false;
        } else {
            ReviewLike newReviewLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(newReviewLike);
            review.increaseLikeCount();
            isLikedNow = true;
        }
        log.info("Service: 리뷰 좋아요 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        return reviewLikeMapper.toDto(review.getId(), requestUserId, isLikedNow);
    }

    private Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Book findBook(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(IllegalArgumentException::new);
    }

    private void validateUpdatePermission(Review review, UUID userId) {
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_EDIT_PERMISSION_DENIED);
        }
    }

    private void validateDeletePermission(Review review, UUID userId) {
        if (!review.getUser().getId().equals(userId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_DELETE_PERMISSION_DENIED);
        }
    }
}

package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.ReviewLike;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewLikeMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private final NotificationService notificationService;

    @Override
    public CursorPageResponseReviewDto findAllReview(ReviewPageRequest request, UUID requestUserId) {
        log.info("Service: 리뷰 목록 커서 페이징 조회 시작");

        // 기본 정렬 방향 및 기준 세팅
        /* 정렬 기준
            orderBy: createAt(기본) || rating
            direction: DESC(기본) || ASC
         */
        String orderBy = request.orderBy() == null || request.orderBy().isEmpty() ? "createdAt" : request.orderBy();
        String directionStr = request.direction() == null || request.direction().isBlank() ? "DESC" : request.direction();
        // 대소문자 상관 없이 Sort.Direction.DESC, Sort.Direction.ASC 만들어 줌
        Sort.Direction direction = Sort.Direction.fromString(directionStr);

        // limit + 1 개수 만큼 조회하기 위한 Pageable
        int limit = request.limit();
        Pageable pageable = PageRequest.of(0, limit + 1);

        // String to Instant
        Instant afterInstant = null;
        if (request.after() != null && !request.after().isBlank()) {
            afterInstant = Instant.parse(request.after());
        }

        // 정렬 조건(Dto)
        ReviewSearchCondition condition = ReviewSearchCondition.builder()
                .userId(request.userId())
                .bookId(request.bookId())
                .keyword(request.keyword())
                .cursor(request.cursor())
                .after(afterInstant)
                .orderBy(orderBy)
                .direction(direction)
                .build();

        List<Review> reviewList = reviewRepository.findAllByCursor(
                condition,
                pageable
        );

        // Slice
        boolean hasNext = false;
        if (reviewList.size() > limit) {
            hasNext = true;
            reviewList.remove(reviewList.size() - 1);
        }

        List<UUID> reviewIds = reviewList.stream()
                .map(Review::getId)
                .toList();

        // 요청자가 좋아요를 누른 것을 확인할 수 있는 로직
        Set<UUID> likedReviewIds;
        if (reviewIds.isEmpty()) {
            likedReviewIds = Set.of();
        } else {
            likedReviewIds = reviewLikeRepository.findLikedReviewIds(requestUserId, reviewIds);
        }

        // 전체 리뷰 목록을 가져와서 맵핑
        List<ReviewDto> reviewDtoList = reviewList.stream()
                .map(review -> {
                    boolean isLiked = likedReviewIds.contains(review.getId());
                    return reviewMapper.toDto(review, isLiked, review.getBook().getThumbnailUrl());
                })
                .toList();

        String nextCursor = null;
        String nextAfter = null;

        if (!reviewDtoList.isEmpty()) {
            // 기존 limit + 1로 가져와서 -1 해줘야 함
            ReviewDto lastReviewDto = reviewDtoList.get(reviewDtoList.size() - 1);
            nextAfter = lastReviewDto.createdAt().toString();

            // 커서 기준: rating || createdAt
            if ("rating".equals(orderBy)) {
                nextCursor = String.valueOf(lastReviewDto.rating());
            } else {
                nextCursor = lastReviewDto.createdAt().toString();
            }
        }

        log.info("Service: 리뷰 목록 커서 페이징 조회 완료 - 반환 개수: {}, hasNext: {}", reviewDtoList.size(), hasNext);

        return new CursorPageResponseReviewDto(
                reviewDtoList,
                nextCursor,
                nextAfter,
                limit,
                null,
                hasNext
        );
    }

    @Override
    @Transactional
    public ReviewDto createReview(ReviewCreateRequest request) {
        UUID bookId = request.bookId();
        UUID userId = request.userId();
        log.info("Service: 리뷰 생성 로직 시작 - bookId: {}, userId: {}", bookId, userId);

        User user = findUser(userId);
        Book book = findBook(bookId);

        Optional<Review> optionalReview = reviewRepository.findByBookIdAndUserIdIncludeDeleted(bookId, userId);

        Review savedReview;

        if (optionalReview.isPresent()) {
            Review exisstingReview = optionalReview.get();

            if (exisstingReview.getDeletedAt() == null) {
                throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
            } else {
                exisstingReview.restore(request.rating(), request.content());
                savedReview = reviewRepository.save(exisstingReview);
            }
        } else {
            Review newReview = Review.builder()
                    .rating(request.rating())
                    .content(request.content())
                    .user(user)
                    .book(book)
                    .build();
            savedReview = reviewRepository.save(newReview);
        }

        // 도서의 평점/ 리뷰 수 증가 반영
        book.addReviewRating(request.rating());

        log.info("Service: 리뷰 생성 성공 - ID: {}", savedReview.getId());

        return reviewMapper.toDto(savedReview, false, book.getThumbnailUrl());
    }

    @Override
    @Transactional
    public ReviewLikeDto createReviewLike(UUID reviewId, UUID requestUserId) {
        log.info("Service: 리뷰 좋아요 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReviewWithLock(reviewId);
        User user = findUser(requestUserId);

        Optional<ReviewLike> existingReviewLike = reviewLikeRepository.findByReviewIdAndUserId(reviewId, requestUserId);

        boolean isLikedNow;
        if (existingReviewLike.isPresent()) {
            reviewLikeRepository.delete(existingReviewLike.get());
            reviewLikeRepository.flush();

            reviewRepository.decreaseLikeCount(reviewId);
            isLikedNow = false;
        } else {
            ReviewLike newReviewLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.saveAndFlush(newReviewLike);
            reviewRepository.increaseLikeCount(reviewId);
            isLikedNow = true;

            User toUser = review.getUser(); // 리뷰 작성자

            // 다른 사람이 좋아요를 누를 경우만 알림 발송
            if (!toUser.getId().equals(user.getId())) {
                String message = String.format("[%s]님이 나의 리뷰를 좋아합니다.", user.getNickname());
                notificationService.createNotification(toUser, review, message);
            }
        }
        log.info("Service: 리뷰 좋아요 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        return reviewLikeMapper.toDto(review.getId(), requestUserId, isLikedNow);
    }

    @Override
    public ReviewDto findDetailReview(UUID reviewId, UUID requestUserId) {
        log.info("Service: 리뷰 상세 조회 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReview(reviewId);
        findUser(requestUserId);

        log.info("Service: 리뷰 상세 조회 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        return reviewMapper.toDto(review, findIsLiked(reviewId, requestUserId), review.getBook().getThumbnailUrl());
    }


    @Override
    @Transactional
    public void softDeleteReview(UUID reviewId, UUID requestUserId) {
        log.info("Service: 리뷰 논리 삭제 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReviewWithLock(reviewId);
        findUser(requestUserId);
        validateDeletePermission(review, requestUserId);

        review.getBook().removeReviewRating(review.getRating());

        // 논리 삭제를 위해 deletedAt 갱신
        review.delete();

        // 명시적으로 saveAndFlush() 호출해서 캐시를 비우고 변경된 상태를 DB에 강제로 반영
        reviewRepository.saveAndFlush(review);

        // 리뷰와 연관된 댓글 한번에 논리 삭제(벌크 연산)
        commentRepository.softDeleteAllByReviewId(reviewId, Instant.now());

        log.info("Service: 리뷰 논리 삭제 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
    }

    @Override
    @Transactional
    public ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId) {
        log.info("Service: 리뷰 수정 로직 시작 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
        Review review = findReviewWithLock(reviewId);
        findUser(requestUserId);
        validateUpdatePermission(review, requestUserId);

        Integer oldRating = review.getRating();

        review.update(request.rating(), request.content());

        if (!oldRating.equals(request.rating())) {
            review.getBook().updateReviewRating(oldRating, request.rating());
        }

        log.info("Service: 리뷰 수정 완료 - ID: {}", reviewId);

        return reviewMapper.toDto(review, findIsLiked(reviewId, requestUserId), review.getBook().getThumbnailUrl());
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

        //가짜 프록시가 아닌 확실한 Book을 조회해서 가져옴
        Book book = bookRepository.findById(review.getBook().getId())
                .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

        book.removeReviewRating(review.getRating());

        // @SQLRestriction을 통해서 deleted_at이 null이 아닌 경우 삭제 시 해당 Review를 찾을 수 없음
        reviewRepository.hardDeleteById(reviewId);

        log.info("Service: 리뷰 물리 삭제 로직 성공 - reviewId: {}, requestUserId: {}", reviewId, requestUserId);
    }

    // 단순 조회용 - 상세 조회
    private Review findReview(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    // 좋아요/수정/삭제용 - 동시성 제어가 필요한 경우
    private Review findReviewWithLock(UUID reviewId) {
        return reviewRepository.findByIdWithPessimisticLock(reviewId)
                .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    private Book findBook(UUID bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));
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

    private boolean findIsLiked(UUID reviewId, UUID userId) {
        return reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId).isPresent();
    }
}

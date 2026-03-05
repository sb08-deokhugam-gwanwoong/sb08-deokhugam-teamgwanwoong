package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.ReviewMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDto create(ReviewCreateRequest request) {
        UUID bookId = request.bookId();
        UUID userId = request.userId();

        log.info("Service: 리뷰 생성 로직 시작 - bookId: {}, userId: {}", bookId, userId);

        // 이미 작성한 리뷰가 있는 경우
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw new BusinessException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        // 추후 커스텀 에러 만드시면 수정하겠습니다.
        User user = userRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(IllegalArgumentException::new);

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
    public ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId) {

    }
}

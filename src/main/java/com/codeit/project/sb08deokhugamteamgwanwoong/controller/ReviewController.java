package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.ReviewApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

    @Override
    public ResponseEntity<CursorPageResponseReviewDto> findAll(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @ModelAttribute ReviewPageRequest request
    ) {
        log.info("Controller: 리뷰 목록 조회 요청");
        CursorPageResponseReviewDto reviewDto = reviewService.findAllReview(request, requestUserId);
        log.info("Controller: 리뷰 목록 조회 완료");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    @Override
    public ResponseEntity<ReviewDto> create(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        log.info("Controller: 리뷰 생성 요청");
        ReviewDto reviewDto = reviewService.createReview(request);
        log.info("Controller: 리뷰 생성 완료");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewDto);
    }

    @Override
    public ResponseEntity<ReviewLikeDto> createReviewLike(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        log.info("Controller: 리뷰 좋아요 요청 - ID: {}, UserId: {}", reviewId, requestUserId);
        ReviewLikeDto reviewLikeDto = reviewService.createReviewLike(reviewId, requestUserId);
        log.info("Controller: 리뷰 좋아요 성공 - ID: {}, UserId: {}", reviewId, requestUserId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reviewLikeDto);
    }

    @Override
    public ResponseEntity<ReviewDto> findDetail(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        log.info("Controller: 리뷰 상세 정보 요청 - ID: {}, UserId: {}", reviewId, requestUserId);
        ReviewDto reviewDto = reviewService.findDetailReview(reviewId, requestUserId);
        log.info("Controller: 리뷰 상세 정보 성공 - ID: {}, UserId: {}", reviewId, requestUserId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    @Override
    public ResponseEntity<Void> softDelete(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        log.info("Controller: 리뷰 논리 삭제 요청 - ID: {}, UserId: {}", reviewId, requestUserId);
        reviewService.softDeleteReview(reviewId, requestUserId);
        log.info("Controller: 리뷰 논리 삭제 완료 - ID: {}, UserId: {}", reviewId, requestUserId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @Override
    public ResponseEntity<ReviewDto> update(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        log.info("Controller: 리뷰 수정 요청 - ID: {}, UserId: {}", reviewId, requestUserId);
        ReviewDto reviewDto = reviewService.updateReview(reviewId, request, requestUserId);
        log.info("Controller: 리뷰 수정 완료 - ID: {}, UserId: {}", reviewId, requestUserId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    @Override
    public ResponseEntity<Void> hardDelete(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        log.info("Controller: 리뷰 물리 삭제 요청 - ID: {}, UserId: {}", reviewId, requestUserId);
        reviewService.hardDeleteReview(reviewId, requestUserId);
        log.info("Controller: 리뷰 물리 삭제 성공 - ID: {}, UserId: {}", reviewId, requestUserId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}

package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;
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
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
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

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> update(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        log.info("Controller: 리뷰 수정 요청 - ID: {}", reviewId);
        ReviewDto reviewDto = reviewService.updateReview(reviewId, request, requestUserId);
        log.info("Controller: 리뷰 수정 완료 - ID: {}", reviewId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(reviewDto);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> softDelete(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        log.info("Controller: 리뷰 논리 삭제 요청 - ID: {}", reviewId);
        reviewService.softDeleteReview(reviewId, requestUserId);
        log.info("Controller: 리뷰 논리 삭제 완료 - ID: {}", reviewId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDeleteHard(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        log.info("Controller: 리뷰 물리 삭제 요청 - ID: {}", reviewId);
        reviewService.hardDeleteReview(reviewId, requestUserId);
        log.info("Controller: 리뷰 물리 삭제 성공 - ID: {}", reviewId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}

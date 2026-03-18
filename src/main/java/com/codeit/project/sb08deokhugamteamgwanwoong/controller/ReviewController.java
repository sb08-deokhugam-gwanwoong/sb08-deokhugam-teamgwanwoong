package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.ReviewApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.DashboardService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reviews")
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;
    private final DashboardService dashboardService;

    // Value타입을 Object로 받아 JSON으로 변환되게 함
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

    @PostMapping("/{reviewId}/like")
    public ResponseEntity<Void> createReviewLike(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    ) {
        // 현재 좋아요 눌렀는지 DB 상태 조회
        boolean currentLiked = reviewService.checkIsLiked(reviewId, requestUserId);

        // 카프카에 반영해야 할 목표 상태(낙관)
        boolean targetState = !currentLiked;

        // 기존 DTO에 목표 상태를 담아서 전송
        ReviewLikeDto eventDto = new ReviewLikeDto(reviewId, requestUserId, targetState);
        kafkaTemplate.send("review-like", reviewId.toString(), eventDto);

        // 비동기 처리 완료 응답
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
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

    @GetMapping("/popular")
    public ResponseEntity<CursorPageResponsePopularReviewDto> getPopularReviews(
            @ModelAttribute DashboardPageRequest request
    ) {
        log.info("인기 리뷰 목록 조회 요청 - 기간: {}, cursor: {}, limit: {}",
                request.period(), request.cursor(), request.limit());

        CursorPageResponsePopularReviewDto responseDto = dashboardService.getPopularReviews(request);
        log.info("인기 리뷰 목록 조회 완료 - 반환된 데이터 개수: {}, 다음 페이지 존재 여부: {}",
                responseDto.content().size(), responseDto.hasNext());

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}

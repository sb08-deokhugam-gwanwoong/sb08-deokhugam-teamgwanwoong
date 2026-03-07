package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewApi {

    @Operation(
            summary = "리뷰 목록 조회",
            description = "검색 조건에 맞는 리뷰 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리뷰 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = CursorPageResponseReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)",
                            content = @Content(schema = @Schema(implementation = CursorPageResponseReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = CursorPageResponseReviewDto.class))
                    )
            }
    )
    @GetMapping
    ResponseEntity<CursorPageResponseReviewDto> findAll(
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @ModelAttribute ReviewPageRequest request
    );

    @Operation(
            summary = "리뷰 등록",
            description = "새로운 리뷰를 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "리뷰 등록 성공",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(입력값 검증 실패)",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "도서 정보 없음",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 작성된 리뷰 존재",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    )
            }
    )
    @PostMapping
    ResponseEntity<ReviewDto> create(
            @Valid @RequestBody ReviewCreateRequest request
    );

    @Operation(
            summary = "리뷰 좋아요",
            description = "리뷰에 좋아요를 추가하거나 취소합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "리뷰 좋아요 성공",
                            content = @Content(schema = @Schema(implementation = ReviewLikeDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(요청자 ID 누락)",
                            content = @Content(schema = @Schema(implementation = ReviewLikeDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "리뷰 정보 없음",
                            content = @Content(schema = @Schema(implementation = ReviewLikeDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    )
            }
    )
    @PostMapping("/{reviewId}/like")
    ResponseEntity<ReviewLikeDto> createReviewLike(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    @Operation(
            summary = "리뷰 상세 정보 조회",
            description = "리뷰 ID로 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리뷰 정보 조회 성공",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(요청자 ID 누락)",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "리뷰 정보 없음",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    )
            }
    )
    @GetMapping("/{reviewId}")
    ResponseEntity<ReviewDto> findDetail(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    @Operation(
            summary = "리뷰 논리 삭제",
            description = "본인이 작성한 리뷰를 논리적으로 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "리뷰 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(요청자 ID 누락)"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "리뷰 삭제 권한 없음"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "리뷰 정보 없음"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류"
                    )
            }
    )
    @DeleteMapping("/{reviewId}")
    ResponseEntity<Void> softDelete(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );

    @Operation(
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰를 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "리뷰 수정 성공",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(입력값 검증 실패)",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "리뷰 수정 권한 없음",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "리뷰 정보 없음",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ReviewDto.class))
                    )
            }
    )
    @PatchMapping("/{reviewId}")
    ResponseEntity<ReviewDto> update(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId,
            @Valid @RequestBody ReviewUpdateRequest request
    );

    @Operation(
            summary = "리뷰 물리 삭제",
            description = "본인이 작성한 리뷰를 물리적으로 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "리뷰 삭제 성공"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청(요청자 ID 누락)"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "리뷰 삭제 권한 없음"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "리뷰 정보 없음"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류"
                    )
            }
    )
    @DeleteMapping("/{reviewId}/hard")
    ResponseEntity<Void> hardDelete(
            @PathVariable("reviewId") UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requestUserId
    );
}

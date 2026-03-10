package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.*;

import java.util.UUID;

public interface ReviewService {

    CursorPageResponseReviewDto findAllReview(ReviewPageRequest request, UUID requestUserId);

    ReviewDto createReview(ReviewCreateRequest request);

    ReviewLikeDto createReviewLike(UUID reviewId, UUID requestUserId);

    ReviewDto findDetailReview(UUID reviewId, UUID requestUserId);

    void softDeleteReview(UUID reviewId, UUID requestUserId);

    ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId);

    void hardDeleteReview(UUID reviewId, UUID requestUserId);

}
package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewLikeDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;

import java.util.UUID;

public interface ReviewService {
    ReviewDto createReview(ReviewCreateRequest request);
    ReviewDto updateReview(UUID reviewId, ReviewUpdateRequest request, UUID requestUserId);
    void softDeleteReview(UUID reviewId,  UUID requestUserId);
    void hardDeleteReview(UUID reviewId,  UUID requestUserId);
//    ReviewLikeDto  createReviewLike(UUID reviewId, UUID requestUserId);
}
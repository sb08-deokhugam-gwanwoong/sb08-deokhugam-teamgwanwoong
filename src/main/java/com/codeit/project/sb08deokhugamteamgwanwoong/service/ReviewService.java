package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewUpdateRequest;

import java.util.UUID;

public interface ReviewService {
    ReviewDto create(ReviewCreateRequest request);
}
package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewRepositoryCustom {

    List<Review> findAllByCursor(ReviewSearchCondition condition, Pageable pageable);
}

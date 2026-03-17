package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDocument;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewSearchCondition;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;

public interface ReviewSearchRepositoryCustom {
    SearchHits<ReviewDocument> searchByCursor(ReviewSearchCondition condition, Pageable pageable);
}

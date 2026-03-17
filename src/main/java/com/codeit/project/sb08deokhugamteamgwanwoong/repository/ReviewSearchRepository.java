package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewSearchRepository extends ElasticsearchRepository<ReviewDocument, String>, ReviewSearchRepositoryCustom {
}

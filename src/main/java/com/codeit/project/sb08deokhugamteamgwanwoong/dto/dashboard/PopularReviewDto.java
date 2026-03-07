package com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.time.Instant;
import java.util.UUID;

public record PopularReviewDto(
		UUID id,
		UUID reviewId,
		UUID bookId,
		String bookTitle,
		String bookThumbnailUrl,
		UUID userId,
		String userNickname,
		String reviewContent,
		Double reviewRating,
		DashboardPeriodEnums period,
		Instant createdAt,
		Long rank,
		Double score,
		Long likeCount,
		Long commentCount
){
}

package com.codeit.project.sb08deokhugamteamgwanwoong.mapper;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

	@Mapping(source = "dashboard.id", target = "id")
	@Mapping(source = "book.id", target = "bookId")
	@Mapping(source = "book.title", target = "title")
	@Mapping(source = "book.author", target = "author")
	@Mapping(source = "book.thumbnailUrl", target = "thumbnailUrl")
	@Mapping(source = "dashboard.periodType", target = "period")
	@Mapping(source = "dashboard.rankingPos", target = "rank")
	@Mapping(source = "dashboard.score", target = "score")
	@Mapping(source = "book.reviewCount", target = "reviewCount")
	@Mapping(source = "book.rating", target = "rating")
	@Mapping(source = "dashboard.createdAt", target = "createdAt")
	PopularBookDto toPopularBookDto(Dashboard dashboard, Book book);

	@Mapping(source = "dashboard.id", target = "id")
	@Mapping(source = "review.id", target = "reviewId")
	@Mapping(source = "review.book.id", target = "bookId")
	@Mapping(source = "review.book.title", target = "bookTitle")
	@Mapping(source = "review.book.thumbnailUrl", target = "bookThumbnailUrl")
	@Mapping(source = "review.user.id", target = "userId")
	@Mapping(source = "review.user.nickname", target = "userNickname")
	@Mapping(source = "review.content", target = "reviewContent")
	@Mapping(source = "review.rating", target = "reviewRating")
	@Mapping(source = "dashboard.periodType", target = "period")
	@Mapping(source = "dashboard.createdAt", target = "createdAt")
	@Mapping(source = "dashboard.rankingPos", target = "rank")
	@Mapping(source = "dashboard.score", target = "score")
	@Mapping(source = "review.likeCount", target = "likeCount")
	@Mapping(source = "review.commentCount", target = "commentCount")
	PopularReviewDto toPopularReviewDto(Dashboard dashboard, Review review);
}

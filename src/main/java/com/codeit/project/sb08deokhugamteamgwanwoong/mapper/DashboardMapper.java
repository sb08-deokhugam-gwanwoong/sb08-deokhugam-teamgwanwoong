package com.codeit.project.sb08deokhugamteamgwanwoong.mapper;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
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
}

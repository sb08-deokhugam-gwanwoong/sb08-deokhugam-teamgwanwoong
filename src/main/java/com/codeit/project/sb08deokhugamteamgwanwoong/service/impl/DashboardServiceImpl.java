package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.DashboardMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.DashboardService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

	private final DashboardRepository dashboardRepository;
	private final BookRepository bookRepository;
	private final ReviewRepository reviewRepository;
	private final DashboardMapper dashboardMapper;

	@Override
	public List<PopularBookDto> getPopularBooks() {
		return getPopularBooks(DashboardPeriodEnums.ALL_TIME);
	}

	public List<PopularBookDto> getPopularBooks(DashboardPeriodEnums period) {
		List<Dashboard> rankings = dashboardRepository.findRecentRankings(
				"BOOK", period, PageRequest.of(0, 10));

		if (rankings.isEmpty()) {
			return List.of();
		}

		List<UUID> bookIds = rankings.stream()
				.map(Dashboard::getTargetId)
				.toList();

		//N+1 문제 해결: 10번 쿼리를 날리는 대신 findAllById를 써서 IN 절 쿼리 한 번으로 도서 10권의 정보를 다 가져옵니다.
		Map<UUID, Book> bookMap = bookRepository.findAllById(bookIds).stream()
				.collect(Collectors.toMap(Book::getId, book -> book));

		return rankings.stream()
				.filter(r -> bookMap.containsKey(r.getTargetId()))
				.map(r -> dashboardMapper.toPopularBookDto(r, bookMap.get(r.getTargetId())))
				.toList();
	}

	@Override
	public List<PopularReviewDto> getPopularReviews(DashboardPeriodEnums period) {
		List<Dashboard> rankings = dashboardRepository.findRecentRankings(
				"REVIEW", period, PageRequest.of(0, 10));

		if (rankings.isEmpty()) {
			return List.of();
		}

		List<UUID> reviewIds = rankings.stream()
				.map(Dashboard::getTargetId)
				.toList();

		Map<UUID, Review> reviewMap = reviewRepository.findAllById(reviewIds).stream()
				.collect(Collectors.toMap(Review::getId, review -> review));

		return rankings.stream()
				.filter(r -> reviewMap.containsKey(r.getTargetId()))
				.map(r -> dashboardMapper.toPopularReviewDto(r, reviewMap.get(r.getTargetId())))
				.toList();
	}
}


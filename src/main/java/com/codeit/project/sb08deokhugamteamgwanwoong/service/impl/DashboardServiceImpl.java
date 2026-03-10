package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularReviewDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PowerUserDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.DashboardMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.DashboardService;
import java.time.Instant;
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
	private final UserRepository userRepository;
	private final DashboardMapper dashboardMapper;

	@Override
	public List<PopularBookDto> getPopularBooks() {
		return getPopularBooks(DashboardPeriodEnums.ALL_TIME);
	}

	public List<PopularBookDto> getPopularBooks(DashboardPeriodEnums period) {
		List<Dashboard> rankings = dashboardRepository.findRecentRankings(
				DashboardTargetType.BOOK, period, PageRequest.of(0, 10));

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
				DashboardTargetType.REVIEW, period, PageRequest.of(0, 10));

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

	@Override
	public List<PowerUserDto> getPowerUsers(DashboardPeriodEnums period) {
		List<Dashboard> rankings = dashboardRepository.findRecentRankings(
				DashboardTargetType.USER, period, PageRequest.of(0, 10));

		if (rankings.isEmpty()) {
			return List.of();
		}

		List<UUID> userIds = rankings.stream()
				.map(Dashboard::getTargetId)
				.toList();

		Map<UUID, User> userMap = userRepository.findAllById(userIds).stream()
				.collect(Collectors.toMap(User::getId, user -> user));

		return rankings.stream()
				.filter(r -> userMap.containsKey(r.getTargetId()))
				.map(r -> dashboardMapper.toPowerUserDto(r, userMap.get(r.getTargetId())))
				.toList();
	}

	/** 인기 도서 목록 조회 (커서 페이지네이션) */
	@Override
	public CursorPageResponsePopularBookDto getPopularBooks(DashboardPageRequest request) {
		DashboardPageSlice slice = findRankingsByCursor(DashboardTargetType.BOOK, request);
		List<Dashboard> rankings = slice.rankings();
		int limit = slice.limit();
		boolean hasNext = slice.hasNext();

		// N+1 방지: IN 절로 관련 엔티티 일괄 조회
		List<UUID> ids = rankings.stream().map(Dashboard::getTargetId).toList();
		Map<UUID, Book> bookMap = bookRepository.findAllById(ids).stream().collect(Collectors.toMap(Book::getId, b -> b));
		List<PopularBookDto> content = rankings.stream()
				.filter(r -> bookMap.containsKey(r.getTargetId()))
				.map(r -> dashboardMapper.toPopularBookDto(r, bookMap.get(r.getTargetId())))
				.toList();

		String nextCursor = null;
		Instant nextAfter = null;
		if (!content.isEmpty()) {
			Dashboard last = rankings.get(rankings.size() - 1);
			nextCursor = String.valueOf(last.getRankingPos());
			nextAfter = last.getCreatedAt();
		}

		return new CursorPageResponsePopularBookDto(content, nextCursor, nextAfter, limit, null, hasNext);
	}

	@Override
	public CursorPageResponsePopularReviewDto getPopularReviews(DashboardPageRequest request) {
		DashboardPageSlice slice = findRankingsByCursor(DashboardTargetType.REVIEW, request);
		List<Dashboard> rankings = slice.rankings();
		int limit = slice.limit();
		boolean hasNext = slice.hasNext();

		// N+1 방지: IN 절로 관련 엔티티 일괄 조회
		List<UUID> ids = rankings.stream().map(Dashboard::getTargetId).toList();
		Map<UUID, Review> reviewMap = reviewRepository.findAllById(ids).stream()
				.collect(Collectors.toMap(Review::getId, r -> r));
		List<PopularReviewDto> content = rankings.stream()
				.filter(r -> reviewMap.containsKey(r.getTargetId()))
				.map(r -> dashboardMapper.toPopularReviewDto(r, reviewMap.get(r.getTargetId())))
				.toList();

		// 다음 페이지 요청용 커서
		String nextCursor = null;
		Instant nextAfter = null;
		if (!content.isEmpty()) {
			Dashboard last = rankings.get(rankings.size() - 1);
			nextCursor = String.valueOf(last.getRankingPos());
			nextAfter = last.getCreatedAt();
		}

		return new CursorPageResponsePopularReviewDto(content, nextCursor, nextAfter, limit, null, hasNext);
	}

	/** 파워 유저 목록 조회 (커서 페이지네이션) */
	@Override
	public CursorPageResponsePowerUserDto getPowerUsers(DashboardPageRequest request) {
		DashboardPageSlice slice = findRankingsByCursor(DashboardTargetType.USER, request);
		List<Dashboard> rankings = slice.rankings();
		int limit = slice.limit();
		boolean hasNext = slice.hasNext();

		// N+1 방지: IN 절로 관련 엔티티 일괄 조회
		List<UUID> ids = rankings.stream().map(Dashboard::getTargetId).toList();
		Map<UUID, User> userMap = userRepository.findAllById(ids).stream().collect(Collectors.toMap(User::getId, u -> u));
		List<PowerUserDto> content = rankings.stream()
				.filter(r -> userMap.containsKey(r.getTargetId()))
				.map(r -> dashboardMapper.toPowerUserDto(r, userMap.get(r.getTargetId())))
				.toList();

		String nextCursor = null;
		Instant nextAfter = null;
		if (!content.isEmpty()) {
			Dashboard last = rankings.get(rankings.size() - 1);
			nextCursor = String.valueOf(last.getRankingPos());
			nextAfter = last.getCreatedAt();
		}

		return new CursorPageResponsePowerUserDto(content, nextCursor, nextAfter, limit, null, hasNext);
	}

	private DashboardPageSlice findRankingsByCursor(DashboardTargetType targetType, DashboardPageRequest request) {
		Instant after = parseAfter(request.after());
		Integer cursorRankingPos = parseCursorRankingPos(request.cursor());
		int limit = request.limit();
		// direction에 따라 정렬 방식 분기, limit+1개 조회로 다음 페이지 존재 여부 확인
		List<Dashboard> rankings = "DESC".equalsIgnoreCase(request.direction())
				? dashboardRepository.findRecentRankingsByCursorDesc(targetType, request.period(), after, cursorRankingPos,
						PageRequest.of(0, limit + 1))
				: dashboardRepository.findRecentRankingsByCursorAsc(targetType, request.period(), after, cursorRankingPos,
						PageRequest.of(0, limit + 1));

		boolean hasNext = rankings.size() > limit;
		if (hasNext) {
			rankings = rankings.subList(0, limit);
		}

		return new DashboardPageSlice(rankings, hasNext, limit);
	}

	private record DashboardPageSlice(List<Dashboard> rankings, boolean hasNext, int limit) {
	}

	private Instant parseAfter(String after) {
		if (after == null || after.isBlank()) {
			return null;
		}
		try {
			return Instant.parse(after);
		} catch (Exception e) {
			return null;
		}
	}

	/** cursor 쿼리 파라미터를 rankingPos(Integer)로 파싱, 첫 페이지는 -1 */
	private Integer parseCursorRankingPos(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return -1;
		}
		try {
			return Integer.parseInt(cursor);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}


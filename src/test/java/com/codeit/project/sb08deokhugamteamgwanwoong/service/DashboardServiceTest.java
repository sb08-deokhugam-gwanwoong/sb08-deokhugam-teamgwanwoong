package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.DashboardServiceImpl;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("대시보드 서비스")
class DashboardServiceTest {

	@Mock
	private DashboardRepository dashboardRepository;

	@Mock
	private BookRepository bookRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private DashboardMapper dashboardMapper;

	@InjectMocks
	private DashboardServiceImpl dashboardService;

	@Test
	@DisplayName("대시보드 데이터가 없으면 빈 리스트를 반환한다.")
	void getPopularBooks_returnsEmpty_whenNoDashboardData() {
		// given
		when(dashboardRepository.findRecentRankings(
				eq(DashboardTargetType.BOOK),
				eq(DashboardPeriodEnums.ALL_TIME),
				any(PageRequest.class)
		)).thenReturn(List.of());

		// when
		List<PopularBookDto> response = dashboardService.getPopularBooks();

		// then
		assertThat(response).isEmpty();
	}

	@Test
	@DisplayName("대시보드 데이터가 있으면 점수 순(랭킹 순)으로 인기 도서 목록을 조회한다.")
	void getPopularBooks_returnsOrderedByRanking_whenDashboardDataExists() {
		// given
		UUID book1Id = UUID.randomUUID();
		UUID book2Id = UUID.randomUUID();

		Dashboard dashboard1 = createDashboard(book1Id, "BOOK", DashboardPeriodEnums.ALL_TIME, 90.0, 1);
		Dashboard dashboard2 = createDashboard(book2Id, "BOOK", DashboardPeriodEnums.ALL_TIME, 80.0, 2);

		Book book1 = createMockBook(book1Id);
		Book book2 = createMockBook(book2Id);

		PopularBookDto dto1 = createPopularBookDto(
				book1Id, "자바의 정석", "남궁성", "https://example.com/thumb1.jpg",
				DashboardPeriodEnums.ALL_TIME, 1L, 90.0, 10L, 4.5);
		PopularBookDto dto2 = createPopularBookDto(
				book2Id, "클린 코드", "로버트 마틴", null,
				DashboardPeriodEnums.ALL_TIME, 2L, 80.0, 5L, 4.0);

		when(dashboardRepository.findRecentRankings(
				eq(DashboardTargetType.BOOK),
				eq(DashboardPeriodEnums.ALL_TIME),
				any(PageRequest.class)
		)).thenReturn(List.of(dashboard1, dashboard2));

		when(bookRepository.findAllById(anyList())).thenReturn(List.of(book1, book2));

		when(dashboardMapper.toPopularBookDto(dashboard1, book1)).thenReturn(dto1);
		when(dashboardMapper.toPopularBookDto(dashboard2, book2)).thenReturn(dto2);

		// when
		List<PopularBookDto> response = dashboardService.getPopularBooks();

		// then
		assertThat(response).hasSize(2);
		assertThat(response.get(0).rank()).isEqualTo(1L);
		assertThat(response.get(0).title()).isEqualTo("자바의 정석");
		assertThat(response.get(0).author()).isEqualTo("남궁성");
		assertThat(response.get(0).bookId()).isEqualTo(book1Id);
		assertThat(response.get(0).score()).isEqualTo(90.0);
		assertThat(response.get(0).thumbnailUrl()).isEqualTo("https://example.com/thumb1.jpg");

		assertThat(response.get(1).rank()).isEqualTo(2L);
		assertThat(response.get(1).title()).isEqualTo("클린 코드");
		assertThat(response.get(1).author()).isEqualTo("로버트 마틴");
		assertThat(response.get(1).bookId()).isEqualTo(book2Id);
		assertThat(response.get(1).score()).isEqualTo(80.0);
		assertThat(response.get(1).thumbnailUrl()).isNull();
	}

	// ---- 커서 페이지네이션 테스트 ----

	@Test
	@DisplayName("인기 도서 커서: 첫 페이지 조회 시 content, hasNext, nextCursor, nextAfter를 반환한다.")
	void getPopularBooks_cursor_returnsFirstPageWithCursorInfo() {
		// given - rankings 3개, limit 2 -> 2개 반환, hasNext true
		UUID book1Id = UUID.randomUUID();
		UUID book2Id = UUID.randomUUID();
		UUID book3Id = UUID.randomUUID();

		Instant createdAt = Instant.parse("2026-03-09T05:00:00Z");
		Dashboard dashboard1 = createDashboardWithCreatedAt(book1Id, "BOOK", DashboardPeriodEnums.ALL_TIME, 90.0, 1, createdAt);
		Dashboard dashboard2 = createDashboardWithCreatedAt(book2Id, "BOOK", DashboardPeriodEnums.ALL_TIME, 80.0, 2, createdAt.plusSeconds(1));
		Dashboard dashboard3 = createDashboardWithCreatedAt(book3Id, "BOOK", DashboardPeriodEnums.ALL_TIME, 70.0, 3, createdAt.plusSeconds(2));

		Book book1 = createMockBook(book1Id);
		Book book2 = createMockBook(book2Id);

		PopularBookDto dto1 = createPopularBookDto(book1Id, "자바의 정석", "남궁성", null, DashboardPeriodEnums.ALL_TIME, 1L, 90.0, 10L, 4.5);
		PopularBookDto dto2 = createPopularBookDto(book2Id, "클린 코드", "로버트 마틴", null, DashboardPeriodEnums.ALL_TIME, 2L, 80.0, 5L, 4.0);

		DashboardPageRequest request = new DashboardPageRequest(
				DashboardPeriodEnums.ALL_TIME, "ASC", null, null, 2);

		when(dashboardRepository.findRecentRankingsByCursorAsc(
				eq(DashboardTargetType.BOOK),
				eq(DashboardPeriodEnums.ALL_TIME),
				any(),
				anyInt(),
				any(org.springframework.data.domain.Pageable.class)
		)).thenReturn(List.of(dashboard1, dashboard2, dashboard3));

		when(bookRepository.findAllById(anyList())).thenReturn(List.of(book1, book2));

		when(dashboardMapper.toPopularBookDto(dashboard1, book1)).thenReturn(dto1);
		when(dashboardMapper.toPopularBookDto(dashboard2, book2)).thenReturn(dto2);

		// when
		CursorPageResponsePopularBookDto response = dashboardService.getPopularBooks(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isTrue();
		assertThat(response.nextCursor()).isEqualTo("2");
		assertThat(response.nextAfter()).isEqualTo(createdAt.plusSeconds(1));
		assertThat(response.size()).isEqualTo(2);
	}

	@Test
	@DisplayName("인기 도서 커서: 마지막 페이지 조회 시 hasNext는 false이다.")
	void getPopularBooks_cursor_returnsLastPage_hasNextFalse() {
		// given - rankings 2개, limit 2 -> 2개 반환, hasNext false
		UUID book1Id = UUID.randomUUID();
		UUID book2Id = UUID.randomUUID();
		Instant createdAt = Instant.parse("2026-03-09T05:00:00Z");

		Dashboard dashboard1 = createDashboardWithCreatedAt(book1Id, "BOOK", DashboardPeriodEnums.DAILY, 90.0, 1, createdAt);
		Dashboard dashboard2 = createDashboardWithCreatedAt(book2Id, "BOOK", DashboardPeriodEnums.DAILY, 80.0, 2, createdAt.plusSeconds(1));

		Book book1 = createMockBook(book1Id);
		Book book2 = createMockBook(book2Id);

		PopularBookDto dto1 = createPopularBookDto(book1Id, "책1", "저자1", null, DashboardPeriodEnums.DAILY, 1L, 90.0, 1L, 4.5);
		PopularBookDto dto2 = createPopularBookDto(book2Id, "책2", "저자2", null, DashboardPeriodEnums.DAILY, 2L, 80.0, 1L, 4.0);

		DashboardPageRequest request = new DashboardPageRequest(
				DashboardPeriodEnums.DAILY, "DESC", null, null, 2);

		when(dashboardRepository.findRecentRankingsByCursorDesc(
				eq(DashboardTargetType.BOOK),
				eq(DashboardPeriodEnums.DAILY),
				any(),
				anyInt(),
				any(org.springframework.data.domain.Pageable.class)
		)).thenReturn(List.of(dashboard1, dashboard2));

		when(bookRepository.findAllById(anyList())).thenReturn(List.of(book1, book2));
		when(dashboardMapper.toPopularBookDto(dashboard1, book1)).thenReturn(dto1);
		when(dashboardMapper.toPopularBookDto(dashboard2, book2)).thenReturn(dto2);

		// when
		CursorPageResponsePopularBookDto response = dashboardService.getPopularBooks(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isEqualTo("2");
		assertThat(response.nextAfter()).isEqualTo(createdAt.plusSeconds(1));
	}

	@Test
	@DisplayName("인기 도서 커서: 대시보드 데이터가 없으면 빈 content와 hasNext false를 반환한다.")
	void getPopularBooks_cursor_returnsEmpty_whenNoData() {
		// given
		DashboardPageRequest request = new DashboardPageRequest(
				DashboardPeriodEnums.ALL_TIME, "ASC", null, null, 10);

		when(dashboardRepository.findRecentRankingsByCursorAsc(
				eq(DashboardTargetType.BOOK),
				eq(DashboardPeriodEnums.ALL_TIME),
				any(),
				anyInt(),
				any(org.springframework.data.domain.Pageable.class)
		)).thenReturn(List.of());

		// when
		CursorPageResponsePopularBookDto response = dashboardService.getPopularBooks(request);

		// then
		assertThat(response.content()).isEmpty();
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isNull();
		assertThat(response.nextAfter()).isNull();
		assertThat(response.size()).isEqualTo(10);
	}

	@Test
	@DisplayName("인기 리뷰 커서: 첫 페이지 조회 시 CursorPageResponse를 반환한다.")
	void getPopularReviews_cursor_returnsFirstPage() {
		// given
		UUID review1Id = UUID.randomUUID();
		UUID review2Id = UUID.randomUUID();
		Instant createdAt = Instant.parse("2026-03-09T05:00:00Z");

		Dashboard dashboard1 = createDashboardWithCreatedAt(review1Id, "REVIEW", DashboardPeriodEnums.WEEKLY, 85.0, 1, createdAt);
		Dashboard dashboard2 = createDashboardWithCreatedAt(review2Id, "REVIEW", DashboardPeriodEnums.WEEKLY, 70.0, 2, createdAt.plusSeconds(1));

		Review review1 = createMockReview(review1Id);
		Review review2 = createMockReview(review2Id);

		PopularReviewDto dto1 = createPopularReviewDto(review1Id, "책1", null, "유저1", "리뷰1", 5.0, 1L, 85.0, 10L, 3L);
		PopularReviewDto dto2 = createPopularReviewDto(review2Id, "책2", null, "유저2", "리뷰2", 4.0, 2L, 70.0, 5L, 2L);

		DashboardPageRequest request = new DashboardPageRequest(
				DashboardPeriodEnums.WEEKLY, "ASC", null, null, 2);

		when(dashboardRepository.findRecentRankingsByCursorAsc(
				eq(DashboardTargetType.REVIEW),
				eq(DashboardPeriodEnums.WEEKLY),
				any(),
				anyInt(),
				any(org.springframework.data.domain.Pageable.class)
		)).thenReturn(List.of(dashboard1, dashboard2));

		when(reviewRepository.findAllById(anyList())).thenReturn(List.of(review1, review2));
		when(dashboardMapper.toPopularReviewDto(dashboard1, review1)).thenReturn(dto1);
		when(dashboardMapper.toPopularReviewDto(dashboard2, review2)).thenReturn(dto2);

		// when
		CursorPageResponsePopularReviewDto response = dashboardService.getPopularReviews(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isEqualTo("2");
		assertThat(response.size()).isEqualTo(2);
	}

	@Test
	@DisplayName("파워 유저 커서: 첫 페이지 조회 시 CursorPageResponse를 반환한다.")
	void getPowerUsers_cursor_returnsFirstPage() {
		// given
		UUID user1Id = UUID.randomUUID();
		UUID user2Id = UUID.randomUUID();
		Instant createdAt = Instant.parse("2026-03-09T05:00:00Z");

		Dashboard dashboard1 = createDashboardWithCreatedAt(user1Id, "USER", DashboardPeriodEnums.MONTHLY, 95.0, 1, createdAt);
		Dashboard dashboard2 = createDashboardWithCreatedAt(user2Id, "USER", DashboardPeriodEnums.MONTHLY, 80.0, 2, createdAt.plusSeconds(1));

		User user1 = createMockUser(user1Id, "파워유저1");
		User user2 = createMockUser(user2Id, "파워유저2");

		PowerUserDto dto1 = createPowerUserDto(user1Id, "파워유저1", DashboardPeriodEnums.MONTHLY, 1L, 95.0);
		PowerUserDto dto2 = createPowerUserDto(user2Id, "파워유저2", DashboardPeriodEnums.MONTHLY, 2L, 80.0);

		DashboardPageRequest request = new DashboardPageRequest(
				DashboardPeriodEnums.MONTHLY, "DESC", null, null, 2);

		when(dashboardRepository.findRecentRankingsByCursorDesc(
				eq(DashboardTargetType.USER),
				eq(DashboardPeriodEnums.MONTHLY),
				any(),
				anyInt(),
				any(org.springframework.data.domain.Pageable.class)
		)).thenReturn(List.of(dashboard1, dashboard2));

		when(userRepository.findAllById(anyList())).thenReturn(List.of(user1, user2));
		when(dashboardMapper.toPowerUserDto(dashboard1, user1)).thenReturn(dto1);
		when(dashboardMapper.toPowerUserDto(dashboard2, user2)).thenReturn(dto2);

		// when
		CursorPageResponsePowerUserDto response = dashboardService.getPowerUsers(request);

		// then
		assertThat(response.content()).hasSize(2);
		assertThat(response.hasNext()).isFalse();
		assertThat(response.nextCursor()).isEqualTo("2");
		assertThat(response.size()).isEqualTo(2);
	}

	@Test
	@DisplayName("인기 리뷰: 대시보드 데이터가 없으면 빈 리스트를 반환한다.")
	void getPopularReviews_returnsEmpty_whenNoDashboardData() {
		// given
		when(dashboardRepository.findRecentRankings(
				eq(DashboardTargetType.REVIEW),
				eq(DashboardPeriodEnums.WEEKLY),
				any(PageRequest.class)
		)).thenReturn(List.of());

		// when
		List<PopularReviewDto> response = dashboardService.getPopularReviews(DashboardPeriodEnums.WEEKLY);

		// then
		assertThat(response).isEmpty();
	}

	@Test
	@DisplayName("인기 리뷰: 대시보드 데이터가 있으면 점수 순(랭킹 순)으로 인기 리뷰 목록을 조회한다.")
	void getPopularReviews_returnsOrderedByRanking_whenDashboardDataExists() {
		// given
		UUID review1Id = UUID.randomUUID();
		UUID review2Id = UUID.randomUUID();

		Dashboard dashboard1 = createDashboard(review1Id, "REVIEW", DashboardPeriodEnums.MONTHLY, 85.0, 1);
		Dashboard dashboard2 = createDashboard(review2Id, "REVIEW", DashboardPeriodEnums.MONTHLY, 70.0, 2);

		Review review1 = createMockReview(review1Id);
		Review review2 = createMockReview(review2Id);

		PopularReviewDto dto1 = createPopularReviewDto(
				review1Id, "자바의 정석", "https://example.com/thumb1.jpg", "사용자1",
				"좋은 책이에요", 5.0, 1L, 85.0, 10L, 3L);
		PopularReviewDto dto2 = createPopularReviewDto(
				review2Id, "클린 코드", null, "사용자2",
				"재미있어요", 4.0, 2L, 70.0, 5L, 2L);

		when(dashboardRepository.findRecentRankings(
				eq(DashboardTargetType.REVIEW),
				eq(DashboardPeriodEnums.MONTHLY),
				any(PageRequest.class)
		)).thenReturn(List.of(dashboard1, dashboard2));

		when(reviewRepository.findAllById(anyList())).thenReturn(List.of(review1, review2));

		when(dashboardMapper.toPopularReviewDto(dashboard1, review1)).thenReturn(dto1);
		when(dashboardMapper.toPopularReviewDto(dashboard2, review2)).thenReturn(dto2);

		// when
		List<PopularReviewDto> response = dashboardService.getPopularReviews(DashboardPeriodEnums.MONTHLY);

		// then
		assertThat(response).hasSize(2);
		assertThat(response.get(0).rank()).isEqualTo(1L);
		assertThat(response.get(0).reviewId()).isEqualTo(review1Id);
		assertThat(response.get(0).reviewContent()).isEqualTo("좋은 책이에요");
		assertThat(response.get(0).score()).isEqualTo(85.0);

		assertThat(response.get(1).rank()).isEqualTo(2L);
		assertThat(response.get(1).reviewId()).isEqualTo(review2Id);
		assertThat(response.get(1).reviewContent()).isEqualTo("재미있어요");
		assertThat(response.get(1).score()).isEqualTo(70.0);
	}

	@Test
	@DisplayName("파워유저: 대시보드 데이터가 없으면 빈 리스트를 반환한다.")
	void getPowerUsers_returnsEmpty_whenNoDashboardData() {
		// given
		when(dashboardRepository.findRecentRankings(
				eq(DashboardTargetType.USER),
				eq(DashboardPeriodEnums.WEEKLY),
				any(PageRequest.class)
		)).thenReturn(List.of());

		// when
		List<PowerUserDto> response = dashboardService.getPowerUsers(DashboardPeriodEnums.WEEKLY);

		// then
		assertThat(response).isEmpty();
	}

	@Test
	@DisplayName("파워유저: 대시보드 데이터가 있으면 점수 순(랭킹 순)으로 파워유저 목록을 조회한다.")
	void getPowerUsers_returnsOrderedByRanking_whenDashboardDataExists() {
		// given
		UUID user1Id = UUID.randomUUID();
		UUID user2Id = UUID.randomUUID();

		Dashboard dashboard1 = createDashboard(user1Id, "USER", DashboardPeriodEnums.MONTHLY, 95.0, 1);
		Dashboard dashboard2 = createDashboard(user2Id, "USER", DashboardPeriodEnums.MONTHLY, 80.0, 2);

		User user1 = createMockUser(user1Id, "파워유저1");
		User user2 = createMockUser(user2Id, "파워유저2");

		PowerUserDto dto1 = createPowerUserDto(user1Id, "파워유저1", DashboardPeriodEnums.MONTHLY, 1L, 95.0);
		PowerUserDto dto2 = createPowerUserDto(user2Id, "파워유저2", DashboardPeriodEnums.MONTHLY, 2L, 80.0);

		when(dashboardRepository.findRecentRankings(
				eq(DashboardTargetType.USER),
				eq(DashboardPeriodEnums.MONTHLY),
				any(PageRequest.class)
		)).thenReturn(List.of(dashboard1, dashboard2));

		when(userRepository.findAllById(anyList())).thenReturn(List.of(user1, user2));

		when(dashboardMapper.toPowerUserDto(dashboard1, user1)).thenReturn(dto1);
		when(dashboardMapper.toPowerUserDto(dashboard2, user2)).thenReturn(dto2);

		// when
		List<PowerUserDto> response = dashboardService.getPowerUsers(DashboardPeriodEnums.MONTHLY);

		// then
		assertThat(response).hasSize(2);
		assertThat(response.get(0).rank()).isEqualTo(1L);
		assertThat(response.get(0).userId()).isEqualTo(user1Id);
		assertThat(response.get(0).nickname()).isEqualTo("파워유저1");
		assertThat(response.get(0).score()).isEqualTo(95.0);

		assertThat(response.get(1).rank()).isEqualTo(2L);
		assertThat(response.get(1).userId()).isEqualTo(user2Id);
		assertThat(response.get(1).nickname()).isEqualTo("파워유저2");
		assertThat(response.get(1).score()).isEqualTo(80.0);
	}

	private Dashboard createDashboard(UUID targetId, String targetType, DashboardPeriodEnums periodType,
			double score, int rankingPos) {
		return Dashboard.builder()
				.targetId(targetId)
				.targetType(targetType)
				.periodType(periodType)
				.score(score)
				.rankingPos(rankingPos)
				.build();
	}

	private Dashboard createDashboardWithCreatedAt(UUID targetId, String targetType, DashboardPeriodEnums periodType,
			double score, int rankingPos, Instant createdAt) {
		Dashboard dashboard = createDashboard(targetId, targetType, periodType, score, rankingPos);
		ReflectionTestUtils.setField(dashboard, "createdAt", createdAt);
		return dashboard;
	}

	private PopularBookDto createPopularBookDto(UUID bookId, String title, String author, String thumbnailUrl,
			DashboardPeriodEnums period, long rank, double score, long reviewCount, double rating) {
		return new PopularBookDto(
				UUID.randomUUID(),
				bookId,
				title,
				author,
				thumbnailUrl,
				period,
				rank,
				score,
				reviewCount,
				rating,
				Instant.now()
		);
	}

	private PopularReviewDto createPopularReviewDto(UUID reviewId, String bookTitle, String bookThumbnailUrl,
			String userNickname, String reviewContent, double reviewRating, long rank, double score,
			long likeCount, long commentCount) {
		return new PopularReviewDto(
				UUID.randomUUID(),
				reviewId,
				UUID.randomUUID(),
				bookTitle,
				bookThumbnailUrl,
				UUID.randomUUID(),
				userNickname,
				reviewContent,
				reviewRating,
				DashboardPeriodEnums.MONTHLY,
				Instant.now(),
				rank,
				score,
				likeCount,
				commentCount
		);
	}

	private PowerUserDto createPowerUserDto(UUID userId, String nickname, DashboardPeriodEnums period,
			long rank, double score) {
		return new PowerUserDto(
				userId,
				nickname,
				period,
				Instant.now(),
				rank,
				score,
				0.0,
				0L,
				0L
		);
	}

	private Book createMockBook(UUID id) {
		Book book = org.mockito.Mockito.mock(Book.class);
		when(book.getId()).thenReturn(id);
		return book;
	}

	private Review createMockReview(UUID id) {
		Review review = org.mockito.Mockito.mock(Review.class);
		when(review.getId()).thenReturn(id);
		return review;
	}

	private User createMockUser(UUID id, String nickname) {
		User user = org.mockito.Mockito.mock(User.class);
		when(user.getId()).thenReturn(id);
		return user;
	}
}
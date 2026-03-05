package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.PopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.integration.support.IntegrationTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.DashboardRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DashboardServiceTest extends IntegrationTestSupport {

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private BookRepository bookRepository;

	@DisplayName("대시보드 데이터가 없으면 빈 리스트를 반환한다.")
	@Test
	void getPopularBooks_returnsEmpty_whenNoDashboardData() {
		// when
		List<PopularBookDto> response = dashboardService.getPopularBooks();

		// then
		assertThat(response).isEmpty();
	}

	@DisplayName("대시보드 데이터가 있으면 점수 순(랭킹 순)으로 인기 도서 목록을 조회한다.")
	@Test
	void getPopularBooks_returnsOrderedByRanking_whenDashboardDataExists() {
		// given
		Book book1 = Book.builder()
				.title("자바의 정석")
				.author("남궁성")
				.isbn("9788994492032")
				.publisher("도우출판")
				.publishedDate(LocalDate.now())
				.description("자바 기초")
				.thumbnailUrl("https://example.com/thumb1.jpg")
				.build();
		Book savedBook1 = bookRepository.save(book1);

		Book book2 = Book.builder()
				.title("클린 코드")
				.author("로버트 마틴")
				.isbn("9788966260959")
				.publisher("인사이트")
				.publishedDate(LocalDate.now())
				.description("클린 코드")
				.thumbnailUrl(null)
				.build();
		Book savedBook2 = bookRepository.save(book2);

		Dashboard dashboard1 = Dashboard.builder()
				.targetId(savedBook1.getId())
				.targetType("BOOK")
				.periodType(DashboardPeriodEnums.ALL_TIME)
				.score(90.0)
				.rankingPos(1)
				.build();
		dashboardRepository.save(dashboard1);

		Dashboard dashboard2 = Dashboard.builder()
				.targetId(savedBook2.getId())
				.targetType("BOOK")
				.periodType(DashboardPeriodEnums.ALL_TIME)
				.score(80.0)
				.rankingPos(2)
				.build();
		dashboardRepository.save(dashboard2);

		// when
		List<PopularBookDto> response = dashboardService.getPopularBooks();

		// then
		assertThat(response).hasSize(2);
		assertThat(response.get(0).rank()).isEqualTo(1L);
		assertThat(response.get(0).title()).isEqualTo("자바의 정석");
		assertThat(response.get(0).author()).isEqualTo("남궁성");
		assertThat(response.get(0).bookId()).isEqualTo(savedBook1.getId());
		assertThat(response.get(0).score()).isEqualTo(90.0);
		assertThat(response.get(0).thumbnailUrl()).isEqualTo("https://example.com/thumb1.jpg");

		assertThat(response.get(1).rank()).isEqualTo(2L);
		assertThat(response.get(1).title()).isEqualTo("클린 코드");
		assertThat(response.get(1).author()).isEqualTo("로버트 마틴");
		assertThat(response.get(1).bookId()).isEqualTo(savedBook2.getId());
		assertThat(response.get(1).score()).isEqualTo(80.0);
		assertThat(response.get(1).thumbnailUrl()).isNull();
	}
}
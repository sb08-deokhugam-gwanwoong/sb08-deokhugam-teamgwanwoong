package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.codeit.project.sb08deokhugamteamgwanwoong.config.ExternalApiConfig;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(NaverBookProvider.class)
@Import(ExternalApiConfig.class)
public class NaverBookProviderTest {

  @Autowired
  private NaverBookProvider naverBookProvider;

  @Autowired
  private RestTemplate restTemplate;

  private MockRestServiceServer mockServer;

  @BeforeEach
  void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);
  }

  @DisplayName("네이버 API를 호출해서 도서 정보를 가져온다.")
  @Test
  void getBookMetadata() {
    // given
    String isbn = "9788965402602";
    // 실제 구현에서 사용할 URL 패턴과 일치해야함
    String expectedUrl = "https://openapi.naver.com/v1/search/book.json?query=" + isbn + "&display=1";

    // 네이버 API 응답 예시 (JSON)
    String mockResponse = """
        {
          "items": [
            {
              "title": "객체지향의 사실과 오해",
              "author": "조영호",
              "publisher": "위키북스",
              "description": "역할, 책임, 협력 관점에서 본 객체지향",
              "image": "https:////bookthumb-phinf.pstatic.net/cover/092/006/09200628.jpg?type=m1&udate=20150611",
              "pubdate": "20150617"
             }
          ]
        }
        """;

    // Mock 서버 설정: 특정 URL로 GET 요청이 오면 mockResponse로 반환
    mockServer.expect(requestTo(expectedUrl))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(mockResponse, MediaType.APPLICATION_JSON));

    // when
    BookDto bookDto = naverBookProvider.getBookMetadata(isbn);

    // then
    assertThat(bookDto.title()).isEqualTo("객체지향의 사실과 오해");
    assertThat(bookDto.author()).isEqualTo("조영호");
    assertThat(bookDto.isbn()).isEqualTo("9788965402602");
    assertThat(bookDto.publisher()).isEqualTo("위키북스");
    // 날짜 형식 변환 등은 구현 단계에서 처리해야 함
  }
}

package com.codeit.project.sb08deokhugamteamgwanwoong.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.integration.support.IntegrationTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

public class BookServiceCacheTest extends IntegrationTestSupport {

  @Autowired
  protected BookService bookService;

  // 실제 네이버 API 호출 막기
  @MockitoBean
  private RestTemplate restTemplate;

  @DisplayName("Redis 캐싱 적용 확인. 동일한 ISBN 조회 시 외부 API는 1번만 호출된다.")
  @Test
  void getBookInfoByIsbn_CachingTest() {
    // given
    String isbn = "TEST-ISBN_" + UUID.randomUUID().toString();

    // 네이버 API가 반환할 가짜 JSON 문자열
    String mockJsonResponse = """
            {
              "items": [
                {
                  "title": "테스트 도서",
                  "author": "테스트 저자",
                  "publisher": "테스트 출판사",
                  "description": "테스트 설명입니다.",
                  "pubdate": "20260316",
                  "isbn": "9791139730029",
                  "image": "http://test.com/image.jpg"
                }
              ]
            }
            """;

    ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);

    // RestTemplate.exchange() 메서드가 호출되면 가짜 JSON 반환하도록 설정
    given(restTemplate.exchange(
        anyString(),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        eq(String.class)
    )).willReturn(mockResponseEntity);

    // when
    // 같은 ISBN으로 연속 2번 조회 요청
    NaverBookDto result1 = bookService.getBookInfoByIsbn(isbn); // 1번째 호출 (캐시 Miss X -> 실제 로직 실행 O)
    NaverBookDto result2 = bookService.getBookInfoByIsbn(isbn); // 2번째 호출 (캐시 Hit  O -> 실제 로직 실행 X)

    // then
    // 외부 API 호출(RestTemplate.exchange)이 1번만 호출됐는지 검증
    verify(restTemplate, times(1)).exchange(
        anyString(),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        eq(String.class)
    );

    // 두 객체의 데이터가 동일한지 확인
    assertThat(result1.title()).isEqualTo("테스트 도서");
    assertThat(result1.title()).isEqualTo(result2.title());

  }
}

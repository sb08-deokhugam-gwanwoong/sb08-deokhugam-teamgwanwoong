package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverBookProvider implements BookMetadataProvider {

    private final RestTemplate restTemplate;

    @Value("${naver.api.client-id:dummy-id}")
    private String clientId;

    @Value("${naver.api.client-secret:dummy-secret}")
    private String clientSecret;

    @Override
    @Retryable(
        retryFor = { Exception.class }, // 특정 예외 지정 가능 (예: RestClientException)
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Cacheable(value = "bookMetadata", key = "#query", unless = "#result == null")
    public BookDto getBookMetadata(String query) {
        log.info("Fetching book metadata from Naver API for query: {}", query);

        // TODO: 실제 네이버 API 호출 로직 구현
        // HttpHeaders headers = new HttpHeaders();
        // headers.set("X-Naver-Client-Id", clientId);
        // headers.set("X-Naver-Client-Secret", clientSecret);
        // HttpEntity<String> entity = new HttpEntity<>(headers);
        // ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // 임시 반환값
        return BookDto.builder()
            .title("Naver Book: " + query)
            .author("Naver Author")
            .isbn("1234567890")
            .build();
    }
}

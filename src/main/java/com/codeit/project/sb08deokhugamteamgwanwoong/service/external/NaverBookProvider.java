package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.external.naver.NaverApiResponse;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.external.naver.NaverBookItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverBookProvider implements BookMetadataProvider<NaverBookDto> {

    private final RestTemplate restTemplate;

    @Value("${naver.api.client-id:dummy-id}")
    private String clientId;

    @Value("${naver.api.client-secret:dummy-secret}")
    private String clientSecret;

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Cacheable(value = "bookMetadata", key = "#query", unless = "#result == null")
    public NaverBookDto getBookMetadata(String query) {
        log.info("Fetching book metadata from Naver API for query: {}", query);

        String url = "https://openapi.naver.com/v1/search/book.json?query=" + query + "&display=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<NaverApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                NaverApiResponse.class
            );

            NaverApiResponse body = response.getBody();
            if (body != null && body.items() != null && !body.items().isEmpty()) {
                NaverBookItemResponse item = body.items().get(0);
                return NaverBookDto.builder()
                    .title(item.title())
                    .author(item.author())
                    .publisher(item.publisher())
                    .isbn(item.isbn())
                    .description(item.description())
                    .thumbnailImage(item.image())
                    .publishedDate(item.pubdate())
                    .build();
            }
        } catch (Exception e) {
            log.error("Failed to fetch book metadata from Naver API", e);
            throw new RuntimeException("Naver API 호출 중 오류 발생", e);
        }

        throw new RuntimeException("도서 정보를 찾을 수 없습니다: " + query);
    }
}

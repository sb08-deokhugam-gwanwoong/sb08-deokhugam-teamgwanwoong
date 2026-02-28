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
public class OcrSpaceBookProvider implements BookMetadataProvider {

    private final RestTemplate restTemplate;

    @Value("${ocr.api.key:dummy-key}")
    private String apiKey;

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    @Cacheable(value = "bookMetadata", key = "#query", unless = "#result == null")
    public BookDto getBookMetadata(String query) {
        log.info("Fetching book metadata from OCR Space API for query: {}", query);

        // TODO: 실제 OCR API 호출 로직 구현
        // HttpHeaders headers = new HttpHeaders();
        // headers.set("apikey", apiKey);
        // MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // body.add("base64Image", query); // query가 이미지 데이터라고 가정
        // HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        // ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // 임시 반환값
        return BookDto.builder()
            .title("OCR Book: " + query)
            .author("OCR Author")
            .isbn("0987654321")
            .build();
    }
}

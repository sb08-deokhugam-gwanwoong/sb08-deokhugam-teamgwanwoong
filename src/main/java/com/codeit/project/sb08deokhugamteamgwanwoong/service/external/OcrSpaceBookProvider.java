package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.GlobalErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrSpaceBookProvider implements BookMetadataProvider<String> {

    private static final String OCR_SPACE_API_KEY = "${ocr.space.api.key:dummy-key}";
    private static final String OCR_SPACE_API_URL = "${ocr.space.api.url:https://api.ocr.space/parse/image}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value(OCR_SPACE_API_KEY)
    private String apiKey;
    @Value(OCR_SPACE_API_URL)
    private String apiUrl;

    @Override
    @Retryable(
        // 통신 관련 에러만 재시도하도록 수정
        retryFor = { RestClientException.class, IOException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    // 이미지가 Base64 기반이라 길기 때문에 해시값을 캐시로 사용함
    @Cacheable(value = "ocrIsbn", key = "#base64Image.hashCode()", unless = "#result == null")
    public String getBookMetadata(String base64Image) {
        log.info("OCR Space API를 호출하여 ISBN을 추출합니다.");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("apiKey", apiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("base64Image", base64Image);
        body.add("language", "kor"); // 한글 최적화
        body.add("isOverlayRequired", "false"); // 좌표 비활성화 (속도 향상)

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity,
                String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode parseResults = root.path("ParsedResults");

            if (parseResults.isArray() && !parseResults.isEmpty()) {
                String parsedText = parseResults.get(0).path("ParsedText").asText();
                return extractIsbn(parsedText);
            }
            throw new BusinessException(BookErrorCode.OCR_TEXT_NOT_FOUND);
        } catch (BusinessException e) {
            throw e;
        } catch (RestClientException | IOException e) {
            // OCR 전용 에러
            throw new BusinessException(BookErrorCode.OCR_SERVER_ERROR);
        } catch (Exception e) {
            // 그 외 정말 모르는 에러만 500 에러처리
            throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "이미지에서 바코드를 인식하는 중 서버 오류가 발생했습니다.");
        }
    }

    // 정규표현식으로 13자리 ISBN만 추출하는 헬퍼 메서드
    private String extractIsbn(String text) {
        String cleanText = text.replaceAll("[\\-\\s]","");
        Pattern pattern = Pattern.compile("(978|979)\\d{10}");
        Matcher matcher = pattern.matcher(cleanText);

        if (matcher.find()) {
            return matcher.group();
        }
        throw new BusinessException(BookErrorCode.OCR_BARCODE_NOT_FOUND);
    }
}

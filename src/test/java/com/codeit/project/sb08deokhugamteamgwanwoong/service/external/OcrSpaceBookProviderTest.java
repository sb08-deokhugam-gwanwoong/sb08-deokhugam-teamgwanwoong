package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class OcrSpaceBookProviderTest {

  // 추후 OcrSpaceBookProvider 생성 시 의존성 주입을 위해 Mock 객체로 만들어둠
  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private OcrSpaceBookProvider ocrSpaceBookProvider;

  @BeforeEach
  void setUp() {
    // 단위 테스트 환경이므로 @Value("${ocr.api.key}") 값을 강제로 주입
    ReflectionTestUtils.setField(ocrSpaceBookProvider, "apiKey", "test-api-key");
    ReflectionTestUtils.setField(ocrSpaceBookProvider, "apiUrl", "https://api.ocr.space/parse/image");
  }

  @DisplayName("OCR API 응답에서 하이픈이나 공백이 섞인 테스트라도 ISBN 13자리978/979 시작)를 정확히 추출한다.")
  @Test
  void getBookMetaData_Success_ExtractIsbn() {
    // given
    String base64Image = "dummy_base64_string";
    // 하이픈, 공백, 문자열이 섞여있는 상황 가정
    String mockJsonResponse = """
        {
          "ParsedResults": [
            {
              "ParsedText": "정가 15,000원 ISBN 978-89- 94492 -03-2 바코드입니다."
            }
          ]
        }
        """;

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));

    // when
    String isbn = ocrSpaceBookProvider.getBookMetadata(base64Image);

    // then
    assertThat(isbn).isEqualTo("9788994492032"); // 정규식을 거친 숫자
  }

  @DisplayName("OCR API 응답 배열이 비어있거나 텍스트가 없으면 OCR_TEXT_NOT_FOUND 예외가 발생한다.")
  @Test
  void getBookMetadata_Fail_EmptyParsedResults() {
    // given
    String base64Image = "dummy_base64_string";
    String mockJsonResponse = "{ \"ParsedResults\": [] }"; // 빈 배열 응답

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));

    // when & then
    assertThatThrownBy(() -> ocrSpaceBookProvider.getBookMetadata(base64Image))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(BookErrorCode.OCR_TEXT_NOT_FOUND.getMessage());
  }

  @DisplayName("추출된 텍스트 안에 978/979로 시작하는 13자리 숫자가 없으면 OCR_BARCODE_NOT_FOUND 예외를 던진다.")
  @Test
  void getBookMetadata_Fail_NoIsbnPattern() {
    // given
    String base64Image = "dummy_base64_string";
    // 바코드가 아닌 다른 숫자만 있는 상황
    String mockJsonResponse = """
        {
          "ParsedResults": [
            {
              "ParsedText": "이 책의 가격은 15000원 이고 전화번호는 010-1234-5678 입니다."
            }
          ]
        }
        """;

    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));

    // when & then
    assertThatThrownBy(() -> ocrSpaceBookProvider.getBookMetadata(base64Image))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(BookErrorCode.OCR_BARCODE_NOT_FOUND.getMessage());
  }

  @DisplayName("RestTemplate 통신 중 런타임 예외가 발생하면 INTERNAL_SERVER_ERROR를 던진다.")
  @Test
  void getBookMetadata_Fail_InternalServerError() {
    // given
    String base64Image = "dummy_base64_string";
    when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
        .thenThrow(new RestClientException("OCR 서버 타임아웃"));

    // when & then
    assertThatThrownBy(() -> ocrSpaceBookProvider.getBookMetadata(base64Image))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("외부 OCR 서버와 통신할 수 없습니다");
  }
}

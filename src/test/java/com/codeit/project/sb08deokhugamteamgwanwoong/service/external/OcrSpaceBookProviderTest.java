package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
  }

  @Test
  @DisplayName("OCR API 껍데기(Stub) 로직이 하드코딩된 임시 BookDto를 정상 반환한다.")
  void getBookMetadata_Stub_Success() {
    // given
    String query = "base64_encoded_image_string_dummy";

    // when: 현재 통신 로직이 주석 처리되어 있으므로 바로 DTO가 튀어나옵니다.
    BookDto result = ocrSpaceBookProvider.getBookMetadata(query);

    // then
    assertThat(result.title()).isEqualTo("OCR Book: " + query);
    assertThat(result.author()).isEqualTo("OCR Author");
    assertThat(result.isbn()).isEqualTo("0987654321");
  }

}

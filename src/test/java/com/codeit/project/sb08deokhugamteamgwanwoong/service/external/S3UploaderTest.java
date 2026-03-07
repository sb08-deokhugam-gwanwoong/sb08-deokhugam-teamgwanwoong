package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
public class S3UploaderTest {

  @InjectMocks
  private S3Uploader s3Uploader;

  @Mock
  private S3Client s3Client;

  @BeforeEach
  void setUp() {
    // @Value("${spring.cloud.aws.s3.bucket}") 값을 주입
    ReflectionTestUtils.setField(s3Uploader, "bucket", "test-bucket");
  }

  @DisplayName("파일을 S3에 업로드하고 URL을 반환한다.")
  @Test
  void upload_Success() throws IOException {
    // given
    MockMultipartFile file = new MockMultipartFile(
        "file", "test.jpg", "image/jpeg", "content".getBytes()
    );

    S3Utilities s3Utilities = mock(S3Utilities.class);
    given(s3Client.utilities()).willReturn(s3Utilities);

    URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/uuid.jpg");
    given(s3Utilities.getUrl(any(GetUrlRequest.class))).willReturn(expectedUrl);

    // putObject는 PutObjectResponse를 반환
    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willReturn(PutObjectResponse.builder().build());

    // when
    String resultUrl = s3Uploader.upload(file);

    // then
    assertThat(resultUrl).isEqualTo(expectedUrl.toString());
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @DisplayName("S3 업로드 중 에러가 발생하면 예외를 던진다.")
  @Test
  void upload_Failure() throws IOException{
    // given
    MultipartFile file = mock(MultipartFile.class);

    given(file.isEmpty()).willReturn(false);
    given(file.getOriginalFilename()).willReturn("test.jpg");
    given(file.getContentType()).willReturn("image/jpeg");

    // getInputStream 호출 시 IOException 발생
    given(file.getInputStream()).willThrow(new IOException("IO Error"));

    // when & then
    assertThatThrownBy(() -> s3Uploader.upload(file))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("파일 업로드에 실패했습니다.");
  }
}

package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.GlobalErrorCode;
import java.io.File;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

  @DisplayName("업로드할 파일이 null이면 null을 반환한다.")
  @Test
  void upload_ReturnNull_WhenFileIsNull() {
    // when
    String result = s3Uploader.upload(null);

    // then
    assertThat(result).isNull();
  }

  @DisplayName("업로드할 파일이 비어있으면 null을 반환한다.")
  @Test
  void upload_ReturnNull_WhenFileIsEmpty() {
    // given
    MockMultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

    // when
    String result = s3Uploader.upload(emptyFile);

    // then
    assertThat(result).isNull();
  }

  @DisplayName("확장자가 없는 파일도 정상적으로 S3에 업로드한다.")
  @Test
  void upload_Success_WithoutExtension() throws IOException {
    // given (확장자 없는 파일명)
    MockMultipartFile fileWithoutExt = new MockMultipartFile(
        "file", "testfile_no_extension", "image/jpeg", "content".getBytes()
    );

    S3Utilities s3Utilities = mock(S3Utilities.class);
    given(s3Client.utilities()).willReturn(s3Utilities);

    URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/uuid");
    given(s3Utilities.getUrl(any(GetUrlRequest.class))).willReturn(expectedUrl);

    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willReturn(PutObjectResponse.builder().build());

    // when
    String resultUrl = s3Uploader.upload(fileWithoutExt);

    // then
    assertThat(resultUrl).isEqualTo(expectedUrl.toString());
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  /*
   * Delete 메서드 관련 테스트
   * */
  @DisplayName("삭제할 파일 URL이 null이거나 비어있으면 아무 작업도 하지 않고 리턴한다.")
  @Test
  void delete_ReturnEarly_WhenUrlIsNullOrBlank() {
    // when
    s3Uploader.delete(null);
    s3Uploader.delete("");
    s3Uploader.delete("  ");

    // then
    // s3Client의 deleteObject가 한 번도 호출되지 않아야 함
    verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
  }

  @DisplayName("S3 URL을 전달하면 정상적으로 파일을 삭제한다.")
  @Test
  void delete_Success() {
    // given
    String fileUrl = "https://test-bucket.s3.amazonaws.com/test.jpg";

    // when
    s3Uploader.delete(fileUrl);

    // then
    verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
  }

  @DisplayName("파일 삭제 중 에러가 발생하면 로그를 남기고 정상 종료된다 (예외를 밖으로 던지지 않음).")
  @Test
  void delete_Failure_LogsError() {
    // given
    String fileUrl = "https://test-bucket.s3.amazonaws.com/test.jpg";

    // s3Client가 삭제 시도 시 예외를 던지도록 모킹
    doThrow(new RuntimeException("S3 Delete Error"))
        .when(s3Client).deleteObject(any(DeleteObjectRequest.class));

    // when & then
    // catch 블록에서 예외를 처리하므로 밖으로 에러가 전파되지 않아야 함
    assertDoesNotThrow(() -> {
      s3Uploader.delete(fileUrl);
    });

    verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
  }

  /*
  * S3 로그 파일 업로드 테스트
  * */
  @DisplayName("서버 로컬의 로그 파일을 S3에 성공적으로 업로드하고 URL을 반환한다.")
  @Test
  void uploadLogFile_Success() throws Exception {
    // given
    // 테스트용 임시 파일 생성
    File templogFile = File.createTempFile("app-2026-03-15", ".log");
    templogFile.deleteOnExit(); // 테스트 종료 시 자동 삭제

    S3Utilities s3Utilities = mock(S3Utilities.class);
    URL mockUrl = new URL("https://mock-bucket.s3.amazonaws.com/logs/" + templogFile.getName());

    // AWS S3Client mocking
    given(s3Client.utilities()).willReturn(s3Utilities);
    given(s3Utilities.getUrl(any(GetUrlRequest.class))).willReturn(mockUrl);

    // when
    String uploadUrl = s3Uploader.uploadLogFile(templogFile);

    // then
    assertThat(uploadUrl).isEqualTo(mockUrl.toExternalForm());

    // putObject가 1번 호출됐는지 검증
    verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @DisplayName("업로드할 로그 파일이 null이거나 존재하지 않으면 null을 반환한다")
  @Test
  void uploadLogFile_Fail_FileNotExists() {
    // given
    File notExistingFile = new File("not-existing-file.log");

    // when
    String result = s3Uploader.uploadLogFile(notExistingFile);

    // then
    assertThat(result).isNull();
    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @DisplayName("로그 파일 업로드 중 S3 서버 에러가 발생하면 예외를 발생시킨다")
  @Test
  void uploadLogFile_Fail_InternalServerError() throws Exception {
    // given
    File tempLogFile = File.createTempFile("app-2026-03-12", ".log");
    tempLogFile.deleteOnExit();

    // S3Client가 업로드 중 에러를 던지도록 설정
    given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
        .willThrow(new RuntimeException("S3 연결 시간 초과"));

    // when & then
    assertThatThrownBy(() -> s3Uploader.uploadLogFile(tempLogFile))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(GlobalErrorCode.FILE_UPLOAD_FAILED.getMessage());
  }
}

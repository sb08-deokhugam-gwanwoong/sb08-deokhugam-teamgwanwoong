package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.GlobalErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final static String BUCKET = "${spring.cloud.aws.s3.bucket}";

  // AWS S3와 통신하기 위한 클라이언트 객체 -> S3Config에서 빈 등록
  private final S3Client s3Client;

  // S3 버킷 이름
  @Value(BUCKET)
  private String bucket;

  /**
   * MultipartFile을 리사이징(압축)해서 S3에 업로드하고, 업로드된 파일의 URL을 반환한다.
   *
   * @param file 업로드할 파일 (이미지 등)
   * @return 업로드된 파일의 S3 URL (실패 시 예외 발생, 파일 없으면 null 반환)
   * */
  public String upload(MultipartFile file) {
    // 1. 파일 비어있는지 확인
    if (file == null || file.isEmpty()) {
      return null;
    }

    // 2. 리사이징 시 포맷을 강제로 jpg로 확장자 고정
    String s3FileName = UUID.randomUUID().toString() + ".jpg";

    try {
      // 3. 리사이징 결과를 담을 메모리 스트림 생성
      ByteArrayOutputStream os = new ByteArrayOutputStream();

      // 4. Thumbnailator로 이미지 압축 + 리사이징 (400x600, 화질 80%)
      Thumbnails.of(file.getInputStream())
          .size(400, 600) // 비율을 유지하면서 축소
          .outputFormat("jpg") // jpg 일괄 변환
          .outputQuality(0.8) // 화질 80% -> 용량 축소
          .toOutputStream(os);

      byte[] imageBytes = os.toByteArray();

      // 5. S3 업로드 요청 객체 생성
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket) // 업로드할 버킷 이름
          .key(s3FileName) // 저장할 파일명 (Key)
          .contentType("image/jpeg") // 파일 MIME 타입 설정
          .build();

      // 6. S3에 파일 업로드
      // 바이트 배열을 바로 넘겨서 AWS SDK가 사이즈를 알아서 계산하게 함
      s3Client.putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));

      // 업로드된 파일 URL 반환
      // GetUrlRequest를 사용해서 해당 버킷과 키에 해당하는 URL 생성함
      return s3Client.utilities().getUrl(GetUrlRequest.builder()
          .bucket(bucket)
          .key(s3FileName)
          .build())
          .toExternalForm(); // URL 객체를 문자열로 변환

    } catch (IOException e) {
      // 8. 업로드 중 에러 발생 시 로그 남기고 예외 발생
      log.error("S3 업로드 실패: {}", e.getMessage());
      throw new BusinessException(GlobalErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  /**
   * S3에 저장된 파일을 삭제한다.
   *
   * @param fileUrl 삭제할 파일의 전체 S3 URL
   */
  public void delete(String fileUrl) {
    if (fileUrl == null || fileUrl.isBlank()) {
      return;
    }

    try {
      // URL에서 S3 객체 키(파일명) 추출
      String s3FileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

      // S3 삭제 요청 객체 생성
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(s3FileName)
          .build();

      // S3 클라이언트를 통해 삭제 실행
      s3Client.deleteObject(deleteObjectRequest);
      log.info("S3 파일 삭제 완료 : {} ", s3FileName);
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패 (URL: {}): {}", fileUrl, e.getMessage());
    }
  }

  /**
   * 서버 로컬의 파일(java.io.File)을 S3의 특정 디렉토리(logs/)에 업로드한다.
   *
   * @param file 업로드할 로컬 파일 객체
   * @return 업로드된 파일의 S3 URL (실패 시 예외 발생, 파일 없으면 null 반환)
   */
  public String uploadLogFile(File file) {
    if (file == null || !file.exists()) {
      return null;
    }

    // S3 내부의 logs/ 폴더 하위에 날짜별 파일 이름으로 저장
    String s3FileName = "logs/" + file.getName();

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket)
          .key(s3FileName)
          .contentType("text/plain")
          .build();

      // AWS SDK v2의 Request.fromFile()을 사용해서 로컬 파일을 바로 스트리밍 업로드
      s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

      String uploadUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
          .bucket(bucket)
          .key(s3FileName)
          .build())
          .toExternalForm();

      log.info("S3 로그 파일 업로드 완료: {}", uploadUrl);
      return uploadUrl;
    } catch (Exception e) {
      log.error("S3 로그 파일 업로드 실패(파일명: {}): {}", file.getName(), e.getMessage());
      throw new BusinessException(GlobalErrorCode.FILE_UPLOAD_FAILED);
    }
  }
}

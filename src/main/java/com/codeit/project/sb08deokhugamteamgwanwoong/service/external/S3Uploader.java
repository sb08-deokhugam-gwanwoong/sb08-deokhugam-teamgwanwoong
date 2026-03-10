package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.GlobalErrorCode;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
   * MultipartFile을 S3에 업로드하고, 업로드된 파일의 URL을 반환한다.
   *
   * @param file 업로드할 파일 (이미지 등)
   * @return 업로드된 파일의 S3 URL (실패 시 예외 발생, 파일 없으면 null 반환)
   * */
  public String upload(MultipartFile file) {
    // 1. 파일 비어있는지 확인
    if (file == null || file.isEmpty()) {
      return null;
    }

    // 2. 원본 파일명 추출
    String originalFileName = file.getOriginalFilename();

    // 3. 파일 확장자 추출 (.jpg, .png 등)
    String extension = "";
    if (originalFileName != null && originalFileName.contains(".")) {
      extension = originalFileName.substring(originalFileName.lastIndexOf("."));
    }

    // 4. S3에 저장할 고유 파일명 생성 (UUID 사용 -> 파일 중복 방지)
    String s3FileName = UUID.randomUUID().toString() + extension;

    try {
      // 5. S3 업로드 요청 객체 생성
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucket) // 업로드할 버킷 이름
          .key(s3FileName) // 저장할 파일명 (Key)
          .contentType(file.getContentType()) // 파일 MIME 타입 설정
          .build();

      // 6. S3에 파일 업로드
      // RequestBody.fromInputStream : 파일의 내용을 스트림으로 읽어서 전송
      // file.getSize() : 파일 크기를 명시해서 S3가 효율적으로 처리하도록 함
      s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

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
}

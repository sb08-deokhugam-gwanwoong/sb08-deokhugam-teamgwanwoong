package com.codeit.project.sb08deokhugamteamgwanwoong.component.scheduler;

import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogBackupScheduler {

  private final S3Uploader s3Uploader;

  // logback-spring.xml에 설정한 디렉토리 및 파일 네이밍 규칙
  private static final String LOG_DIR = "./logs";
  private static final String LOG_FILE_PREFIX = "app-";
  private static final String LOG_FILE_EXTNENSION = ".log";

  @Scheduled(cron = "0 5 0 * * *")
  public void backupYesterdayLog() {
    log.info("[로그 백업 스케줄러] 전날 로그 파일 S3 백업 시작");

    // 어제 날짜 계산
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String formattedDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    // 타겟 파일 객체 생성 ( ./logs/app-2026-03-13.log)
    String fileName = LOG_FILE_PREFIX + formattedDate + LOG_FILE_EXTNENSION;
    File logFile = new File(LOG_DIR, fileName);

    // 파일 존재 여부
    if (!logFile.exists()) {
      log.warn("[로그 백업 스케줄러] 백업할 로그 파일이 존재하지 않습니다: {}", logFile.getAbsolutePath());
      return;
    }

    try {
      // S3Uploader로 S3에 업로드
      String uploadUrl = s3Uploader.uploadLogFile(logFile);

      // 업로드 성공 시, 로컬 서버 파일 삭제
      if (uploadUrl != null) {
        boolean isDeleted = logFile.delete();
        if (isDeleted) {
          log.info("[로그 백업 스케줄러] 로그 백업 및 로컬 파일 삭제 완료: {}", fileName);
        } else {
          log.warn("[로그 백업 스케줄러] S3 업로드는 성공했으나 로컬 파일 삭제에 실패했습니다: {}", fileName);
        }
      }
    } catch (Exception e) {
      log.error("[로그 백업 스케줄러] 로그 파일 백업 중 예기치 않은 오류가 발생했습니다.", e);
    }
  }

}

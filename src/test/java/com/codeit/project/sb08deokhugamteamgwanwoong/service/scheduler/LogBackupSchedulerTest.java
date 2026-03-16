package com.codeit.project.sb08deokhugamteamgwanwoong.service.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.component.scheduler.LogBackupScheduler;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.bytebuddy.asm.Advice.Local;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LogBackupSchedulerTest {

  @Mock
  private S3Uploader s3Uploader;

  @InjectMocks
  private LogBackupScheduler logBackupScheduler;

  private final String LOG_DIR = "./logs";
  private File testLogFile;

  @BeforeEach
  void setUp() throws IOException {
    // 테스트 실행 시 로그 디렉토리가 없으면 생성
    File dir = new File(LOG_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }

    // 스케줄러가 찾을 어제 날짜 파일명 계산
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String formattedDate = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String fileName = "app-" + formattedDate + ".log";

    // 실제 가짜 로그 파일 생성
    testLogFile = new File(LOG_DIR, fileName);
    testLogFile.createNewFile();
  }

  @AfterEach
  void tearDown() {
    // 테스트 끝나면 파일이 남아있지 않도록 삭제 처리
    if (testLogFile != null && testLogFile.exists()) {
      testLogFile.delete();
    }
  }

  @DisplayName("어제 날짜의 로그 파일이 존재하면 S3에 업로드하고 로컬 파일을 삭제한다.")
  @Test
  void backupYesterdayLog_Success() {
    // given
    String expectedUrl = "https://s3.url/logs/app-yesterday.log";
    given(s3Uploader.uploadLogFile(any(File.class))).willReturn(expectedUrl);

    // 파일이 존재하는지 확인
    assertThat(testLogFile.exists()).isTrue();

    // when
    logBackupScheduler.backupYesterdayLog();

    // then
    // S3 업로더가 호출되었는가
    verify(s3Uploader).uploadLogFile(any(File.class));
    // 업로드 성공 후 로컬 파일이 삭제 되었는가
    assertThat(testLogFile.exists()).isFalse();
  }

  @DisplayName("백업할 로그 파일이 존재하지 않으면 S3 업로드 로직을 타지 않는다.")
  @Test
  void backupYesterdayLog_NoFile() {
    // given
    // 테스트용으로 만들어진 파일을 강제로 삭제시켜 '파일이 없는' 환경 구성
    testLogFile.delete();

    // when
    logBackupScheduler.backupYesterdayLog();

    // then
    // 파일이 없으므로 S3 업로드는 절대 호출되지 않는다
    verify(s3Uploader, never()).uploadLogFile(any());
  }

  @DisplayName("S3 업로드 중 에러가 발생하면 파일은 삭제되지 않고 유지된다.")
  @Test
  void backupYesterdayLog_Fail_S3UploadError() {
    // given
    // S3 서버 장애 상황 가정
    given(s3Uploader.uploadLogFile(any(File.class))).willThrow(new RuntimeException("S3 에러"));

    // when
    logBackupScheduler.backupYesterdayLog();

    // then
    // 에러가 났으므로 로컬 파일은 삭제되지 않고 안전하게 보존됨
    assertThat(testLogFile.exists()).isTrue();
  }
}

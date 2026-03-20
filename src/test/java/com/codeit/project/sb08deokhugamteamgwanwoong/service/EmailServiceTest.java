package com.codeit.project.sb08deokhugamteamgwanwoong.service;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private KafkaTemplate<String, Object> kafkaTemplate;

  @InjectMocks
  private EmailServiceImpl emailService;

  @Test
  @DisplayName("메일 발송 성공: 카프카 토픽으로 메시지를 발행해야 한다.")
  void sendVerificationCode_Test() {
    // Given
    String toEmail = "test@test.com";
    String code = "123456";

    // When
    emailService.sendVerificationCode(toEmail, code);

    // Then
    verify(kafkaTemplate).send(any(String.class), any(Object.class));
  }

  @Test
  @DisplayName("메일 발송 실패: MailException 발생 시 BusinessException을 (EMAIL_SEND_FAILED) 던져야 한다.")
  void sendVerificationCode_Fail_Test() {
    // Given
    String toEmail = "test@test.com";
    String code = "123456";

    // send()가 예외를 던지도록 설정
    willThrow(new RuntimeException("Kafka error"))
        .given(kafkaTemplate)
        .send(any(String.class), any(Object.class));

    // When & Then
    assertThatThrownBy(() -> emailService.sendVerificationCode(toEmail, code))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_SEND_FAILED);
  }
}

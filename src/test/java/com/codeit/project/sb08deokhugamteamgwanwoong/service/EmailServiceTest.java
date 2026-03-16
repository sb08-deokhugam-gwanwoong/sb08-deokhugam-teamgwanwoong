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
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailServiceImpl emailService;

  @Test
  @DisplayName("메일 발송 성공: SimpleMailMessage를 생성하여 발송을 시도해야 한다.")
  void sendVerificationCode_Test() {
    // Given
    String toEmail = "test@test.com";
    String code = "123456";

    // When
    emailService.sendVerificationCode(toEmail, code);

    // Then
    verify(mailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("메일 발송 실패: MailException 발생 시 BusinessException을 (EMAIL_SEND_FAILED) 던져야 한다.")
  void sendVerificationCode_Fail_Test() {
    // Given
    String toEmail = "test@test.com";
    String code = "123456";

    // send()가 예외를 던지도록 설정
    willThrow(new MailSendException("error"))
        .given(mailSender)
        .send(any(SimpleMailMessage.class));

    // When & Then
    assertThatThrownBy(() -> emailService.sendVerificationCode(toEmail, code))
        .isInstanceOf(BusinessException.class)
        .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.EMAIL_SEND_FAILED);
  }
}

package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserEmailEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.EmailConsumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class EmailConsumerTest {

  @Mock
  private JavaMailSender mailSender;

  @InjectMocks
  private EmailConsumer emailConsumer;

  @Test
  @DisplayName("컨슈머 수신 성공: 메시지를 받으면 올바른 형식의 메일을 발송해야 한다.")
  void consume_Success_Test() {
    // Given
    UserEmailEvent event = new UserEmailEvent("test@test.com", "123456", "비밀번호 찾기");

    // SimpleMailMessage를 캡처하기 위한 객체
    ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

    // When
    emailConsumer.consume(event);

    // Then
    verify(mailSender).send(messageCaptor.capture());

    SimpleMailMessage capturedMessage = messageCaptor.getValue();
    assertThat(capturedMessage.getTo()).contains("test@test.com");
    assertThat(capturedMessage.getSubject()).isEqualTo("비밀번호 찾기");
    assertThat(capturedMessage.getText()).contains("123456");
  }

  @Test
  @DisplayName("컨슈머 예외 처리: 메일 발송 중 예외가 발생해도 로직이 정상적으로 종료되어야 한다.")
  void consume_MailException_Test() {
    // Given
    UserEmailEvent event = new UserEmailEvent("test@test.com", "123456", "비밀번호 찾기");

    // mailSender.send() 호출 시 예외 발생 설정
    doThrow(new MailSendException("SMTP server error"))
        .when(mailSender).send(any(SimpleMailMessage.class));

    // When & Then
    emailConsumer.consume(event);

    verify(mailSender).send(any(SimpleMailMessage.class));
  }
}

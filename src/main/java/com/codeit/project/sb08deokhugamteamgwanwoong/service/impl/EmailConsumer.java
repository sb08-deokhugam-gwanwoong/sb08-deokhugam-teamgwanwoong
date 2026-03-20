package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumer {

  private final JavaMailSender mailSender;

  @KafkaListener(topics = "email-topic", groupId = "email-group")
  public void consume(UserEmailEvent event) {

    log.info("[Kafka Consumer] 이메일 메시지 수신 - To: {}", event.toEmail());

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(event.toEmail());
    message.setSubject(event.subject());
    message.setText(createEmailContent(event.code()));
    try {
      mailSender.send(message);
      log.info("[메일 발송 성공] email: {}", event.toEmail());
    } catch (MailException e) {
      log.error("[메일 발송 실패] email: {}, error: {}", event.toEmail(), e.getMessage());
    }
  }

  private String createEmailContent(String code) {
    return "안녕하세요, 관웅입니다.\n\n" +
        "비밀번호 재설정을 위한 인증번호는 다음과 같습니다.\n" +
        "인증번호: [" + code + "]\n\n" +
        "인증번호의 유효 시간은 3분입니다.\n" +
        "감사합니다.";
  }
}

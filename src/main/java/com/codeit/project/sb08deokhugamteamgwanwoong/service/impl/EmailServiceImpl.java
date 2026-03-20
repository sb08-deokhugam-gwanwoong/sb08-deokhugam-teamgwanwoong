package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.user.UserEmailEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private static final String TOPIC = "email-topic";

  @Override
  public void sendVerificationCode(String toEmail, String code) {

    try {
      log.info("[이메일 발송 이벤트 발행] - Email: {}", toEmail);
      UserEmailEvent event = new UserEmailEvent(toEmail, code, "[관웅팀][덕후감] 비밀번호 찾기 인증번호 안내");
      kafkaTemplate.send(TOPIC, event);
    } catch (Exception e) {
      log.warn("[이메일 발송 실패] 원인: {}", e.getMessage());
      throw new BusinessException(UserErrorCode.EMAIL_SEND_FAILED);
    }

  }
}
